package vistas;

import consultas.TeamCRUD;
import consultas.PlayerCRUD;
import tablas.Team;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.text.ParseException; // Necesario para parsear fechas al cargar
import com.toedter.calendar.JDateChooser;

public class TeamsPanel extends JPanel {

    private Connection connection;
    private TeamCRUD teamCRUD;
    private PlayerCRUD playerCRUD;

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtTeamId, txtTeamName, txtIdCoach, txtCreatedByPlayer;
    private JDateChooser dateChooserCreated, dateChooserDisbanded;

    // Declaración de los JLabels de error
    private JLabel lblErrorTeamId, lblErrorTeamName, lblErrorIdCoach, lblErrorCreatedByPlayer;
    private JLabel lblErrorDateCreated, lblErrorDateDisbanded;

    private DocumentListener validationDocumentListener;
    private PropertyChangeListener validationDateListener;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public TeamsPanel(Connection connection) {
        this.connection = connection;
        this.teamCRUD = new TeamCRUD(connection);
        this.playerCRUD = new PlayerCRUD(connection);
        SDF.setLenient(false);
        setupValidationListeners();
        initComponents();
        loadTeams();
        validateFields();
    }

    // 2. Método para crear los JLabels de error
    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ACCENT_DANGER);
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

    // Nuevo método para configurar los listeners
    private void setupValidationListeners() {
        validationDocumentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateFields(); }
            public void removeUpdate(DocumentEvent e) { validateFields(); }
            public void insertUpdate(DocumentEvent e) { validateFields(); }
        };

        validationDateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("date".equals(evt.getPropertyName())) validateFields();
            }
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

        // Inicializar JLabels de error
        lblErrorTeamId = createErrorLabel();
        lblErrorTeamName = createErrorLabel();
        lblErrorIdCoach = createErrorLabel();
        lblErrorCreatedByPlayer = createErrorLabel();
        lblErrorDateCreated = createErrorLabel();
        lblErrorDateDisbanded = createErrorLabel();

        JPanel fieldsPanel1 = new JPanel(new GridLayout(2, 3, SPACING_MD, 0));
        fieldsPanel1.setBackground(BG_CARD);

        txtTeamId = createStyledTextField();
        txtTeamName = createStyledTextField();

        fieldsPanel1.add(createFieldPanel("ID del Equipo:", txtTeamId, lblErrorTeamId));
        fieldsPanel1.add(createFieldPanel("Nombre del Equipo:", txtTeamName, lblErrorTeamName));

        dateChooserCreated = createDateChooser();
        fieldsPanel1.add(createDateChooserPanel("Fecha Creación:", dateChooserCreated, lblErrorDateCreated));

        JPanel fieldsPanel2 = new JPanel(new GridLayout(2, 3, SPACING_MD, 0));
        fieldsPanel2.setBackground(BG_CARD);
        fieldsPanel2.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        dateChooserDisbanded = createDateChooser();
        txtIdCoach = createStyledTextField();
        txtCreatedByPlayer = createStyledTextField();

        fieldsPanel2.add(createDateChooserPanel("Fecha Disolución (Opcional):", dateChooserDisbanded, lblErrorDateDisbanded));
        fieldsPanel2.add(createFieldPanel("ID del Entrenador:", txtIdCoach, lblErrorIdCoach));
        fieldsPanel2.add(createFieldPanel("Creado por Jugador:", txtCreatedByPlayer, lblErrorCreatedByPlayer));

        // Registrar los DocumentListeners
        txtTeamId.getDocument().addDocumentListener(validationDocumentListener);
        txtTeamName.getDocument().addDocumentListener(validationDocumentListener);
        txtIdCoach.getDocument().addDocumentListener(validationDocumentListener);
        txtCreatedByPlayer.getDocument().addDocumentListener(validationDocumentListener);

        // Registrar los PropertyChangeListeners
        dateChooserCreated.getDateEditor().addPropertyChangeListener(validationDateListener);
        dateChooserDisbanded.getDateEditor().addPropertyChangeListener(validationDateListener);

        JPanel allFieldsPanel = new JPanel(new BorderLayout());
        allFieldsPanel.setBackground(BG_CARD);
        allFieldsPanel.add(fieldsPanel1, BorderLayout.NORTH);
        allFieldsPanel.add(fieldsPanel2, BorderLayout.CENTER);

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

        panel.add(allFieldsPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Método para crear el panel del campo de texto con su error
    private JPanel createFieldPanel(String labelText, JTextField textField, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBackground(BG_CARD);
        inputContainer.add(textField, BorderLayout.CENTER);
        inputContainer.add(errorLabel, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(inputContainer, BorderLayout.CENTER);

        return panel;
    }

    // Método auxiliar para crear solo el JDateChooser base
    private JDateChooser createDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setFont(getBodyFont(FONT_SIZE_BODY));

        JTextField dateField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        dateField.setBackground(BG_INPUT);
        dateField.setForeground(TEXT_PRIMARY);
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));
        return dateChooser;
    }

    // Método para crear el panel del DateChooser con su JLabel de error
    private JPanel createDateChooserPanel(String labelText, JDateChooser dateChooser, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBackground(BG_CARD);
        inputContainer.add(dateChooser, BorderLayout.CENTER);
        inputContainer.add(errorLabel, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(inputContainer, BorderLayout.CENTER);

        return panel;
    }

    /**
     * CONSTRUCCIÓN DE LA TABLA
     */
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

        String[] columns = {"ID", "Nombre", "Fecha Creación", "Fecha Disolución", "ID Entrenador", "Creado Por"};
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

    /**
     * CARGA DE DATOS DE LA BD
     */
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
                        team.getCreatedByPlayer()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error al cargar los equipos: " + e.getMessage(), "Error de Carga");
        }
    }

    /**
     * CARGA DE DATOS EN FORMULARIO AL SELECCIONAR
     */
    private void loadSelectedTeam() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtTeamId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTeamName.setText(tableModel.getValueAt(selectedRow, 1).toString());

            try {
                String dateCreatedStr = tableModel.getValueAt(selectedRow, 2).toString();
                if (!dateCreatedStr.isEmpty()) dateChooserCreated.setDate(SDF.parse(dateCreatedStr));
                else dateChooserCreated.setDate(null);

                String dateDisbandedStr = tableModel.getValueAt(selectedRow, 3).toString();
                if (!dateDisbandedStr.isEmpty()) dateChooserDisbanded.setDate(SDF.parse(dateDisbandedStr));
                else dateChooserDisbanded.setDate(null);
            } catch (ParseException e) {
                // Manejar error de parseo si ocurre
                dateChooserCreated.setDate(null);
                dateChooserDisbanded.setDate(null);
            }

            // Asegurar manejo de valores nulos (si se almacenan como null en la tabla)
            Object idCoachObj = tableModel.getValueAt(selectedRow, 4);
            txtIdCoach.setText(idCoachObj != null ? idCoachObj.toString() : "");

            Object createdByPlayerObj = tableModel.getValueAt(selectedRow, 5);
            txtCreatedByPlayer.setText(createdByPlayerObj != null ? createdByPlayerObj.toString() : "");

            txtTeamId.setEditable(false);
            validateFields();
        }
    }


    /**
     * LÓGICA DE BOTONES (CRUD)
     */
    private void addTeam() {
        if (!validateAndShowErrorOnUI()) return; // Validar campos UI
        if (!validateDateLogic()) return; // Validar lógica de fechas

        try {
            int teamId = Integer.parseInt(txtTeamId.getText().trim());
            int idCoach = Integer.parseInt(txtIdCoach.getText().trim());
            String createdByPlayer = txtCreatedByPlayer.getText().trim();

            // Validación de existencia de jugadores (BD)
            if (!playerCRUD.playerExists(createdByPlayer)) {
                showError("El jugador '" + createdByPlayer + "' no existe en la BD.", "Jugador No Encontrado");
                return;
            }

            Team team = new Team(
                    teamId,
                    txtTeamName.getText().trim(),
                    dateChooserCreated.getDate(),
                    dateChooserDisbanded.getDate(),
                    idCoach,
                    createdByPlayer
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
                showError("Error: Ya existe un equipo con el ID '" + txtTeamId.getText().trim() + "'.", "Error de Clave Primaria");
            } else if (errorMessage.contains("id_coach_fk")) {
                showError("Error de Entrenador: El ID del Entrenador ingresado no existe.", "Error de Clave Foránea");
            } else {
                showError("Error al agregar: " + errorMessage, "Error SQL");
            }
        } catch (NumberFormatException e) {
            showError("Los IDs deben ser números enteros positivos.", "Error de Formato");
        }
    }

    private void updateTeam() {
        if (txtTeamId.isEditable() || table.getSelectedRow() == -1) {
            showError("Selecciona un equipo de la tabla para actualizar.", "Error de Selección");
            return;
        }

        if (!validateAndShowErrorOnUI()) return;
        if (!validateDateLogic()) return;

        try {
            int teamId = Integer.parseInt(txtTeamId.getText().trim());
            int idCoach = Integer.parseInt(txtIdCoach.getText().trim());
            String createdByPlayer = txtCreatedByPlayer.getText().trim();

            // Validación de existencia de jugadores (BD)
            if (!playerCRUD.playerExists(createdByPlayer)) {
                showError("El jugador '" + createdByPlayer + "' no existe en la BD.", "Jugador No Encontrado");
                return;
            }

            Team team = new Team(
                    teamId,
                    txtTeamName.getText().trim(),
                    dateChooserCreated.getDate(),
                    dateChooserDisbanded.getDate(),
                    idCoach,
                    createdByPlayer
            );

            if (teamCRUD.updateTeam(team)) {
                showSuccess("¡Equipo actualizado exitosamente!");
                clearFields();
                loadTeams();
            } else {
                showError("No se pudo actualizar el equipo. Asegúrate que el ID exista.", "Error de Actualización");
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("id_coach_fk")) {
                showError("Error de Entrenador: El ID del Entrenador ingresado no existe.", "Error de Clave Foránea");
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
                "¿Estás seguro de eliminar este equipo? Si el equipo tiene dependencias (jugadores, etc.) la operación fallará.",
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
                    showError("No se pudo eliminar el equipo. Es posible que el ID no exista.", "Error de Eliminación");
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("violates foreign key constraint")) {
                    showError("Error: No puedes eliminar este equipo porque tiene dependencias asociadas.", "Error de Clave Foránea");
                } else {
                    showError("Error al eliminar: " + e.getMessage(), "Error SQL");
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
        txtTeamId.setEditable(true);
        table.clearSelection();

        // Limpiar JLabels de error
        lblErrorTeamId.setText(" ");
        lblErrorTeamName.setText(" ");
        lblErrorIdCoach.setText(" ");
        lblErrorCreatedByPlayer.setText(" ");
        lblErrorDateCreated.setText(" ");
        lblErrorDateDisbanded.setText(" ");

        validateFields();
    }

    /**
     * LÓGICA DE VALIDACIÓN EN TIEMPO REAL (UI)
     */
    private boolean validateAndShowErrorOnUI() {
        validateFields(); // Forzar la validación para actualizar los errores de la UI
        // Si cualquier JLabel de error contiene texto distinto de " ", significa que hay un error visible.
        return lblErrorTeamId.getText().equals(" ") &&
                lblErrorTeamName.getText().equals(" ") &&
                lblErrorIdCoach.getText().equals(" ") &&
                lblErrorCreatedByPlayer.getText().equals(" ") &&
                lblErrorDateCreated.getText().equals(" ") &&
                lblErrorDateDisbanded.getText().equals(" ");
    }


    private void validateFields() {
        boolean isValid = true;

        // Limpiar errores primero
        lblErrorTeamId.setText(" ");
        lblErrorTeamName.setText(" ");
        lblErrorIdCoach.setText(" ");
        lblErrorCreatedByPlayer.setText(" ");
        lblErrorDateCreated.setText(" ");
        lblErrorDateDisbanded.setText(" ");

        // 1. Validaciones de ID del Equipo
        if (txtTeamId.getText().trim().isEmpty()) {
            lblErrorTeamId.setText("El ID es obligatorio.");
            isValid = false;
        } else {
            try {
                if (Integer.parseInt(txtTeamId.getText().trim()) <= 0) {
                    lblErrorTeamId.setText("Debe ser un número positivo.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorTeamId.setText("Debe ser un número entero.");
                isValid = false;
            }
        }

        // 2. Validaciones de Nombre del Equipo
        if (txtTeamName.getText().trim().isEmpty()) {
            lblErrorTeamName.setText("El nombre es obligatorio.");
            isValid = false;
        } else if (txtTeamName.getText().trim().length() < 3) {
            lblErrorTeamName.setText("Mínimo 3 caracteres.");
            isValid = false;
        }

        // 3. Validaciones de ID del Entrenador
        if (txtIdCoach.getText().trim().isEmpty()) {
            lblErrorIdCoach.setText("El ID del entrenador es obligatorio.");
            isValid = false;
        } else {
            try {
                if (Integer.parseInt(txtIdCoach.getText().trim()) <= 0) {
                    lblErrorIdCoach.setText("Debe ser un número positivo.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorIdCoach.setText("Debe ser un número entero.");
                isValid = false;
            }
        }

        // 4. Validaciones de Jugador Creador
        if (txtCreatedByPlayer.getText().trim().isEmpty()) {
            lblErrorCreatedByPlayer.setText("El jugador creador es obligatorio.");
            isValid = false;
        }

        // 5. Validaciones de Fecha de Creación
        Date dateCreated = dateChooserCreated.getDate();
        if (dateCreated == null) {
            lblErrorDateCreated.setText("La fecha es obligatoria.");
            isValid = false;
        }

        // 6. Validaciones de Lógica de Fechas (si ambas existen)
        Date dateDisbanded = dateChooserDisbanded.getDate();
        if (dateCreated != null && dateDisbanded != null) {
            if (dateCreated.after(dateDisbanded)) {
                lblErrorDateDisbanded.setText("No puede ser anterior a creación.");
                isValid = false;
            }
        }

        // Actualizar el estado de los botones
        boolean isSelected = !txtTeamId.isEditable();
        btnAdd.setEnabled(isValid && !isSelected);
        btnUpdate.setEnabled(isValid && isSelected);
        btnDelete.setEnabled(isSelected);
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
        return true;
    }

    /**
     * MÉTODOS DE UTILIDAD
     */
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

    // Métodos de utilidad para fonts y colores (asumiendo que no están en DesignConstants)
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