package vistas;

import com.toedter.calendar.JCalendar;
import consultas.MatchCRUD;
import consultas.GameCRUD;
import tablas.Match;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

public class MatchesPanel extends JPanel {

    private Connection connection;
    private MatchCRUD matchCRUD;
    private GameCRUD gameCRUD;

    private final SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat SDF_TIME_HHMM = new SimpleDateFormat("hh:mm", Locale.US);
    private final SimpleDateFormat SDF_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMatchId;
    private JTextField txtTimeValue;
    private JComboBox<String> cmbAmPm;
    private JTextField txtPlayer1, txtPlayer2;
    private JDateChooser dateChooserMatch;
    private JComboBox<String> cmbMatchType, cmbResult, cmbGameName;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    // **DECLARACIÓN DE LABELS DE ERROR**
    private JLabel lblIdError, lblDateError, lblTimeError, lblPlayer1Error, lblPlayer2Error, lblResultError;

    private Font getBodyFont(int size) { return new Font("Roboto", Font.PLAIN, size); }
    private Font getTitleFont(int size) { return new Font("Roboto Slab", Font.BOLD, size); }
    private Font getBoldFont(int size) { return new Font("Roboto", Font.BOLD, size); }
    private static final int RADIUS_SM = 8;

    private static Color darken(Color color, float factor) { return new Color(Math.max(0, (int)(color.getRed() * factor)), Math.max(0, (int)(color.getGreen() * factor)), Math.max(0, (int)(color.getBlue() * factor))); }
    private static Color brighten(Color color, float factor) { return new Color(Math.min(255, (int)(color.getRed() * factor)), Math.min(255, (int)(color.getGreen() * factor)), Math.min(255, (int)(color.getBlue() * factor))); }

    public MatchesPanel(Connection connection, GameCRUD gameCRUD) {
        this.connection = connection;
        this.matchCRUD = new MatchCRUD(connection);
        this.gameCRUD = gameCRUD;
        SDF_TIMESTAMP.setLenient(false);

        initializeErrorLabels(); // Inicialización de labels
        initComponents();
        loadMatches();
        validateFields(); // Validación inicial para estado de botones
    }

    // --- Métodos de inicialización de soporte ---

    private void initializeErrorLabels() {
        lblIdError = createErrorLabel();
        lblDateError = createErrorLabel();
        lblTimeError = createErrorLabel();
        lblPlayer1Error = createErrorLabel();
        lblPlayer2Error = createErrorLabel();
        lblResultError = createErrorLabel();
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" "); // Siempre tiene espacio, incluso vacío
        label.setForeground(ACCENT_DANGER);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setBorder(new EmptyBorder(2, 0, 0, 0));
        return label;
    }

    private void initComponents() {
        setLayout(new BorderLayout(SPACING_MD, SPACING_MD));
        setBackground(BG_DARK_SECONDARY);
        setBorder(new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD));

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JPanel fieldsContainer = new JPanel(new GridBagLayout());
        fieldsContainer.setBackground(BG_CARD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        txtMatchId = createStyledTextField();
        dateChooserMatch = createStyledDateChooser();
        JPanel timeInputPanel = createStyledTimeInput();
        cmbMatchType = createStyledComboBox(new String[]{"Oficial", "Amistoso"});

        txtPlayer1 = createStyledTextField();
        txtPlayer2 = createStyledTextField();
        cmbGameName = createGameNameComboBox();
        cmbResult = createStyledComboBox(new String[]{"N/A", "Gana Jugador 1", "Gana Jugador 2", "Empate"});


        // --- Fila 0 ---
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 1.0; fieldsContainer.add(createValidatedFieldPanel("ID del Partido:", txtMatchId, lblIdError), gbc);
        gbc.gridx = 1; gbc.weightx = 2.0; fieldsContainer.add(createValidatedDatePanel("Fecha:", dateChooserMatch, lblDateError), gbc);
        gbc.gridx = 2; gbc.weightx = 1.0; fieldsContainer.add(createValidatedFieldPanel("Hora (hh:mm):", timeInputPanel, lblTimeError), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(createValidatedFieldPanel("Tipo:", cmbMatchType, null), gbc); // No hay validación de error para el tipo, pero mantenemos la estructura

        // --- Fila 1 ---
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        gbc.gridx = 0; gbc.weightx = 1.0; fieldsContainer.add(createValidatedFieldPanel("Nombre del Jugador 1:", txtPlayer1, lblPlayer1Error), gbc);
        gbc.gridx = 1; gbc.weightx = 1.5; fieldsContainer.add(createValidatedFieldPanel("Nombre del Jugador 2:", txtPlayer2, lblPlayer2Error), gbc);
        gbc.gridx = 2; gbc.weightx = 1.0; fieldsContainer.add(createValidatedFieldPanel("Nombre del Juego:", cmbGameName, null), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(createValidatedFieldPanel("Resultado:", cmbResult, lblResultError), gbc);

        // --- Listeners para validación en línea ---
        txtMatchId.addCaretListener(e -> validateFields());
        txtPlayer1.addCaretListener(e -> validateFields());
        txtPlayer2.addCaretListener(e -> validateFields());
        txtTimeValue.addCaretListener(e -> validateFields());

        cmbAmPm.addActionListener(e -> validateFields());
        cmbResult.addActionListener(e -> validateFields());

        dateChooserMatch.getDateEditor().addPropertyChangeListener(evt -> { if ("date".equals(evt.getPropertyName())) validateFields(); });

        // --- Botones ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Agregar", ACCENT_SUCCESS);
        btnUpdate = createStyledButton("Actualizar", ACCENT_WARNING);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addMatch());
        btnUpdate.addActionListener(e -> updateMatch());
        btnDelete.addActionListener(e -> deleteMatch());
        btnClear.addActionListener(e -> clearFieldsWithConfirmation());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        panel.add(fieldsContainer, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Métodos para crear paneles de validación (COPIA DE TEAMS) ---

    private JPanel createValidatedFieldPanel(String labelText, JComponent component, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(component, BorderLayout.NORTH);

        // Si no hay label de error (null), usamos un label vacío que sirve como relleno vertical
        JLabel errorComponent = errorLabel != null ? errorLabel : createErrorLabel();
        contentPanel.add(errorComponent, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createValidatedDatePanel(String labelText, JDateChooser dateChooser, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(dateChooser, BorderLayout.NORTH);

        // Si no hay label de error (null), usamos un label vacío que sirve como relleno vertical
        JLabel errorComponent = errorLabel != null ? errorLabel : createErrorLabel();
        contentPanel.add(errorComponent, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(getBodyFont(FONT_SIZE_BODY));
        textField.setForeground(TEXT_PRIMARY);
        textField.setBackground(BG_INPUT);
        textField.setCaretColor(ACCENT_PRIMARY);

        // CAMBIAR ESTO:
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)  // ← ELIMINAR ESTO
        ));

        // POR ESTO (igual que LeaguesPanel):
        textField.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));

        return textField;
    }

    private JPanel createStyledTimeInput() {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, 0));
        panel.setBackground(BG_CARD);

        txtTimeValue = createStyledTextField();
        txtTimeValue.setText("12:00");

        cmbAmPm = createStyledComboBox(new String[]{"AM", "PM"});
        cmbAmPm.setMaximumSize(new Dimension(80, cmbAmPm.getPreferredSize().height));

        panel.add(txtTimeValue, BorderLayout.CENTER);
        panel.add(cmbAmPm, BorderLayout.EAST);

        return panel;
    }

    private JDateChooser createStyledDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");

        // Configurar tamaño preferido
        dateChooser.setPreferredSize(new Dimension(150, 35));
        dateChooser.setMinimumSize(new Dimension(150, 35));

        JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getDateEditor();

        // Configurar fuente y tamaño
        dateEditor.setFont(getBodyFont(FONT_SIZE_BODY));

        // FORZAR COLOR BLANCO
        dateEditor.setForeground(Color.WHITE);

        dateEditor.setBackground(BG_INPUT);
        dateEditor.setCaretColor(ACCENT_PRIMARY);
        dateEditor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));

        // Configurar tamaño del editor
        Component editorComponent = dateChooser.getDateEditor().getUiComponent();
        if (editorComponent instanceof JTextField) {
            JTextField textField = (JTextField) editorComponent;
            textField.setPreferredSize(new Dimension(150, 35));
            textField.setMinimumSize(new Dimension(150, 35));
        }

        dateChooser.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Configurar el calendario
        JCalendar calendar = dateChooser.getJCalendar();
        calendar.setBackground(BG_INPUT);

        // Configurar días del calendario
        calendar.getDayChooser().getDayPanel().setBackground(BG_INPUT);
        calendar.getDayChooser().setForeground(Color.WHITE);
        calendar.getDayChooser().setBackground(BG_INPUT);
        calendar.getDayChooser().setDecorationBackgroundColor(BG_INPUT);
        calendar.getDayChooser().setDecorationBordersVisible(false);

        // Forzar color blanco en el campo de texto
        dateChooser.addPropertyChangeListener("date", evt -> {
            SwingUtilities.invokeLater(() -> {
                dateEditor.setForeground(Color.WHITE);
                if (editorComponent instanceof JTextField) {
                    ((JTextField) editorComponent).setForeground(Color.WHITE);
                }
            });
        });

        // Listener adicional para asegurar color
        dateEditor.addPropertyChangeListener(evt -> {
            if ("foreground".equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(() -> dateEditor.setForeground(Color.WHITE));
            }
        });

        return dateChooser;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cmb = new JComboBox<>(items);
        cmb.setFont(getBodyFont(FONT_SIZE_BODY));
        cmb.setForeground(BG_DARK_PRIMARY);
        cmb.setBackground(BG_INPUT);

        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setFont(getBodyFont(FONT_SIZE_BODY));

                if (isSelected) {
                    label.setBackground(ACCENT_PRIMARY);
                    label.setForeground(BG_DARK_PRIMARY);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(BG_DARK_PRIMARY);
                }

                list.setBackground(Color.WHITE);

                label.setBorder(new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD));

                return label;
            }
        });

        // CAMBIAR ESTO:
        cmb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)  // ← ELIMINAR ESTO
        ));

        // POR ESTO (opcional, pero para consistencia):
        cmb.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));

        return cmb;
    }

    private JComboBox<String> createGameNameComboBox() {
        JComboBox<String> cmb = createStyledComboBox(new String[]{});
        try {
            List<String> gameNames = gameCRUD.getAllGameNames();
            for (String name : gameNames) {
                cmb.addItem(name);
            }
        } catch (SQLException e) {
            showError("Error al cargar nombres de juego: " + e.getMessage(), "Error");
        }
        return cmb;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JLabel titleLabel = new JLabel("Lista de Partidos");
        titleLabel.setFont(getTitleFont(FONT_SIZE_H2));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, SPACING_MD, 0));

        String[] columns = {"ID", "Fecha", "Resultado", "Tipo", "Jugador 1", "Jugador 2", "Juego"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.tableModel = tableModel;

        table = new JTable(tableModel);
        table.setFont(getBodyFont(FONT_SIZE_BODY));
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_INPUT);
        table.setRowHeight(35);
        table.setGridColor(BORDER_LIGHT);
        table.setSelectionBackground(ACCENT_PRIMARY);
        table.setSelectionForeground(BG_DARK_PRIMARY);
        table.setShowVerticalLines(true);

        table.getTableHeader().setFont(getBoldFont(FONT_SIZE_BODY));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new CustomTableHeaderRenderer(BG_DARK_PRIMARY, Color.WHITE));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_PRIMARY));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedMatch();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(BG_INPUT);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(darken(bgColor, 0.8f));
                } else if (getModel().isRollover()) {
                    g2d.setColor(brighten(bgColor, 1.1f));
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_SM, RADIUS_SM);

                g2d.setColor(BG_DARK_PRIMARY);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        button.setFont(getBoldFont(FONT_SIZE_BODY));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(130, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void loadMatches() {
        tableModel.setRowCount(0);
        try {
            List<Match> matches = matchCRUD.getAllMatches();
            for (Match match : matches) {
                Object[] row = {
                        match.getMatchId(),
                        match.getMatchDate(),
                        match.getResult(),
                        match.getMatchType(),
                        match.getPlayer1(),
                        match.getPlayer2(),
                        match.getGameCode()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error al cargar partidos: " + e.getMessage(), "Error");
        }
    }

    private void loadSelectedMatch() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtMatchId.setText(tableModel.getValueAt(selectedRow, 0).toString());

            String timestampStr = tableModel.getValueAt(selectedRow, 1).toString();
            try {
                Date date = SDF_TIMESTAMP.parse(timestampStr);
                dateChooserMatch.setDate(date);

                SimpleDateFormat hourMinuteFormatter = new SimpleDateFormat("hh:mm", Locale.US);
                SimpleDateFormat amPmFormatter = new SimpleDateFormat("a", Locale.US);

                txtTimeValue.setText(hourMinuteFormatter.format(date));
                cmbAmPm.setSelectedItem(amPmFormatter.format(date).toUpperCase());

            } catch (ParseException e) {
                dateChooserMatch.setDate(null);
                txtTimeValue.setText("12:00");
                cmbAmPm.setSelectedItem("AM");
            }

            cmbResult.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            cmbMatchType.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
            txtPlayer1.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtPlayer2.setText(tableModel.getValueAt(selectedRow, 5).toString());
            cmbGameName.setSelectedItem(tableModel.getValueAt(selectedRow, 6).toString());

            txtMatchId.setEditable(false);
            clearErrorMessages();
            validateFields();
        }
    }

    private void addMatch() {
        if (!validateFields(true)) return;

        Timestamp matchDate = getFullTimestamp();
        if (matchDate == null) {
            showError("Error interno al parsear fecha u hora. Verifica el formato.", "Error de Parseo");
            return;
        }

        try {
            Match match = new Match(
                    Integer.parseInt(txtMatchId.getText().trim()),
                    matchDate,
                    cmbResult.getSelectedItem().toString(),
                    cmbMatchType.getSelectedItem().toString().toLowerCase(),
                    txtPlayer1.getText().trim(),
                    txtPlayer2.getText().trim(),
                    cmbGameName.getSelectedItem().toString()
            );

            if (matchCRUD.createMatch(match)) {
                showSuccess("¡Partido agregado exitosamente!");
                clearFields();
                loadMatches();
            } else {
                showError("No se pudo agregar el partido.", "Error");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (NumberFormatException e) {
            showError("El ID del partido debe ser un número entero válido.", "Error de Formato");
        }
    }

    private void updateMatch() {
        if (txtMatchId.getText().trim().isEmpty()) {
            showError("Selecciona un partido de la tabla para actualizar.", "Error");
            return;
        }
        if (!validateFields(false)) return;

        Timestamp matchDate = getFullTimestamp();
        if (matchDate == null) {
            showError("Error interno al parsear fecha u hora. Verifica el formato.", "Error de Parseo");
            return;
        }

        try {
            Match match = new Match(
                    Integer.parseInt(txtMatchId.getText().trim()),
                    matchDate,
                    cmbResult.getSelectedItem().toString(),
                    cmbMatchType.getSelectedItem().toString().toLowerCase(),
                    txtPlayer1.getText().trim(),
                    txtPlayer2.getText().trim(),
                    cmbGameName.getSelectedItem().toString()
            );

            if (matchCRUD.updateMatch(match)) {
                showSuccess("¡Partido actualizado exitosamente!");
                clearFields();
                loadMatches();
            } else {
                showError("No se pudo actualizar el partido.", "Error");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (NumberFormatException e) {
            showError("El ID del partido debe ser un número entero válido.", "Error de Formato");
        }
    }

    private void deleteMatch() {
        if (txtMatchId.getText().trim().isEmpty()) {
            showError("Selecciona un partido de la tabla para eliminar.", "Error");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar este partido?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (matchCRUD.deleteMatch(Integer.parseInt(txtMatchId.getText().trim()))) {
                    showSuccess("¡Partido eliminado exitosamente!");
                    clearFields();
                    loadMatches();
                } else {
                    showError("No se pudo eliminar el partido.", "Error");
                }
            } catch (SQLException e) {
                showError("Error al eliminar: " + e.getMessage(), "Error");
            }
        }
    }

    private void clearFieldsWithConfirmation() {
        if (txtMatchId.getText().trim().isEmpty() &&
                dateChooserMatch.getDate() == null &&
                txtTimeValue.getText().trim().equals("12:00") &&
                cmbAmPm.getSelectedItem().equals("AM") &&
                cmbResult.getSelectedIndex() == 0 &&
                cmbMatchType.getSelectedIndex() == 0 &&
                txtPlayer1.getText().trim().isEmpty() &&
                txtPlayer2.getText().trim().isEmpty()) {

            table.clearSelection();
            txtMatchId.setEditable(true);
            clearErrorMessages();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de limpiar todos los campos del formulario?",
                "Confirmar limpieza",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            clearFields();
        }
    }

    private void clearErrorMessages() {
        lblIdError.setText(" ");
        lblDateError.setText(" ");
        lblTimeError.setText(" ");
        lblPlayer1Error.setText(" ");
        lblPlayer2Error.setText(" ");
        lblResultError.setText(" ");
    }

    private void clearFields() {
        txtMatchId.setText("");
        dateChooserMatch.setDate(null);
        txtTimeValue.setText("12:00");
        cmbAmPm.setSelectedItem("AM");
        cmbResult.setSelectedIndex(0);
        cmbMatchType.setSelectedIndex(0);
        txtPlayer1.setText("");
        txtPlayer2.setText("");
        if (cmbGameName.getItemCount() > 0) cmbGameName.setSelectedIndex(0);
        txtMatchId.setEditable(true);
        table.clearSelection();

        clearErrorMessages();
        validateFields();
    }

    private boolean validateFields() {
        return validateFields(txtMatchId.isEditable());
    }

    private boolean validateFields(boolean isAdding) {
        clearErrorMessages(); // Limpiar errores al iniciar
        boolean isValid = true;

        String matchIdText = txtMatchId.getText().trim();
        String player1Text = txtPlayer1.getText().trim();
        String player2Text = txtPlayer2.getText().trim();
        String timeStr = txtTimeValue.getText().trim();
        String result = cmbResult.getSelectedItem().toString();
        Date dateSelected = dateChooserMatch.getDate();

        // 1. ID del Partido
        if (matchIdText.isEmpty()) {
            lblIdError.setText("ID obligatorio.");
            isValid = false;
        } else {
            try {
                int id = Integer.parseInt(matchIdText);
                if (id <= 0) {
                    lblIdError.setText("Debe ser un número positivo.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lblIdError.setText("Debe ser un número válido.");
                isValid = false;
            }
        }

        // 2. Jugador 1 (NOT NULL, CHECK LENGTH >= 3)
        if (player1Text.isEmpty()) {
            lblPlayer1Error.setText("Jugador 1 obligatorio.");
            isValid = false;
        } else if (player1Text.length() < 3) {
            lblPlayer1Error.setText("Mínimo 3 caracteres.");
            isValid = false;
        }

        // 3. Jugador 2 (NOT NULL, CHECK LENGTH >= 3)
        if (player2Text.isEmpty()) {
            lblPlayer2Error.setText("Jugador 2 obligatorio.");
            isValid = false;
        } else if (player2Text.length() < 3) {
            lblPlayer2Error.setText("Mínimo 3 caracteres.");
            isValid = false;
        }

        // 4. Jugadores Diferentes
        if (!player1Text.isEmpty() && !player2Text.isEmpty() && player1Text.length() >= 3 && player2Text.length() >= 3) {
            if (player1Text.equalsIgnoreCase(player2Text)) {
                lblPlayer1Error.setText("Los jugadores deben ser diferentes.");
                lblPlayer2Error.setText("Los jugadores deben ser diferentes.");
                isValid = false;
            }
        }

        // 5. Resultado (CHECK LENGTH >= 3)
        if (result.length() < 3) {
            lblResultError.setText("Resultado inválido (Mínimo 3 caracteres).");
            isValid = false;
        }

        // 6. Fecha (NOT NULL)
        if (dateSelected == null) {
            lblDateError.setText("Fecha obligatoria.");
            isValid = false;
        }

        // 7. Hora (Formato hh:mm, Rango 01-12)
        if (timeStr.isEmpty() || !timeStr.matches("\\d{2}:\\d{2}")) {
            lblTimeError.setText("Formato de hora inválido (hh:mm).");
            isValid = false;
        } else {
            try {
                Date timeTest = SDF_TIME_HHMM.parse(timeStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(timeTest);
                int hour = cal.get(Calendar.HOUR);
                if (hour < 1 || hour > 12) {
                    lblTimeError.setText("Hora debe ser 01 a 12.");
                    isValid = false;
                }
            } catch (ParseException e) {
                lblTimeError.setText("Hora no válida.");
                isValid = false;
            }
        }

        // 8. Fecha/Hora Futura (CHECK match_date <= CURRENT_TIMESTAMP)
        if (dateSelected != null && lblTimeError.getText().trim().isEmpty()) { // Solo checamos si la hora ya es válida
            Timestamp matchDate = getFullTimestamp();
            if (matchDate != null && matchDate.after(new Timestamp(System.currentTimeMillis()))) {
                lblDateError.setText("La fecha no puede ser futura.");
                isValid = false;
            }
        }

        // Control de botones
        boolean rowSelected = table.getSelectedRow() != -1;
        btnAdd.setEnabled(isValid && isAdding);
        btnUpdate.setEnabled(isValid && !isAdding && rowSelected);
        btnDelete.setEnabled(rowSelected);

        return isValid;
    }

    private Timestamp getFullTimestamp() {
        Date datePart = dateChooserMatch.getDate();
        String timeValueStr = txtTimeValue.getText().trim();
        String amPmStr = cmbAmPm.getSelectedItem().toString();

        if (datePart == null || timeValueStr.isEmpty()) {
            return null;
        }

        try {
            String fullTimeStr = timeValueStr + " " + amPmStr;

            SimpleDateFormat parser = new SimpleDateFormat("hh:mm a", Locale.US);
            Date timePart = parser.parse(fullTimeStr);

            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(datePart);

            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(timePart);

            dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            dateCal.set(Calendar.SECOND, 0);
            dateCal.set(Calendar.MILLISECOND, 0);

            return new Timestamp(dateCal.getTimeInMillis());
        } catch (ParseException e) {
            return null;
        }
    }

    private void handleSQLException(SQLException e) {
        String msg = e.getMessage();
        if (msg.contains("duplicate key")) {
            showError("Ya existe un partido con ese ID.", "Error");
        } else if (msg.contains("foreign key")) {
            showError("Nombre del jugador o juego no válidos. Asegúrate de que existen en la base de datos.", "Error");
        } else {
            showError("Error SQL: " + msg, "Error");
        }
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private class CustomTableHeaderRenderer extends JLabel implements TableCellRenderer {

        private final Color backgroundColor;
        private final Color foregroundColor;

        public CustomTableHeaderRenderer(Color backgroundColor, Color foregroundColor) {
            this.backgroundColor = backgroundColor;
            this.foregroundColor = foregroundColor;
            setOpaque(true);
            setFont(getBoldFont(FONT_SIZE_BODY));
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(255, 255, 255, 30)),
                    new EmptyBorder(5, 5, 5, 5)
            ));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            setBackground(backgroundColor);
            setForeground(foregroundColor);
            setText(value == null ? "" : value.toString());
            return this;
        }
    }
}