package vistas;

import consultas.MatchCRUD;
import consultas.GameCRUD;
import tablas.Match;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
    private JTextField txtPlayerId1, txtPlayerId2;
    private JDateChooser dateChooserMatch;
    private JComboBox<String> cmbMatchType, cmbResult, cmbGameCode;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

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

        initComponents();
        loadMatches();
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
        gbc.weightx = 1.0;

        txtMatchId = createStyledTextField();
        dateChooserMatch = createStyledDateChooser();
        JPanel timeInputPanel = createStyledTimeInput();
        cmbMatchType = createStyledComboBox(new String[]{"Oficial", "Amistoso"});

        txtPlayerId1 = createStyledTextField();
        txtPlayerId2 = createStyledTextField();
        cmbGameCode = createGameCodeComboBox();
        cmbResult = createStyledComboBox(new String[]{"N/A", "Gana Jugador 1", "Gana Jugador 2", "Empate"});


        gbc.gridy = 0;

        gbc.gridx = 0; fieldsContainer.add(createFieldPanel("ID del Partido:", txtMatchId), gbc);
        gbc.gridx = 1; fieldsContainer.add(createFieldPanel("Fecha:", dateChooserMatch), gbc);
        gbc.gridx = 2; fieldsContainer.add(createFieldPanel("Hora (hh:mm):", timeInputPanel), gbc);
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(createFieldPanel("Tipo:", cmbMatchType), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        gbc.gridx = 0; fieldsContainer.add(createFieldPanel("ID Jugador 1:", txtPlayerId1), gbc);
        gbc.gridx = 1; fieldsContainer.add(createFieldPanel("ID Jugador 2:", txtPlayerId2), gbc);
        gbc.gridx = 2; fieldsContainer.add(createFieldPanel("Código Juego:", cmbGameCode), gbc);
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(createFieldPanel("Resultado:", cmbResult), gbc);

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
        btnClear.addActionListener(e -> clearFields());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        panel.add(fieldsContainer, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFieldPanel(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

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

        JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getDateEditor();
        dateEditor.setFont(getBodyFont(FONT_SIZE_BODY));
        dateEditor.setForeground(TEXT_PRIMARY);
        dateEditor.setBackground(BG_INPUT);
        dateEditor.setCaretColor(ACCENT_PRIMARY);
        dateEditor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));

        dateChooser.setBorder(new EmptyBorder(0, 0, 0, 0));
        dateChooser.getJCalendar().setBackground(BG_INPUT);
        dateChooser.getJCalendar().setForeground(TEXT_PRIMARY);

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

    private JComboBox<String> createGameCodeComboBox() {
        JComboBox<String> cmb = createStyledComboBox(new String[]{});
        try {
            List<String> gameCodes = gameCRUD.getAllGameCodes();
            for (String code : gameCodes) {
                cmb.addItem(code);
            }
        } catch (SQLException e) {
            showError("Error al cargar códigos de juego: " + e.getMessage(), "Error");
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
                        match.getPlayerId1(),
                        match.getPlayerId2(),
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
            txtPlayerId1.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtPlayerId2.setText(tableModel.getValueAt(selectedRow, 5).toString());
            cmbGameCode.setSelectedItem(tableModel.getValueAt(selectedRow, 6).toString());

            txtMatchId.setEditable(false);
        }
    }

    private void addMatch() {
        if (!validateFields()) return;

        Timestamp matchDate = getFullTimestamp();
        if (matchDate == null) {
            showError("Fecha u hora inválidas. Formato de hora requerido: hh:mm.", "Error");
            return;
        }

        try {
            Match match = new Match(
                    Integer.parseInt(txtMatchId.getText().trim()),
                    matchDate,
                    cmbResult.getSelectedItem().toString(),
                    cmbMatchType.getSelectedItem().toString(),
                    Integer.parseInt(txtPlayerId1.getText().trim()),
                    Integer.parseInt(txtPlayerId2.getText().trim()),
                    cmbGameCode.getSelectedItem().toString()
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
        }
    }

    private void updateMatch() {
        if (txtMatchId.getText().trim().isEmpty()) {
            showError("Selecciona un partido de la tabla para actualizar.", "Error");
            return;
        }
        if (!validateFields()) return;

        Timestamp matchDate = getFullTimestamp();
        if (matchDate == null) {
            showError("Fecha u hora inválidas. Formato de hora requerido: hh:mm.", "Error");
            return;
        }

        try {
            Match match = new Match(
                    Integer.parseInt(txtMatchId.getText().trim()),
                    matchDate,
                    cmbResult.getSelectedItem().toString(),
                    cmbMatchType.getSelectedItem().toString(),
                    Integer.parseInt(txtPlayerId1.getText().trim()),
                    Integer.parseInt(txtPlayerId2.getText().trim()),
                    cmbGameCode.getSelectedItem().toString()
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

    private void clearFields() {
        txtMatchId.setText("");
        dateChooserMatch.setDate(null);
        txtTimeValue.setText("12:00");
        cmbAmPm.setSelectedItem("AM");
        cmbResult.setSelectedIndex(0);
        cmbMatchType.setSelectedIndex(0);
        txtPlayerId1.setText("");
        txtPlayerId2.setText("");
        if (cmbGameCode.getItemCount() > 0) cmbGameCode.setSelectedIndex(0);
        txtMatchId.setEditable(true);
        table.clearSelection();
    }

    private boolean validateFields() {
        if (txtMatchId.getText().trim().isEmpty() ||
                txtPlayerId1.getText().trim().isEmpty() ||
                txtPlayerId2.getText().trim().isEmpty()) {
            showError("Todos los campos ID son obligatorios.", "Error");
            return false;
        }

        String timeStr = txtTimeValue.getText().trim();
        if (timeStr.isEmpty() || !timeStr.matches("\\d{2}:\\d{2}")) {
            showError("El formato de hora debe ser hh:mm (ej: 03:30).", "Error de Formato");
            return false;
        }

        try {
            Date timeTest = SDF_TIME_HHMM.parse(timeStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(timeTest);
            int hour = cal.get(Calendar.HOUR);
            if (hour < 1 || hour > 12) {
                showError("La hora debe estar entre 01 y 12.", "Error de Rango");
                return false;
            }
        } catch (ParseException e) {
            showError("La hora no es válida. Asegúrate de usar hh:mm (ej: 03:30).", "Error de Formato");
            return false;
        }


        try {
            int id1 = Integer.parseInt(txtPlayerId1.getText().trim());
            int id2 = Integer.parseInt(txtPlayerId2.getText().trim());
            if (id1 <= 0 || id2 <= 0) {
                showError("Los IDs deben ser números positivos.", "Error");
                return false;
            }
            if (id1 == id2) {
                showError("Los IDs de los jugadores deben ser diferentes.", "Error");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Los IDs deben ser números válidos.", "Error");
            return false;
        }

        return true;
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
            showError("IDs de jugador o código de juego no válidos.", "Error");
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