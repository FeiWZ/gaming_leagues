package vistas;

import consultas.MatchCRUD;
import consultas.GameCRUD;
import tablas.Match;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.awt.event.ActionListener;

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

    // Campos de entrada
    private JTextField txtMatchId;
    private JTextField txtTimeValue;
    private JComboBox<String> cmbAmPm;
    private JTextField txtPlayer1, txtPlayer2;
    private JDateChooser dateChooserMatch;
    private JComboBox<String> cmbMatchType, cmbResult, cmbGameName;

    // Botones
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    // Etiquetas de error
    private JLabel lblErrorMatchId, lblErrorDate, lblErrorTime, lblErrorType, lblErrorPlayer1, lblErrorPlayer2, lblErrorGame, lblErrorResult;
    private DocumentListener validationListener;


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

        validationListener = createValidationListener();

        initComponents();
        loadMatches();
        validateAllFields();
    }

    // --- MÉTODOS DE UTILIDAD Y ESTILO ---

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ACCENT_DANGER);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        return label;
    }

    private DocumentListener createValidationListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateAllFields(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateAllFields(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateAllFields(); }
        };
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

        // Inicializar etiquetas de error
        lblErrorMatchId = createErrorLabel();
        lblErrorDate = createErrorLabel();
        lblErrorTime = createErrorLabel();
        lblErrorType = createErrorLabel();
        lblErrorPlayer1 = createErrorLabel();
        lblErrorPlayer2 = createErrorLabel();
        lblErrorGame = createErrorLabel();
        lblErrorResult = createErrorLabel();


        JPanel fieldsContainer = new JPanel(new GridBagLayout());
        fieldsContainer.setBackground(BG_CARD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        // --- FILA 1 ---
        txtMatchId = createStyledTextField();
        dateChooserMatch = createStyledDateChooser();
        JPanel timeInputPanel = createStyledTimeInput();

        // CORRECCIÓN: Inicializar con opción vacía
        cmbMatchType = createStyledComboBox(new String[]{"", "Oficial", "Amistoso"});

        // Listeners para campos de texto
        txtMatchId.getDocument().addDocumentListener(validationListener);
        // txtTimeValue está en createStyledTimeInput y también se le añade listener.

        // Listener para JDateChooser
        dateChooserMatch.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) validateAllFields();
        });

        // Listener para ComboBoxes
        ActionListener cmbListener = e -> validateAllFields();
        // cmbAmPm.addActionListener(cmbListener); // Se añade en createStyledTimeInput

        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 1.0; fieldsContainer.add(createValidatedFieldPanel("ID del Partido:", txtMatchId, lblErrorMatchId), gbc);
        gbc.gridx = 1; gbc.weightx = 2.0; fieldsContainer.add(createValidatedDatePanel("Fecha:", dateChooserMatch, lblErrorDate), gbc);
        gbc.gridx = 2; gbc.weightx = 1.0; fieldsContainer.add(createValidatedTimePanel("Hora (hh:mm):", timeInputPanel, lblErrorTime), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(createValidatedComboBoxPanel("Tipo:", cmbMatchType, lblErrorType), gbc);

        // --- FILA 2 ---
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        txtPlayer1 = createStyledTextField();
        txtPlayer2 = createStyledTextField();

        // CORRECCIÓN: cmbGameName inicializado con opción vacía
        cmbGameName = createGameNameComboBox(true);

        // CORRECCIÓN: cmbResult inicializado con opción vacía
        cmbResult = createStyledComboBox(new String[]{"", "N/A", "Gana Jugador 1", "Gana Jugador 2", "Empate"});

        txtPlayer1.getDocument().addDocumentListener(validationListener);
        txtPlayer2.getDocument().addDocumentListener(validationListener);
        cmbGameName.addActionListener(cmbListener);
        cmbMatchType.addActionListener(cmbListener);
        cmbResult.addActionListener(cmbListener);

        gbc.gridx = 0; gbc.weightx = 1.0; fieldsContainer.add(createValidatedFieldPanel("Nombre del Jugador 1:", txtPlayer1, lblErrorPlayer1), gbc);
        gbc.gridx = 1; gbc.weightx = 1.5; fieldsContainer.add(createValidatedFieldPanel("Nombre del Jugador 2:", txtPlayer2, lblErrorPlayer2), gbc);
        gbc.gridx = 2; gbc.weightx = 1.0; fieldsContainer.add(createValidatedComboBoxPanel("Nombre del Juego:", cmbGameName, lblErrorGame), gbc);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(createValidatedComboBoxPanel("Resultado:", cmbResult, lblErrorResult), gbc);

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

    /** Crea un panel para JTextField que incluye label y error. */
    private JPanel createValidatedFieldPanel(String labelText, JTextField textField, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(textField, BorderLayout.NORTH);
        contentPanel.add(errorLabel, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /** Crea un panel para JComboBox que incluye label y error. */
    private JPanel createValidatedComboBoxPanel(String labelText, JComboBox<String> comboBox, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(comboBox, BorderLayout.NORTH);
        contentPanel.add(errorLabel, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /** Crea un panel para JDateChooser que incluye label y error. */
    private JPanel createValidatedDatePanel(String labelText, JDateChooser dateChooser, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(dateChooser, BorderLayout.NORTH);
        contentPanel.add(errorLabel, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /** Crea un panel para la entrada de Hora (compuesto) que incluye label y error. */
    private JPanel createValidatedTimePanel(String labelText, JPanel timeInputPanel, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(timeInputPanel, BorderLayout.NORTH);
        contentPanel.add(errorLabel, BorderLayout.SOUTH);

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
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));
        return textField;
    }

    private JPanel createStyledTimeInput() {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, 0));
        panel.setBackground(BG_CARD);

        txtTimeValue = createStyledTextField();
        // CORRECCIÓN: Inicializar sin valor por defecto
        txtTimeValue.setText("");

        // Listener añadido aquí para que esté disponible cuando se inicializa
        txtTimeValue.getDocument().addDocumentListener(validationListener);


        cmbAmPm = createStyledComboBox(new String[]{"AM", "PM"});
        cmbAmPm.setMaximumSize(new Dimension(80, cmbAmPm.getPreferredSize().height));
        cmbAmPm.addActionListener(e -> validateAllFields()); // Añadir listener

        panel.add(txtTimeValue, BorderLayout.CENTER);
        panel.add(cmbAmPm, BorderLayout.EAST);

        return panel;
    }

    private JDateChooser createStyledDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");

        JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getDateEditor();
        dateEditor.setColumns(10);
        dateEditor.setFont(getBodyFont(FONT_SIZE_BODY));
        dateEditor.setForeground(TEXT_PRIMARY);
        dateEditor.setBackground(BG_INPUT);
        dateEditor.setCaretColor(ACCENT_PRIMARY);
        dateEditor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));

        dateChooser.setBorder(new EmptyBorder(0, 0, 0, 0));
        dateChooser.getJCalendar().setBackground(Color.WHITE);
        dateChooser.getJCalendar().setForeground(BG_DARK_PRIMARY);
        dateChooser.getJCalendar().setWeekdayForeground(Color.BLACK);
        dateChooser.getJCalendar().setSundayForeground(Color.BLACK);
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

        cmb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));

        return cmb;
    }

    private JComboBox<String> createGameNameComboBox(boolean includeEmptyOption) {
        JComboBox<String> cmb = createStyledComboBox(new String[]{});
        if (includeEmptyOption) {
            cmb.addItem(""); // Opción vacía inicial
        }
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
            if (table != null && !e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedMatch();
                validateAllFields();
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

    // --- LÓGICA DE VALIDACIÓN Y DATOS ---

    private void validateAllFields() {
        if (btnAdd == null || btnUpdate == null) return;

        boolean isValid = true;

        String matchIdText = txtMatchId.getText().trim();
        String player1Text = txtPlayer1.getText().trim();
        String player2Text = txtPlayer2.getText().trim();
        String timeValueText = txtTimeValue.getText().trim();

        // 1. ID del Partido (Solo requerido para Agregar)
        lblErrorMatchId.setText(" ");
        if (matchIdText.isEmpty()) {
            lblErrorMatchId.setText("El ID es obligatorio.");
            isValid = false;
        } else {
            try {
                if (Integer.parseInt(matchIdText) <= 0) {
                    lblErrorMatchId.setText("Debe ser un número positivo.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorMatchId.setText("Debe ser un número entero válido.");
                isValid = false;
            }
        }

        // 2. Fecha (obligatoria)
        lblErrorDate.setText(" ");
        if (dateChooserMatch.getDate() == null) {
            lblErrorDate.setText("La fecha es obligatoria.");
            isValid = false;
        }

        // 3. Hora (obligatoria y formato)
        lblErrorTime.setText(" ");
        if (timeValueText.isEmpty()) {
            lblErrorTime.setText("La hora es obligatoria.");
            isValid = false;
        } else if (!timeValueText.matches("\\d{2}:\\d{2}")) {
            lblErrorTime.setText("Formato: hh:mm (ej: 03:30).");
            isValid = false;
        } else {
            try {
                Date timeTest = SDF_TIME_HHMM.parse(timeValueText);
                Calendar cal = Calendar.getInstance();
                cal.setTime(timeTest);
                int hour = cal.get(Calendar.HOUR);
                if (hour < 1 || hour > 12) {
                    lblErrorTime.setText("La hora debe ser entre 01 y 12.");
                    isValid = false;
                }
            } catch (ParseException e) {
                lblErrorTime.setText("Hora inválida.");
                isValid = false;
            }
        }

        // 4. Tipo (obligatorio)
        lblErrorType.setText(" ");
        if (cmbMatchType.getSelectedItem() == null || cmbMatchType.getSelectedItem().toString().isEmpty()) {
            lblErrorType.setText("El tipo es obligatorio.");
            isValid = false;
        }

        // 5. Jugador 1
        lblErrorPlayer1.setText(" ");
        if (player1Text.isEmpty()) {
            lblErrorPlayer1.setText("El jugador 1 es obligatorio.");
            isValid = false;
        } else if (player1Text.length() < 3) {
            lblErrorPlayer1.setText("Mínimo 3 caracteres.");
            isValid = false;
        }

        // 6. Jugador 2
        lblErrorPlayer2.setText(" ");
        if (player2Text.isEmpty()) {
            lblErrorPlayer2.setText("El jugador 2 es obligatorio.");
            isValid = false;
        } else if (player2Text.length() < 3) {
            lblErrorPlayer2.setText("Mínimo 3 caracteres.");
            isValid = false;
        } else if (player1Text.equalsIgnoreCase(player2Text)) {
            lblErrorPlayer2.setText("Deben ser jugadores diferentes.");
            isValid = false;
        }

        // 7. Nombre del Juego
        lblErrorGame.setText(" ");
        if (cmbGameName.getSelectedItem() == null || cmbGameName.getSelectedItem().toString().isEmpty()) {
            lblErrorGame.setText("El juego es obligatorio.");
            isValid = false;
        }

        // 8. Resultado
        lblErrorResult.setText(" ");
        if (cmbResult.getSelectedItem() == null || cmbResult.getSelectedItem().toString().isEmpty()) {
            lblErrorResult.setText("El resultado es obligatorio.");
            isValid = false;
        }

        // 9. Lógica de Timestamp (Comprobamos si el timestamp completo es válido)
        if (isValid && getFullTimestamp() == null) {
            lblErrorTime.setText("Hora o Fecha forman un Timestamp inválido.");
            isValid = false;
        }

        // Estado de botones
        boolean isRowSelected = (table != null && table.getSelectedRow() != -1);

        btnAdd.setEnabled(isValid && !isRowSelected);
        btnUpdate.setEnabled(isValid && isRowSelected);
        btnDelete.setEnabled(isRowSelected);

        // El ID solo es editable si estamos en modo Agregar
        txtMatchId.setEditable(!isRowSelected);
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
            clearFields(); // Limpiar campos y deseleccionar la tabla
        } catch (SQLException e) {
            showError("Error al cargar partidos: " + e.getMessage(), "Error");
        }
    }

    private void loadSelectedMatch() {
        if (table == null) return;

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
                txtTimeValue.setText("");
                cmbAmPm.setSelectedItem("AM");
            }

            cmbResult.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            cmbMatchType.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
            txtPlayer1.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtPlayer2.setText(tableModel.getValueAt(selectedRow, 5).toString());
            cmbGameName.setSelectedItem(tableModel.getValueAt(selectedRow, 6).toString());

            txtMatchId.setEditable(false);
            validateAllFields();
        }
    }

    private void addMatch() {
        if (!btnAdd.isEnabled()) {
            showError("Corrige los errores en los campos antes de agregar.", "Validación Pendiente");
            return;
        }

        Timestamp matchDate = getFullTimestamp();

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
            showError("Error de formato numérico: ID o campos vacíos.", "Error");
        }
    }

    private void updateMatch() {
        if (!btnUpdate.isEnabled()) {
            showError("Selecciona un partido y corrige los errores antes de actualizar.", "Validación Pendiente");
            return;
        }

        Timestamp matchDate = getFullTimestamp();

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
            showError("Error de formato numérico: ID o campos vacíos.", "Error");
        }
    }

    private void deleteMatch() {
        if (!btnDelete.isEnabled()) {
            showError("Selecciona un partido de la tabla para eliminar.", "Error de Selección");
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
            } catch (NumberFormatException e) {
                showError("Error de formato: ID de partido inválido.", "Error");
            }
        }
    }

    private void clearFieldsWithConfirmation() {
        // Mostrar el mensaje de corroboración solo si hay datos ingresados o seleccionados.
        boolean isSelected = (table != null && table.getSelectedRow() != -1);
        boolean hasInput = !txtMatchId.getText().isEmpty() || !txtPlayer1.getText().isEmpty() || !txtPlayer2.getText().isEmpty();

        if (isSelected || hasInput) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de limpiar todos los campos del formulario?",
                    "Confirmar limpieza",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                clearFields();
            }
        } else {
            // Si no hay nada que limpiar, simplemente limpiamos la selección por si acaso
            if (table != null) table.clearSelection();
            validateAllFields();
        }
    }

    private void clearFields() {
        txtMatchId.setText("");
        dateChooserMatch.setDate(null);
        txtTimeValue.setText("");

        // Seleccionamos el primer ítem, que ahora es la opción vacía
        if (cmbResult.getItemCount() > 0) cmbResult.setSelectedIndex(0);
        if (cmbMatchType.getItemCount() > 0) cmbMatchType.setSelectedIndex(0);
        if (cmbGameName.getItemCount() > 0) cmbGameName.setSelectedIndex(0);

        cmbAmPm.setSelectedItem("AM"); // Mantenemos un valor de AM/PM
        txtPlayer1.setText("");
        txtPlayer2.setText("");

        // Limpiamos errores
        lblErrorMatchId.setText(" ");
        lblErrorDate.setText(" ");
        lblErrorTime.setText(" ");
        lblErrorType.setText(" ");
        lblErrorPlayer1.setText(" ");
        lblErrorPlayer2.setText(" ");
        lblErrorGame.setText(" ");
        lblErrorResult.setText(" ");

        txtMatchId.setEditable(true);
        if (table != null) table.clearSelection();
        validateAllFields();
    }

    private Timestamp getFullTimestamp() {
        Date datePart = dateChooserMatch.getDate();
        String timeValueStr = txtTimeValue.getText().trim();
        String amPmStr = cmbAmPm.getSelectedItem() != null ? cmbAmPm.getSelectedItem().toString() : "AM";

        if (datePart == null || timeValueStr.isEmpty() || !timeValueStr.matches("\\d{2}:\\d{2}")) {
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
            showError("El jugador o el juego asociado no existen en la base de datos.", "Error de Clave Foránea");
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

        private Font getBoldFont(int size) {
            return MatchesPanel.this.getBoldFont(size);
        }
    }
}