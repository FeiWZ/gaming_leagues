package vistas;

import consultas.TeamCRUD;
import consultas.PlayerCRUD;
import tablas.Team;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;

import com.toedter.calendar.JDateChooser;

public class TeamsPanel extends JPanel {

    private Connection connection;
    private TeamCRUD teamCRUD;
    private PlayerCRUD playerCRUD;

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtTeamId, txtTeamName, txtIdCoach, txtCreatedByPlayer, txtPlayerName, txtRole;
    private JDateChooser dateChooserCreated, dateChooserDisbanded, dateChooserDateFrom, dateChooserDateTo;
    private JLabel lblErrorTeamId, lblErrorTeamName, lblErrorIdCoach, lblErrorCreatedByPlayer, lblErrorPlayerName, lblErrorRole, lblErrorDateCreated, lblErrorDateFrom;

    private CaretListener validationListener;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public TeamsPanel(Connection connection) {
        this.connection = connection;
        this.teamCRUD = new TeamCRUD(connection);
        this.playerCRUD = new PlayerCRUD(connection);
        SDF.setLenient(false);
        validationListener = e -> validateFields();

        // Inicializar etiquetas de error
        initErrorLabels();

        initComponents();
        loadTeams();
        validateFields();
    }

    private void initErrorLabels() {
        lblErrorTeamId = createErrorLabel();
        lblErrorTeamName = createErrorLabel();
        lblErrorIdCoach = createErrorLabel();
        lblErrorCreatedByPlayer = createErrorLabel();
        lblErrorPlayerName = createErrorLabel();
        lblErrorRole = createErrorLabel();
        lblErrorDateCreated = createErrorLabel();
        lblErrorDateFrom = createErrorLabel();
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ACCENT_DANGER); // Color rojo
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        return label;
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

        // --- PRIMERA FILA (3 columnas) ---
        JPanel fieldsPanel1 = new JPanel(new GridLayout(1, 3, SPACING_MD, SPACING_LG));
        fieldsPanel1.setBackground(BG_CARD);

        txtTeamId = createStyledTextField();
        txtTeamName = createStyledTextField();

        // ID del Equipo
        fieldsPanel1.add(createFieldPanel("ID del Equipo:", txtTeamId, lblErrorTeamId));

        // Nombre del Equipo
        fieldsPanel1.add(createFieldPanel("Nombre del Equipo:", txtTeamName, lblErrorTeamName));

        // Fecha Creación (obligatoria)
        dateChooserCreated = createSimpleDateChooser();
        JPanel dateCreatedPanel = createDateFieldPanel("Fecha Creación:", dateChooserCreated, lblErrorDateCreated);
        fieldsPanel1.add(dateCreatedPanel);

        // --- SEGUNDA FILA (3 columnas) ---
        JPanel fieldsPanel2 = new JPanel(new GridLayout(1, 3, SPACING_MD, SPACING_LG));
        fieldsPanel2.setBackground(BG_CARD);
        fieldsPanel2.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        // Fecha Disolución (opcional)
        dateChooserDisbanded = createSimpleDateChooser();
        JPanel dateDisbandedPanel = createDateFieldPanel("Fecha Disolución (Opcional):", dateChooserDisbanded, null);
        fieldsPanel2.add(dateDisbandedPanel);

        // ID del Entrenador
        txtIdCoach = createStyledTextField();
        fieldsPanel2.add(createFieldPanel("ID del Entrenador:", txtIdCoach, lblErrorIdCoach));

        // Creado por Jugador
        txtCreatedByPlayer = createStyledTextField();
        fieldsPanel2.add(createFieldPanel("Creado por Jugador:", txtCreatedByPlayer, lblErrorCreatedByPlayer));

        // --- TERCERA FILA (4 columnas) ---
        JPanel fieldsPanel3 = new JPanel(new GridLayout(1, 4, SPACING_MD, SPACING_LG));
        fieldsPanel3.setBackground(BG_CARD);

        txtPlayerName = createStyledTextField();
        txtRole = createStyledTextField();

        // Crear los dateChooser para la tercera fila
        dateChooserDateFrom = createSimpleDateChooser();
        dateChooserDateTo = createSimpleDateChooser();

        // Nombre del Jugador
        fieldsPanel3.add(createFieldPanel("Nombre del Jugador:", txtPlayerName, lblErrorPlayerName));

        // Fecha Desde (obligatoria)
        JPanel dateFromPanel = createDateFieldPanel("Fecha Desde:", dateChooserDateFrom, lblErrorDateFrom);
        fieldsPanel3.add(dateFromPanel);

        // Fecha Hasta (opcional)
        JPanel dateToPanel = createDateFieldPanel("Fecha Hasta (Opcional):", dateChooserDateTo, null);
        fieldsPanel3.add(dateToPanel);

        // Rol
        fieldsPanel3.add(createFieldPanel("Rol:", txtRole, lblErrorRole));

        // --- AGREGAR LISTENERS DE VALIDACIÓN ---
        txtTeamId.addCaretListener(validationListener);
        txtTeamName.addCaretListener(validationListener);
        txtIdCoach.addCaretListener(validationListener);
        txtCreatedByPlayer.addCaretListener(validationListener);
        txtPlayerName.addCaretListener(validationListener);
        txtRole.addCaretListener(validationListener);

        // Listeners para fechas
        dateChooserCreated.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) validateFields();
        });
        dateChooserDisbanded.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) validateFields();
        });
        dateChooserDateFrom.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) validateFields();
        });
        dateChooserDateTo.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) validateFields();
        });

        // --- PANEL PRINCIPAL QUE CONTIENE LAS 3 FILAS ---
        JPanel allFieldsPanel = new JPanel(new BorderLayout());
        allFieldsPanel.setBackground(BG_CARD);
        allFieldsPanel.add(fieldsPanel1, BorderLayout.NORTH);
        allFieldsPanel.add(fieldsPanel2, BorderLayout.CENTER);
        allFieldsPanel.add(fieldsPanel3, BorderLayout.SOUTH);

        // --- PANEL DE BOTONES ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Agregar", ACCENT_SUCCESS);
        btnUpdate = createStyledButton("Actualizar", ACCENT_WARNING);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addTeam());
        btnUpdate.addActionListener(e -> updateTeam());
        btnDelete.addActionListener(e -> deleteTeam());
        btnClear.addActionListener(e -> clearFields());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        // --- ENSAMBLAR EL PANEL COMPLETO ---
        panel.add(allFieldsPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Método para crear solo el JDateChooser (sin label ni panel)
    private JDateChooser createSimpleDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setFont(getBodyFont(FONT_SIZE_BODY));
        dateChooser.setPreferredSize(new Dimension(150, 35));
        dateChooser.setMinimumSize(new Dimension(150, 35));

        JTextField dateField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        dateField.setPreferredSize(new Dimension(150, 35));
        dateField.setMinimumSize(new Dimension(150, 35));
        dateField.setBackground(BG_INPUT);
        dateField.setForeground(Color.WHITE);
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));
        dateField.setCaretColor(ACCENT_PRIMARY);

        // Forzar color blanco
        dateChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(() -> dateField.setForeground(Color.WHITE));
            }
        });

        dateField.addPropertyChangeListener("foreground", evt -> {
            SwingUtilities.invokeLater(() -> dateField.setForeground(Color.WHITE));
        });

        return dateChooser;
    }

    // Método para crear un panel completo con label + dateChooser + error label
    private JPanel createDateFieldPanel(String labelText, JDateChooser dateChooser, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        // Label (ej: "Fecha Creación:")
        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        // Panel interno para el dateChooser y error
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(dateChooser, BorderLayout.NORTH);

        // Agregar error label si se proporciona
        if (errorLabel != null) {
            contentPanel.add(errorLabel, BorderLayout.SOUTH);
        }

        // Ensamblar
        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        // Tamaño consistente
        panel.setPreferredSize(new Dimension(180, 70));
        panel.setMinimumSize(new Dimension(180, 70));

        return panel;
    }

    private JPanel createFieldPanel(String labelText, JTextField textField, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(textField, BorderLayout.NORTH);

        if (errorLabel != null) {
            contentPanel.add(errorLabel, BorderLayout.SOUTH);
        }

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JLabel titleLabel = new JLabel("Lista de Equipos");
        titleLabel.setFont(getTitleFont(FONT_SIZE_H2));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, SPACING_MD, 0));

        String[] columns = {"ID", "Nombre", "Fecha Creación", "Fecha Disolución", "ID Entrenador", "Creado Por", "Jugador", "Desde", "Hasta", "Rol"};
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
                loadSelectedTeam();
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

    private void loadTeams() {
        tableModel.setRowCount(0);
        try {
            List<Team> teams = teamCRUD.getAllTeams();
            for (Team team : teams) {
                Object[] row = {
                        team.getTeamId(),
                        team.getTeamName(),
                        team.getDateCreated() != null ? SDF.format(team.getDateCreated()) : "",
                        team.getDateDisbanded() != null ? SDF.format(team.getDateDisbanded()) : "",
                        team.getIdCoach(),
                        team.getCreatedByPlayer(),
                        team.getPlayerName(),
                        team.getDateFrom() != null ? SDF.format(team.getDateFrom()) : "",
                        team.getDateTo() != null ? SDF.format(team.getDateTo()) : "",
                        team.getRole()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error al cargar los equipos: " + e.getMessage(), "Error de Carga");
        }
    }

    private void loadSelectedTeam() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            clearErrorMessages();
            txtTeamId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTeamName.setText(tableModel.getValueAt(selectedRow, 1).toString());

            try {
                String dateCreatedStr = tableModel.getValueAt(selectedRow, 2).toString();
                if (!dateCreatedStr.isEmpty()) dateChooserCreated.setDate(SDF.parse(dateCreatedStr));
                else dateChooserCreated.setDate(null);

                String dateDisbandedStr = tableModel.getValueAt(selectedRow, 3).toString();
                if (!dateDisbandedStr.isEmpty()) dateChooserDisbanded.setDate(SDF.parse(dateDisbandedStr));
                else dateChooserDisbanded.setDate(null);
            } catch (Exception e) {
                dateChooserCreated.setDate(null);
                dateChooserDisbanded.setDate(null);
            }

            txtIdCoach.setText(tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString() : "");
            txtCreatedByPlayer.setText(tableModel.getValueAt(selectedRow, 5) != null ? tableModel.getValueAt(selectedRow, 5).toString() : "");
            txtPlayerName.setText(tableModel.getValueAt(selectedRow, 6) != null ? tableModel.getValueAt(selectedRow, 6).toString() : "");

            try {
                String dateFromStr = tableModel.getValueAt(selectedRow, 7).toString();
                if (!dateFromStr.isEmpty()) dateChooserCreated.setDate(SDF.parse(dateFromStr));
                else dateChooserDateFrom.setDate(null);

                String dateToStr = tableModel.getValueAt(selectedRow, 8).toString();
                if (!dateToStr.isEmpty()) dateChooserDisbanded.setDate(SDF.parse(dateToStr));
                else dateChooserDateTo.setDate(null);
            } catch (Exception e) {
                dateChooserDateFrom.setDate(null);
                dateChooserDateTo.setDate(null);
            }

            txtRole.setText(tableModel.getValueAt(selectedRow,9) != null ? tableModel.getValueAt(selectedRow,9).toString() : "");

            txtTeamId.setEditable(false);
            validateFields();
        }
    }

    private void clearErrorMessages() {
        lblErrorTeamId.setText(" ");
        lblErrorTeamName.setText(" ");
        lblErrorIdCoach.setText(" ");
        lblErrorCreatedByPlayer.setText(" ");
        lblErrorPlayerName.setText(" ");
        lblErrorRole.setText(" ");
        lblErrorDateCreated.setText(" ");
        lblErrorDateFrom.setText(" ");
    }

    private void addTeam() {
        if (!validateFieldsForAction(false)) {
            return;
        }

        if (!validateDateLogic()) return;
        if (!validatePlayerExists()) return;

        try {
            Date dateCreated = dateChooserCreated.getDate();
            Date dateDisbanded = dateChooserDisbanded.getDate();
            Date dateFrom = dateChooserDateFrom.getDate();
            Date dateTo = dateChooserDateTo.getDate();

            int teamId = Integer.parseInt(txtTeamId.getText().trim());
            int idCoach = Integer.parseInt(txtIdCoach.getText().trim());
            String createdByPlayer = txtCreatedByPlayer.getText().trim();
            String playerName = txtPlayerName.getText().trim();
            String role = txtRole.getText().trim();

            Team team = new Team(
                    teamId,
                    txtTeamName.getText().trim(),
                    dateCreated,
                    dateDisbanded,
                    idCoach,
                    createdByPlayer,
                    playerName,
                    dateFrom,
                    dateTo,
                    role.isEmpty() ? null : role

            );

            if (teamCRUD.createTeam(team)) {
                showSuccess("¡Equipo agregado exitosamente!");
                clearFields();
                loadTeams();
            } else {
                showError("No se pudo agregar el equipo.", "Error de Inserción");
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage();

            if (errorMessage.contains("duplicate key value") && errorMessage.contains("team_pkey")) {
                showError("Error: Ya existe un equipo con el ID '" + txtTeamId.getText().trim() + "'. El ID debe ser único.", "Error de Clave Primaria");
            } else if (errorMessage.contains("violates foreign key constraint")) {
                if (errorMessage.contains("created_by_player_fk")) {
                    showError("Error de Jugador Creador: El jugador '" + txtCreatedByPlayer.getText().trim() + "' no existe en la tabla de Jugadores.", "Error de Clave Foránea");
                } else if (errorMessage.contains("player_name")) {
                    showError("Error de Jugador: El nombre de jugador ingresado no existe en la tabla de Jugadores.", "Error de Clave Foránea");
                } else {
                    showError("Error de Clave Foránea: Asegúrate de que los IDs de Entrenador y Jugador Creador existan.", "Error de Clave Foránea");
                }
            } else {
                showError("Error al agregar: " + errorMessage, "Error SQL");
            }
        } catch (NumberFormatException e) {
            showError("Los IDs deben ser números enteros positivos.", "Error de Formato");
        }
    }

    private void updateTeam() {
        if (txtTeamId.isEditable()) {
            showError("Selecciona un equipo de la tabla para actualizar.", "Error de Selección");
            return;
        }

        if (!validateFieldsForAction(true)) {
            return;
        }

        if (!validateDateLogic()) return;
        if (!validatePlayerExists()) return;

        try {
            Date dateCreated = dateChooserCreated.getDate();
            Date dateDisbanded = dateChooserDisbanded.getDate();
            Date dateFrom = dateChooserDateFrom.getDate();
            Date dateTo = dateChooserDateTo.getDate();

            int teamId = Integer.parseInt(txtTeamId.getText().trim());
            int idCoach = Integer.parseInt(txtIdCoach.getText().trim());
            String createdByPlayer = txtCreatedByPlayer.getText().trim();
            String playerName = txtPlayerName.getText().trim();
            String role = txtRole.getText().trim();

            Team team = new Team(
                    teamId,
                    txtTeamName.getText().trim(),
                    dateCreated,
                    dateDisbanded,
                    idCoach,
                    createdByPlayer,
                    playerName,
                    dateFrom,
                    dateTo,
                    role.isEmpty() ? null : role
            );

            if (teamCRUD.updateTeam(team)) {
                showSuccess("¡Equipo actualizado exitosamente!");
                clearFields();
                loadTeams();
            } else {
                showError("No se pudo actualizar el equipo.", "Error de Actualización");
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("violates foreign key constraint")) {
                if (errorMessage.contains("created_by_player_fk")) {
                    showError("Error de Jugador Creador: El jugador '" + txtCreatedByPlayer.getText().trim() + "' no existe.", "Error de Clave Foránea");
                } else if (errorMessage.contains("player_name")) {
                    showError("Error de Jugador: El nombre de jugador ingresado no existe en la tabla de Jugadores.", "Error de Clave Foránea");
                } else {
                    showError("Error de Clave Foránea: Asegúrate de que los IDs de Entrenador y Jugador Creador existan.", "Error de Clave Foránea");
                }
            } else {
                showError("Error al actualizar: " + errorMessage, "Error SQL");
            }
        } catch (NumberFormatException e) {
            showError("Los IDs deben ser números enteros positivos.", "Error de Formato");
        }
    }

    private void deleteTeam() {
        if (txtTeamId.getText().trim().isEmpty()) {
            showError("Selecciona un equipo de la tabla para eliminar.", "Error");
            return;
        }

        int teamId;
        try {
            teamId = Integer.parseInt(txtTeamId.getText().trim());
        } catch (NumberFormatException e) {
            showError("El ID del Equipo debe ser un número válido.", "Error de Formato");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar este equipo? Si el equipo tiene dependencias (jugadores, torneos, etc.) la operación fallará.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (teamCRUD.deleteTeam(teamId)) {
                    showSuccess("¡Equipo eliminado exitosamente!");
                    clearFields();
                    loadTeams();
                } else {
                    showError("No se pudo eliminar el equipo.", "Error de Eliminación");
                }
            } catch (SQLException e) {
                String errorMessage = e.getMessage();
                if (errorMessage.contains("violates foreign key constraint")) {
                    showError("Error: No puedes eliminar este equipo porque tiene dependencias (jugadores, torneos, etc.) asociadas.", "Error de Clave Foránea");
                } else {
                    showError("Error al eliminar: " + errorMessage, "Error SQL");
                }
            }
        }
    }

    private void clearFields() {
        txtTeamId.setText("");
        txtTeamName.setText("");
        dateChooserCreated.setDate(null);
        dateChooserDisbanded.setDate(null);
        txtIdCoach.setText("");
        txtCreatedByPlayer.setText("");
        txtPlayerName.setText("");
        dateChooserDateFrom.setDate(null);
        dateChooserDateTo.setDate(null);
        txtRole.setText("");
        txtTeamId.setEditable(true);
        table.clearSelection();

        // Limpiar mensajes de error
        lblErrorTeamId.setText(" ");
        lblErrorTeamName.setText(" ");
        lblErrorIdCoach.setText(" ");
        lblErrorCreatedByPlayer.setText(" ");
        lblErrorPlayerName.setText(" ");
        lblErrorRole.setText(" ");
        lblErrorDateCreated.setText(" ");
        lblErrorDateFrom.setText(" ");

        validateFields();
    }

    private boolean validateFieldsForAction(boolean isUpdate) {
        String teamIdText = txtTeamId.getText().trim();
        String teamNameText = txtTeamName.getText().trim();
        String idCoachText = txtIdCoach.getText().trim();
        String createdByPlayerText = txtCreatedByPlayer.getText().trim();
        String playerNameText = txtPlayerName.getText().trim();
        String role = txtRole.getText().trim();

        if (teamIdText.isEmpty() || teamNameText.isEmpty() || idCoachText.isEmpty() || createdByPlayerText.isEmpty() || playerNameText.isEmpty() || role.isEmpty()) {
            String title = isUpdate ? "No se puede actualizar" : "No se puede agregar";
            showError("Por favor completa todos los campos obligatorios antes de " + (isUpdate ? "actualizar" : "agregar") + ".", title);
            return false;
        }

        try {
            int teamId = Integer.parseInt(teamIdText);
            int idCoach = Integer.parseInt(idCoachText);

            if (teamId <= 0 || idCoach <= 0) {
                showError("Los IDs deben ser números positivos.", "Error de Formato");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Los campos de ID deben contener números válidos.", "Error de Formato");
            return false;
        }

        if (dateChooserCreated.getDate() == null) {
            showError("La fecha de creación es obligatoria.", "Campo Faltante");
            return false;
        }

        Date dateCreated = dateChooserCreated.getDate();
        Date dateDisbanded = dateChooserDisbanded.getDate();
        if (dateCreated != null && dateDisbanded != null && dateCreated.after(dateDisbanded)) {
            showError("La fecha de creación no puede ser posterior a la fecha de disolución.", "Error de Fechas");
            return false;
        }

        return true;
    }

    private void validateFields() {
        boolean isValid = true;

        // Validar ID del Equipo
        String teamIdText = txtTeamId.getText().trim();
        if (teamIdText.isEmpty()) {
            lblErrorTeamId.setText("ID del equipo es obligatorio");
            isValid = false;
        } else {
            try {
                int teamId = Integer.parseInt(teamIdText);
                if (teamId <= 0) {
                    lblErrorTeamId.setText("ID debe ser positivo");
                    isValid = false;
                } else {
                    lblErrorTeamId.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorTeamId.setText("ID debe ser un número");
                isValid = false;
            }
        }

        // Validar Nombre del Equipo
        String teamNameText = txtTeamName.getText().trim();
        if (teamNameText.isEmpty()) {
            lblErrorTeamName.setText("Nombre del equipo es obligatorio");
            isValid = false;
        } else if (teamNameText.length() < 3) {
            lblErrorTeamName.setText("Mínimo 3 caracteres");
            isValid = false;
        } else {
            lblErrorTeamName.setText(" ");
        }

        // Validar ID del Entrenador
        String idCoachText = txtIdCoach.getText().trim();
        if (idCoachText.isEmpty()) {
            lblErrorIdCoach.setText("ID del entrenador es obligatorio");
            isValid = false;
        } else {
            try {
                int idCoach = Integer.parseInt(idCoachText);
                if (idCoach <= 0) {
                    lblErrorIdCoach.setText("ID debe ser positivo");
                    isValid = false;
                } else {
                    lblErrorIdCoach.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorIdCoach.setText("ID debe ser un número");
                isValid = false;
            }
        }

        // Validar Creado por Jugador
        String createdByPlayerText = txtCreatedByPlayer.getText().trim();
        if (createdByPlayerText.isEmpty()) {
            lblErrorCreatedByPlayer.setText("Jugador creador es obligatorio");
            isValid = false;
        } else {
            lblErrorCreatedByPlayer.setText(" ");
        }

        // Validar Nombre del Jugador
        String playerNameText = txtPlayerName.getText().trim();
        if (playerNameText.isEmpty()) {
            lblErrorPlayerName.setText("Nombre del jugador es obligatorio");
            isValid = false;
        } else {
            lblErrorPlayerName.setText(" ");
        }

        // Validar Rol
        String roleText = txtRole.getText().trim();
        if (roleText.isEmpty()) {
            lblErrorRole.setText("Rol es obligatorio");
            isValid = false;
        } else {
            lblErrorRole.setText(" ");
        }

        // Validar Fecha Creación
        if (dateChooserCreated.getDate() == null) {
            lblErrorDateCreated.setText("Fecha de creación es obligatoria");
            isValid = false;
        } else {
            lblErrorDateCreated.setText(" ");
        }

        // Validar Fecha Desde
        if (dateChooserDateFrom.getDate() == null) {
            lblErrorDateFrom.setText("Fecha desde es obligatoria");
            isValid = false;
        } else {
            lblErrorDateFrom.setText(" ");
        }

        // Validar lógica de fechas
        Date dateCreated = dateChooserCreated.getDate();
        Date dateDisbanded = dateChooserDisbanded.getDate();
        Date dateFrom = dateChooserDateFrom.getDate();
        Date dateTo = dateChooserDateTo.getDate();

        if (dateCreated != null && dateDisbanded != null && dateCreated.after(dateDisbanded)) {
            lblErrorDateCreated.setText("No puede ser posterior a disolución");
            isValid = false;
        }

        if (dateFrom != null && dateTo != null && dateFrom.after(dateTo)) {
            // Puedes agregar un label de error para DateTo si lo necesitas
            showError("Fecha desde no puede ser posterior a fecha hasta", "Error de Fechas");
            isValid = false;
        }

        boolean isSelected = !txtTeamId.isEditable();

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    private boolean validateDateLogic() {
        Date dateCreated = dateChooserCreated.getDate();
        Date dateDisbanded = dateChooserDisbanded.getDate();

        if (dateCreated != null && dateDisbanded != null) {
            if (dateCreated.after(dateDisbanded)) {
                showError("La Fecha de Creación no puede ser posterior a la Fecha de Disolución.", "Error Lógico de Fecha");
                return false;
            }
        }

        if (dateChooserDateFrom == null || dateChooserDateFrom.getDate() == null) {
            return false;
        }
        return true;
    }

    private boolean validatePlayerExists() {
        String creatorPlayer = txtCreatedByPlayer.getText().trim();
        String memberPlayer = txtPlayerName.getText().trim(); // Falta validar este

        if (!creatorPlayer.isEmpty()) {
            try {
                if (!playerCRUD.playerExists(creatorPlayer)) {
                    showError("El jugador creador '" + creatorPlayer + "' no existe.", "Jugador No Encontrado");
                    return false;
                }
            } catch (SQLException e) {
                showError("Error al verificar el jugador creador: " + e.getMessage(), "Error de BD");
                return false;
            }
        }

        if (!memberPlayer.isEmpty()) {
            try {
                if (!playerCRUD.playerExists(memberPlayer)) {
                    showError("El jugador miembro '" + memberPlayer + "' no existe.", "Jugador No Encontrado");
                    return false;
                }
            } catch (SQLException e) {
                showError("Error al verificar el jugador miembro: " + e.getMessage(), "Error de BD");
                return false;
            }
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