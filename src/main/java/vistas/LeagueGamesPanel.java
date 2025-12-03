package vistas;

import consultas.GameLeagueCRUD;
import tablas.GameLeague;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentListener; // CAMBIO: Usaremos DocumentListener para una mejor validación en tiempo real

public class LeagueGamesPanel extends JPanel {

    private Connection connection;
    private GameLeagueCRUD leagueGameCRUD;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtLeagueId, txtGameCode;

    // Se mantiene CaretListener o se cambia a DocumentListener, lo cambiamos para consistencia con otras pestañas.
    private DocumentListener validationListener;

    // CAMBIO: Etiquetas de error
    private JLabel lblErrorLeagueId, lblErrorGameCode;

    private JButton btnAdd, btnDelete, btnClear, btnRefresh;

    // Métodos de Font originales (asumiendo que están disponibles en DesignConstants o en la clase)
    private Font getBodyFont(int size) { return new Font("Roboto", Font.PLAIN, size); }
    private Font getTitleFont(int size) { return new Font("Roboto Slab", Font.BOLD, size); }
    private Font getBoldFont(int size) { return new Font("Roboto", Font.BOLD, size); }


    public LeagueGamesPanel(Connection connection) {
        this.connection = connection;
        this.leagueGameCRUD = new GameLeagueCRUD(connection);

        // Creamos el validationListener como DocumentListener para ser más robustos
        validationListener = createValidationListener();

        initComponents();
        loadLeagueGames(); // CAMBIO CLAVE: Cargar datos inmediatamente
    }

    // Se elimina initializePanelData() ya que loadLeagueGames() se llama en el constructor.


    // --- MÉTODOS DE UTILIDAD PARA VALIDACIÓN ---

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

    // --- COMPONENTES ---

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
        lblErrorLeagueId = createErrorLabel();
        lblErrorGameCode = createErrorLabel();


        // Contenedor de campos
        JPanel fieldsPanel = new JPanel(new GridLayout(1, 2, SPACING_LG, SPACING_MD));
        fieldsPanel.setBackground(BG_CARD);
        fieldsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        txtLeagueId = createStyledTextField();
        txtGameCode = createStyledTextField();

        // Usamos DocumentListener para la validación en tiempo real
        txtLeagueId.getDocument().addDocumentListener(validationListener);
        txtGameCode.getDocument().addDocumentListener(validationListener);

        fieldsPanel.add(createValidatedFieldPanel("ID de la Liga:", txtLeagueId, lblErrorLeagueId));
        fieldsPanel.add(createValidatedFieldPanel("Código del Juego:", txtGameCode, lblErrorGameCode));

        // --- Botones ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Asociar", ACCENT_SUCCESS);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);
        btnRefresh = createStyledButton("Refrescar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addLeagueGame());
        btnDelete.addActionListener(e -> deleteLeagueGame());
        btnClear.addActionListener(e -> clearFields()); // Solo limpia campos y deselecciona
        btnRefresh.addActionListener(e -> loadLeagueGames()); // Mantenemos el botón de refresh

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);
        buttonsPanel.add(btnRefresh);

        JPanel centerPanel = new JPanel(new BorderLayout(0, SPACING_MD));
        centerPanel.setBackground(BG_CARD);
        centerPanel.add(fieldsPanel, BorderLayout.NORTH);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        validateAllFields();
        return panel;
    }

    // Se elimina el createFieldPanel simple, usando la versión validada.

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JLabel titleLabel = new JLabel("Juegos Asociados a Ligas");
        titleLabel.setFont(getTitleFont(FONT_SIZE_H2));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, SPACING_MD, 0));

        String[] columns = {"ID Liga", "Código del Juego"};
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
            // CORRECCIÓN CLAVE: Comprobar nulidad de 'table'
            if (table != null && !e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedLeagueGame();
                validateAllFields(); // Usamos el nuevo método
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
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }

                int RADIUS_SM = 8;
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

    // --- LÓGICA DE VALIDACIÓN Y DATOS ---

    private void validateAllFields() {
        // CORRECCIÓN: Si los botones no están inicializados, salimos
        if (btnAdd == null || btnDelete == null) return;

        boolean isValid = true;

        String leagueIdText = txtLeagueId.getText().trim();
        String gameCode = txtGameCode.getText().trim();

        // 1. ID Liga
        if (leagueIdText.isEmpty()) {
            lblErrorLeagueId.setText("El ID de la Liga es obligatorio.");
            isValid = false;
        } else {
            try {
                int id = Integer.parseInt(leagueIdText);
                if (id <= 0) {
                    lblErrorLeagueId.setText("Debe ser un número positivo.");
                    isValid = false;
                } else {
                    lblErrorLeagueId.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorLeagueId.setText("Debe ser un número entero válido.");
                isValid = false;
            }
        }

        // 2. Código Juego
        if (gameCode.isEmpty()) {
            lblErrorGameCode.setText("El Código del Juego es obligatorio.");
            isValid = false;
        } else if (gameCode.length() < 2) {
            lblErrorGameCode.setText("Mínimo 2 caracteres.");
            isValid = false;
        } else {
            lblErrorGameCode.setText(" ");
        }

        // Estado de botones
        boolean isRowSelected = (table != null && table.getSelectedRow() != -1);

        btnAdd.setEnabled(isValid && !isRowSelected);
        btnDelete.setEnabled(isRowSelected);
    }

    private void loadLeagueGames() {
        tableModel.setRowCount(0);
        try {
            List<GameLeague> leagueGames = leagueGameCRUD.getAllLeaguesGames();
            for (GameLeague lg : leagueGames) {
                Object[] row = {
                        lg.getLeagueId(),
                        lg.getGameCode()
                };
                tableModel.addRow(row);
            }
            clearFields();
        } catch (SQLException e) {
            showError("Error al cargar las asociaciones: " + e.getMessage());
        }
    }

    private void loadSelectedLeagueGame() {
        // CORRECCIÓN CLAVE: Si la tabla es nula, salimos inmediatamente.
        if (table == null) return;

        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            // Obtenemos los valores de las celdas
            txtLeagueId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtGameCode.setText(tableModel.getValueAt(selectedRow, 1).toString());
        }
    }

    private void addLeagueGame() {
        if (!btnAdd.isEnabled()) {
            showError("Corrige los errores en los campos antes de asociar.", "Validación Pendiente");
            return;
        }

        try {
            int leagueId = Integer.parseInt(txtLeagueId.getText().trim());
            String gameCode = txtGameCode.getText().trim();

            GameLeague newAssociation = new GameLeague(leagueId, gameCode);

            boolean success = leagueGameCRUD.createLeaguesGames(newAssociation);

            if (success) {
                showSuccess("Asociación creada exitosamente.");
                clearFields();
                loadLeagueGames(); // Recargar la tabla
            } else {
                showError("No se pudo crear la asociación. Puede que ya exista o el ID/Código no sea válido.");
            }

        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("duplicate key value")) {
                showError("Error: Esta asociación (ID Liga y Código Juego) ya existe.", "Error de Clave Compuesta");
            } else if (errorMessage.contains("violates foreign key constraint")) {
                showError("Error: El ID de la Liga o el Código del Juego no existen en sus respectivas tablas.", "Error de Clave Foránea");
            } else {
                showError("Error al insertar la asociación: " + errorMessage, "Error SQL");
            }
        } catch (NumberFormatException e) {
            showError("El ID de la Liga debe ser un número entero válido.", "Error de Formato");
        }
    }

    private void deleteLeagueGame() {
        if (!btnDelete.isEnabled()) {
            showError("Debe seleccionar una asociación para poder eliminarla.", "Error de Selección");
            return;
        }

        int selectedRow = table.getSelectedRow();

        try {
            // Los valores se obtienen de las columnas 0 y 1 del modelo
            // Es seguro castear si se carga correctamente en loadLeagueGames
            int leagueId = (int) tableModel.getValueAt(selectedRow, 0);
            String gameCode = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de que desea eliminar la asociación de Liga " + leagueId + " con el Juego " + gameCode + "?",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = leagueGameCRUD.deleteLeaguesGames(leagueId, gameCode);

                if (success) {
                    showSuccess("Asociación eliminada exitosamente.");
                    loadLeagueGames();
                    clearFields();
                } else {
                    showError("No se pudo eliminar la asociación. Puede que ya no exista.", "Error de BD");
                }
            }
        } catch (SQLException e) {
            showError("Error al eliminar la asociación: " + e.getMessage(), "Error SQL");
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            showError("Error interno al leer datos de la tabla. Intente refrescar.", "Error de Datos");
        }
    }

    private void clearFields() {
        txtLeagueId.setText("");
        txtGameCode.setText("");

        // Limpiar errores
        lblErrorLeagueId.setText(" ");
        lblErrorGameCode.setText(" ");

        if (table != null) {
            table.clearSelection();
        }
        validateAllFields();
    }

    // Se elimina handleClearButton original, ya que no se ajusta a la función "Limpiar" estándar.
    // Si se desea la funcionalidad de Borrar TODO, se debe crear un botón separado.

    // --- UTILERÍAS ---

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error de Asociación", JOptionPane.ERROR_MESSAGE);
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
            // Usamos la referencia a la clase externa para obtener el font
            return LeagueGamesPanel.this.getBoldFont(size);
        }
    }
}