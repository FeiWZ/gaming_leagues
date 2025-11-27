package vistas;

import consultas.TeamPlayerCRUD;
import consultas.PlayerCRUD;
import consultas.TeamCRUD;
import tablas.TeamPlayer;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.toedter.calendar.JDateChooser;

public class TeamPlayerPanel extends JPanel {

    private Connection connection;
    private TeamPlayerCRUD teamPlayerCRUD;
    private PlayerCRUD playerCRUD;
    private TeamCRUD teamCRUD;

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtTeamName, txtPlayerName;
    private JDateChooser dateChooserDateFrom, dateChooserDateTo;
    private JTextField txtRole;

    private DocumentListener validationListener;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public TeamPlayerPanel(Connection connection) {
        this.connection = connection;
        this.teamPlayerCRUD = new TeamPlayerCRUD(connection);
        this.playerCRUD = new PlayerCRUD(connection);
        this.teamCRUD = new TeamCRUD(connection);
        SDF.setLenient(false);
        validationListener = createValidationListener();
        initComponents();
        loadTeamPlayers();
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

    private DocumentListener createValidationListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateFields(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateFields(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateFields(); }
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

        JPanel fieldsPanel = new JPanel(new GridLayout(1, 5, SPACING_MD, SPACING_LG));
        fieldsPanel.setBackground(BG_CARD);

        txtTeamName = createStyledTextField();
        txtPlayerName = createStyledTextField();
        txtRole = createStyledTextField();

        txtTeamName.getDocument().addDocumentListener(validationListener);
        txtPlayerName.getDocument().addDocumentListener(validationListener);
        txtRole.getDocument().addDocumentListener(validationListener);

        fieldsPanel.add(createFieldPanel("Equipo:", txtTeamName));
        fieldsPanel.add(createFieldPanel("Jugador:", txtPlayerName));
        fieldsPanel.add(createDateChooserPanel("Fecha Desde:", dateChooserDateFrom));
        fieldsPanel.add(createDateChooserPanel("Fecha Hasta (Opcional):", dateChooserDateTo));
        fieldsPanel.add(createFieldPanel("Rol:", txtRole));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Agregar", ACCENT_SUCCESS);
        btnUpdate = createStyledButton("Actualizar", ACCENT_WARNING);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addTeamPlayer());
        btnUpdate.addActionListener(e -> updateTeamPlayer());
        btnDelete.addActionListener(e -> deleteTeamPlayer());
        btnClear.addActionListener(e -> clearFields());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        validateFields();
        return panel;
    }

    private JPanel createFieldPanel(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDateChooserPanel(String labelText, JDateChooser dateChooser) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setFont(getBodyFont(FONT_SIZE_BODY));

        JTextField dateField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        dateField.setBackground(BG_INPUT);
        dateField.setForeground(TEXT_PRIMARY);
        dateField.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));

        if (labelText.contains("Desde")) {
            this.dateChooserDateFrom = dateChooser;
        } else {
            this.dateChooserDateTo = dateChooser;
        }

        dateChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                validateFields();
            }
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(dateChooser, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JLabel titleLabel = new JLabel("Relación Equipos - Jugadores");
        titleLabel.setFont(getTitleFont(FONT_SIZE_H2));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, SPACING_MD, 0));

        String[] columns = {"Equipo", "Jugador", "Desde", "Hasta", "Rol"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

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
                loadSelectedTeamPlayer();
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
        button.setForeground(BG_DARK_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(130, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void loadTeamPlayers() {
        tableModel.setRowCount(0);
        try {
            List<TeamPlayer> teamPlayers = teamPlayerCRUD.getAllTeamPlayer();
            for (TeamPlayer teamPlayer : teamPlayers) {
                Object[] row = {
                        teamPlayer.getTeamName(),
                        teamPlayer.getPlayerName(),
                        teamPlayer.getDateFrom() != null ? SDF.format(teamPlayer.getDateFrom()) : "",
                        teamPlayer.getDateTo() != null ? SDF.format(teamPlayer.getDateTo()) : "",
                        teamPlayer.getRole()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error al cargar relaciones: " + e.getMessage(), "Error de Carga");
        }
    }

    private void loadSelectedTeamPlayer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtTeamName.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtPlayerName.setText(tableModel.getValueAt(selectedRow, 1).toString());

            try {
                String dateFromStr = tableModel.getValueAt(selectedRow, 2).toString();
                if (!dateFromStr.isEmpty()) dateChooserDateFrom.setDate(SDF.parse(dateFromStr));
                else dateChooserDateFrom.setDate(null);

                String dateToStr = tableModel.getValueAt(selectedRow, 3).toString();
                if (!dateToStr.isEmpty()) dateChooserDateTo.setDate(SDF.parse(dateToStr));
                else dateChooserDateTo.setDate(null);
            } catch (Exception e) {
                dateChooserDateFrom.setDate(null);
                dateChooserDateTo.setDate(null);
            }

            String currentRole = tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString() : "";
            txtRole.setText(currentRole);

            txtTeamName.setEditable(false);
            txtPlayerName.setEditable(false);
            validateFields();
        }
    }

    private void addTeamPlayer() {
        Date dateFrom, dateTo;

        // ✅ CAMBIO: Validar aquí en lugar de deshabilitar el botón
        if (!validateFields()) {
            showError("Por favor completa todos los campos obligatorios: Equipo, Jugador, Fecha Desde y Rol.", "Campos Incompletos");
            return;
        }

        dateFrom = dateChooserDateFrom.getDate();
        dateTo = dateChooserDateTo.getDate();

        if (dateTo != null && dateFrom.after(dateTo)) {
            showError("La Fecha Desde no puede ser posterior a la Fecha Hasta.", "Error Lógico de Fecha");
            return;
        }

        if (!validateTeamAndPlayerExist()) {
            return;
        }

        try {
            String teamName = txtTeamName.getText().trim();
            String playerName = txtPlayerName.getText().trim();
            String role = txtRole.getText().trim();

            TeamPlayer teamPlayer = new TeamPlayer(
                    teamName,
                    playerName,
                    dateFrom,
                    dateTo,
                    role.isEmpty() ? null : role
            );

            if (teamPlayerCRUD.createTeamPlayer(teamPlayer)) {
                showSuccess("Relación agregada exitosamente!");
                clearFields();
                loadTeamPlayers();
            } else {
                showError("No se pudo agregar la relación.", "Error de Inserción");
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("duplicate key value") && errorMessage.contains("team_player_pkey")) {
                showError("Error: Ya existe esta relación (Equipo, Jugador). Esta combinación debe ser única.", "Error de Clave Compuesta");
            } else if (errorMessage.contains("violates foreign key constraint")) {
                if (errorMessage.contains("team_name")) {
                    showError("Error de Equipo: El nombre de equipo ingresado no existe en la tabla de Equipos.", "Error de Clave Foránea");
                } else if (errorMessage.contains("player_name")) {
                    showError("Error de Jugador: El nombre de jugador ingresado no existe en la tabla de Jugadores.", "Error de Clave Foránea");
                } else {
                    showError("Error de Clave Foránea: Asegúrate de que el nombre de equipo y el nombre de jugador existan.", "Error de Clave Foránea");
                }
            } else {
                showError("Error al agregar: " + errorMessage, "Error SQL");
            }
        }
    }

    private void updateTeamPlayer() {
        if (txtTeamName.isEditable()) {
            showError("Selecciona una relación de la tabla para actualizar.", "Error de Selección");
            return;
        }

        // ✅ CAMBIO: Validar aquí en lugar de deshabilitar el botón
        if (!validateFields()) {
            showError("Por favor completa todos los campos obligatorios: Equipo, Jugador, Fecha Desde y Rol.", "Campos Incompletos");
            return;
        }

        Date dateFrom = dateChooserDateFrom.getDate();
        Date dateTo = dateChooserDateTo.getDate();

        if (dateTo != null && dateFrom.after(dateTo)) {
            showError("La Fecha Desde no puede ser posterior a la Fecha Hasta.", "Error Lógico de Fecha");
            return;
        }

        if (!validateTeamAndPlayerExist()) {
            return;
        }

        try {
            String teamName = txtTeamName.getText().trim();
            String playerName = txtPlayerName.getText().trim();
            String role = txtRole.getText().trim();

            TeamPlayer teamPlayer = new TeamPlayer(
                    teamName,
                    playerName,
                    dateFrom,
                    dateTo,
                    role.isEmpty() ? null : role
            );

            if (teamPlayerCRUD.updateTeamPlayer(teamPlayer)) {
                showSuccess("Relación actualizada exitosamente!");
                clearFields();
                loadTeamPlayers();
            } else {
                showError("No se pudo actualizar la relación.", "Error de Actualización");
            }
        } catch (SQLException e) {
            showError("Error al actualizar: " + e.getMessage(), "Error SQL");
        }
    }

    private void deleteTeamPlayer() {
        if (txtTeamName.getText().trim().isEmpty()) {
            showError("Selecciona una relación de la tabla para eliminar.", "Error");
            return;
        }

        String teamName = txtTeamName.getText().trim();
        String playerName = txtPlayerName.getText().trim();

        if (teamName.isEmpty() || playerName.isEmpty()) {
            showError("Los nombres deben ser válidos.", "Error de Formato");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar esta relación?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (teamPlayerCRUD.deleteTeamPlayer(teamName, playerName)) {
                    showSuccess("Relación eliminada exitosamente!");
                    clearFields();
                    loadTeamPlayers();
                } else {
                    showError("No se pudo eliminar la relación.", "Error de Eliminación");
                }
            } catch (SQLException e) {
                showError("Error al eliminar: " + e.getMessage(), "Error SQL");
            }
        }
    }

    private void clearFields() {
        txtTeamName.setText("");
        txtPlayerName.setText("");
        dateChooserDateFrom.setDate(null);
        dateChooserDateTo.setDate(null);
        txtRole.setText("");

        txtTeamName.setEditable(true);
        txtPlayerName.setEditable(true);
        table.clearSelection();
        validateFields();
    }

    private boolean validateFields() {
        String teamNameText = txtTeamName.getText().trim();
        String playerNameText = txtPlayerName.getText().trim();
        String role = txtRole.getText().trim();

        boolean isValid = true;

        if (teamNameText.isEmpty() || playerNameText.isEmpty() || role.isEmpty()) {
            isValid = false;
        }

        if (dateChooserDateFrom == null || dateChooserDateFrom.getDate() == null) {
            isValid = false;
        }

        boolean isSelected = !txtTeamName.isEditable();

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);

        return isValid;
    }

    private boolean validateTeamAndPlayerExist() {
        String teamName = txtTeamName.getText().trim();
        String playerName = txtPlayerName.getText().trim();

        try {
            if (!teamCRUD.teamExists(teamName)) {
                showError("El equipo '" + teamName + "' no existe en la base de datos.", "Equipo No Encontrado");
                txtTeamName.requestFocus();
                return false;
            }

            if (!playerCRUD.playerExists(playerName)) {
                showError("El jugador '" + playerName + "' no existe en la base de datos.", "Jugador No Encontrado");
                txtPlayerName.requestFocus();
                return false;
            }
        } catch (SQLException e) {
            showError("Error al verificar en la base de datos: " + e.getMessage(), "Error de BD");
            return false;
        }

        return true;
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

    // Métodos de utilidad para fonts y colores
    private Font getTitleFont(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    private Font getBoldFont(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    private Font getBodyFont(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    private Color darken(Color color, float factor) {
        return new Color(
                Math.max((int)(color.getRed() * factor), 0),
                Math.max((int)(color.getGreen() * factor), 0),
                Math.max((int)(color.getBlue() * factor), 0)
        );
    }

    private Color brighten(Color color, float factor) {
        return new Color(
                Math.min((int)(color.getRed() * factor), 255),
                Math.min((int)(color.getGreen() * factor), 255),
                Math.min((int)(color.getBlue() * factor), 255)
        );
    }
}