package vistas;

import consultas.GameCRUD;
import tablas.Game;
// Importar DesignConstants para usar el mismo estilo
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.swing.event.CaretListener;

public class GamesPanel extends JPanel {

    private Connection connection;
    private GameCRUD gameCRUD;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtGameCode, txtGameName, txtDescription, txtGenres;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private JLabel lblCodeError, lblNameError, lblGenresError; // Nuevo: lblGenresError

    public GamesPanel(Connection connection) {
        this.connection = connection;
        this.gameCRUD = new GameCRUD(connection);
        initComponents();
        loadGames();
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ACCENT_DANGER);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setBorder(new EmptyBorder(2, 0, 0, 0));
        return label;
    }

    private CaretListener createCaretListener() {
        return e -> {
            // Llama a validateFields() para reevaluar la validez y el estado de los botones
            validateFields();
        };
    }

    private void initComponents() {
        setLayout(new BorderLayout(SPACING_MD, SPACING_MD));
        setBackground(BG_DARK_SECONDARY);
        setBorder(new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD));

        // Inicializar la tabla y el modelo AQUÍ, antes de crear el formulario
        initializeTableComponents();

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        // Llamar a validateFields() una vez que 'table' ya está inicializado
        validateFields(false);
    }

    private void initializeTableComponents() {
        String[] columns = {"Código", "Nombre", "Descripción", "Géneros"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JPanel fieldsPanel = new JPanel(new GridLayout(1, 4, SPACING_MD, SPACING_MD));
        fieldsPanel.setBackground(BG_CARD);

        txtGameCode = createStyledTextField();
        txtGameName = createStyledTextField();
        txtDescription = createStyledTextField();
        txtGenres = createStyledTextField();

        // Inicializar JLabels de error
        lblCodeError = createErrorLabel();
        lblNameError = createErrorLabel();
        lblGenresError = createErrorLabel(); // Inicializar el nuevo label de error

        // Asignar listeners para revalidar y ocultar errores al escribir
        CaretListener listener = createCaretListener();
        txtGameCode.addCaretListener(listener);
        txtGameName.addCaretListener(listener);
        txtDescription.addCaretListener(listener); // Aunque no tiene check, revalidamos
        txtGenres.addCaretListener(listener); // Listener para Géneros

        fieldsPanel.add(createFieldPanel("Código del Juego:", txtGameCode, lblCodeError));
        fieldsPanel.add(createFieldPanel("Nombre:", txtGameName, lblNameError));
        fieldsPanel.add(createFieldPanel("Descripción:", txtDescription, null)); // Descripción no tiene validación en línea
        fieldsPanel.add(createFieldPanel("Géneros:", txtGenres, lblGenresError)); // Géneros tiene validación de longitud

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Agregar", ACCENT_SUCCESS);
        btnUpdate = createStyledButton("Actualizar", ACCENT_WARNING);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addGame());
        btnUpdate.addActionListener(e -> updateGame());
        btnDelete.addActionListener(e -> deleteGame());
        btnClear.addActionListener(e -> clearFields());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFieldPanel(String labelText, JTextField textField, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(BG_CARD);
        inputPanel.add(textField, BorderLayout.NORTH);

        if (errorLabel != null) {
            inputPanel.add(errorLabel, BorderLayout.SOUTH);
        }

        panel.add(label, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);

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

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JLabel titleLabel = new JLabel("Lista de Juegos");
        titleLabel.setFont(getTitleFont(FONT_SIZE_H2));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, SPACING_MD, 0));

        // Configuración de JTable (JTable y tableModel ya están inicializados en initializeTableComponents)
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
                loadSelectedGame();
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
            private Color darken(Color color, float factor) { return color; }
            private Color brighten(Color color, float factor) { return color; }
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

    private void loadGames() {
        tableModel.setRowCount(0);
        try {
            List<Game> games = gameCRUD.getAllGames();
            for (Game game : games) {
                Object[] row = {
                        game.getGameCode(),
                        game.getGameName(),
                        game.getGameDescription(),
                        game.getGameGenres()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error al cargar los juegos: " + e.getMessage());
        }
    }

    private void loadSelectedGame() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtGameCode.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtGameName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtDescription.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtGenres.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtGameCode.setEditable(false);

            // Limpiar errores al seleccionar un registro
            lblCodeError.setText(" ");
            lblNameError.setText(" ");
            lblGenresError.setText(" ");

            // Validar para habilitar el botón de actualizar
            validateFields(false);
        }
    }

    private void addGame() {
        if (!validateFields(true)) return;

        try {
            Game game = new Game(
                    txtGameCode.getText().trim(),
                    txtGameName.getText().trim(),
                    txtDescription.getText().trim(),
                    txtGenres.getText().trim()
            );

            if (gameCRUD.createGame(game)) {
                showSuccess("¡Juego agregado exitosamente!");
                clearFields();
                loadGames();
            } else {
                showError("No se pudo agregar el juego.");
            }
        } catch (SQLException e) {
            showError("Error al agregar: " + e.getMessage());
        }
    }

    private void updateGame() {
        if (txtGameCode.getText().trim().isEmpty()) {
            showError("Selecciona un juego de la tabla para actualizar.");
            return;
        }

        if (!validateFields(false)) return;

        try {
            Game game = new Game(
                    txtGameCode.getText().trim(),
                    txtGameName.getText().trim(),
                    txtDescription.getText().trim(),
                    txtGenres.getText().trim()
            );

            if (gameCRUD.updateGame(game)) {
                showSuccess("¡Juego actualizado exitosamente!");
                clearFields();
                loadGames();
            } else {
                showError("No se pudo actualizar el juego.");
            }
        } catch (SQLException e) {
            showError("Error al actualizar: " + e.getMessage());
        }
    }

    private void deleteGame() {
        if (txtGameCode.getText().trim().isEmpty()) {
            showError("Selecciona un juego de la tabla para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar este juego?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (gameCRUD.deleteGame(txtGameCode.getText().trim())) {
                    showSuccess("¡Juego eliminado exitosamente!");
                    clearFields();
                    loadGames();
                } else {
                    showError("No se pudo eliminar el juego.");
                }
            } catch (SQLException e) {
                showError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        txtGameCode.setText("");
        txtGameName.setText("");
        txtDescription.setText("");
        txtGenres.setText("");
        txtGameCode.setEditable(true);
        table.clearSelection();

        // Limpiar mensajes de error
        lblCodeError.setText(" ");
        lblNameError.setText(" ");
        lblGenresError.setText(" "); // Limpiar el error de géneros

        // Deshabilitar botones al limpiar
        validateFields(true);
    }

    /**
     * Valida campos, actualiza mensajes de error y habilita/deshabilita botones,
     * incluyendo las restricciones CHECK de la tabla SQL.
     * @param isAdding true si el modo actual es Agregar (txtGameCode editable).
     * @return true si los campos obligatorios son válidos.
     */
    private boolean validateFields(boolean isAdding) {
        boolean isValid = true;

        // --- 1. Validación Código (Obligatorio) ---
        if (txtGameCode.getText().trim().isEmpty()) {
            lblCodeError.setText("El código es obligatorio.");
            isValid = false;
        } else {
            lblCodeError.setText(" ");
        }

        // --- 2. Validación Nombre (Obligatorio + CHECK LENGTH >= 3) ---
        String gameName = txtGameName.getText().trim();
        if (gameName.isEmpty()) {
            lblNameError.setText("El nombre es obligatorio.");
            isValid = false;
        } else if (gameName.length() < 3) {
            lblNameError.setText("Mínimo 3 caracteres.");
            isValid = false;
        } else {
            lblNameError.setText(" ");
        }

        // --- 3. Validación Géneros (Obligatorio + CHECK LENGTH >= 3) ---
        String genres = txtGenres.getText().trim();
        if (genres.isEmpty()) {
            lblGenresError.setText("Los géneros son obligatorios.");
            isValid = false;
        } else if (genres.length() < 3) {
            lblGenresError.setText("Mínimo 3 caracteres.");
            isValid = false;
        } else {
            lblGenresError.setText(" ");
        }

        // --- 4. Habilitar/Deshabilitar botones ---
        btnAdd.setEnabled(isValid && isAdding);
        btnUpdate.setEnabled(isValid && !isAdding && table.getSelectedRow() != -1);
        btnDelete.setEnabled(table.getSelectedRow() != -1);

        return isValid;
    }

    // Sobrecarga del método de validación para llamar desde listeners
    private boolean validateFields() {
        return validateFields(txtGameCode.isEditable());
    }


    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    // CLASE INTERNA PARA FORZAR EL ESTILO DE LA CABECERA
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
            return new Font("Segoe UI", Font.BOLD, size);
        }
    }
}