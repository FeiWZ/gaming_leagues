package vistas;

import consultas.PlayerCRUD;
import consultas.RankingPlayerGameCRUD;
import tablas.RankingPlayerGame;
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

public class RankingsPanel extends JPanel {

    private Connection connection;
    private RankingPlayerGameCRUD rankingCRUD;
    private PlayerCRUD playerCRUD;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtPlayerId, txtPlayerName, txtGameName, txtRanking, txtWins, txtLosses;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    // Declaración de JLabels para los mensajes de validación
    private JLabel lblErrorPlayerId, lblErrorPlayerName, lblErrorGameName;
    private JLabel lblErrorRanking, lblErrorWins, lblErrorLosses;

    public RankingsPanel(Connection connection) {
        this.connection = connection;
        this.rankingCRUD = new RankingPlayerGameCRUD(connection);
        this.playerCRUD = new PlayerCRUD(connection);

        // Inicializar componentes de la tabla ANTES de crear el formulario
        initializeTableComponents();
        initComponents();
        loadRankings();

        // Validar el estado inicial de los botones
        validateFields(true);
    }

    // Método auxiliar para crear el JLabel de error con estilo rojo
    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ACCENT_DANGER);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setBorder(new EmptyBorder(2, 0, 0, 0));
        return label;
    }

    private CaretListener createCaretListener() {
        return e -> validateFields();
    }

    private void initializeTableComponents() {
        String[] columns = {"ID Jugador", "Nombre Jugador", "Nombre Juego", "Ranking", "Victorias", "Derrotas"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
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

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 3, SPACING_MD, SPACING_LG));
        fieldsPanel.setBackground(BG_CARD);

        txtPlayerId = createStyledTextField();
        txtPlayerName = createStyledTextField();
        txtGameName = createStyledTextField();
        txtRanking = createStyledTextField();
        txtWins = createStyledTextField();
        txtLosses = createStyledTextField();

        // Inicializar JLabels de error
        lblErrorPlayerId = createErrorLabel();
        lblErrorPlayerName = createErrorLabel();
        lblErrorGameName = createErrorLabel();
        lblErrorRanking = createErrorLabel();
        lblErrorWins = createErrorLabel();
        lblErrorLosses = createErrorLabel();

        // Asignar listeners
        CaretListener listener = createCaretListener();
        txtPlayerId.addCaretListener(listener);
        txtPlayerName.addCaretListener(listener);
        txtGameName.addCaretListener(listener);
        txtRanking.addCaretListener(listener);
        txtWins.addCaretListener(listener);
        txtLosses.addCaretListener(listener);


        // Usar el nuevo createFieldPanel con validación
        fieldsPanel.add(createValidatedFieldPanel("ID Jugador:", txtPlayerId, lblErrorPlayerId));
        fieldsPanel.add(createValidatedFieldPanel("Nombre del Jugador:", txtPlayerName, lblErrorPlayerName));
        fieldsPanel.add(createValidatedFieldPanel("Nombre del Juego:", txtGameName, lblErrorGameName));
        fieldsPanel.add(createValidatedFieldPanel("Ranking (Opcional):", txtRanking, lblErrorRanking));
        fieldsPanel.add(createValidatedFieldPanel("Victorias:", txtWins, lblErrorWins));
        fieldsPanel.add(createValidatedFieldPanel("Derrotas:", txtLosses, lblErrorLosses));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Agregar", ACCENT_SUCCESS);
        btnUpdate = createStyledButton("Actualizar", ACCENT_WARNING);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addRanking());
        btnUpdate.addActionListener(e -> updateRanking());
        btnDelete.addActionListener(e -> deleteRanking());
        btnClear.addActionListener(e -> clearFields());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Nuevo método para campos de texto con validación
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

    private JPanel createFieldPanel(String labelText, JTextField textField) {
        // Mantenemos esta versión como alias por si se usa en otro contexto, pero ahora llamamos a createValidatedFieldPanel
        return createValidatedFieldPanel(labelText, textField, new JLabel(" ")); // Usar un label vacío si no se necesita validación roja
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JLabel titleLabel = new JLabel("Ranking de Jugadores por Juego");
        titleLabel.setFont(getTitleFont(FONT_SIZE_H2));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, SPACING_MD, 0));

        // JTable y tableModel inicializados en initializeTableComponents
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
                loadSelectedRanking();
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

    private void loadRankings() {
        tableModel.setRowCount(0);
        try {
            List<RankingPlayerGame> rankings = rankingCRUD.getAllRankingPlayerGame();
            for (RankingPlayerGame ranking : rankings) {
                Object[] row = {
                        ranking.getPlayerId(),
                        ranking.getPlayerName(),
                        ranking.getGameName(),
                        ranking.getRanking(),
                        ranking.getWins(),
                        ranking.getLosses()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error al cargar rankings: " + e.getMessage(), "Error de Carga");
        }
    }

    private void loadSelectedRanking() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtPlayerId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtPlayerName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtGameName.setText(tableModel.getValueAt(selectedRow, 2).toString());

            Object rankingValue = tableModel.getValueAt(selectedRow, 3);
            txtRanking.setText(rankingValue != null ? rankingValue.toString() : "");

            txtWins.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtLosses.setText(tableModel.getValueAt(selectedRow, 5).toString());

            txtPlayerId.setEditable(false);
            txtGameName.setEditable(false);

            clearErrorMessages();
            validateFields(false); // Validar en modo Actualizar
        }
    }

    private void addRanking() {
        if (!validateFields(true)) return;

        try {
            Integer ranking = null;
            if (!txtRanking.getText().trim().isEmpty()) {
                ranking = Integer.parseInt(txtRanking.getText().trim());
            }

            int playerId = Integer.parseInt(txtPlayerId.getText().trim());
            int wins = Integer.parseInt(txtWins.getText().trim());
            int losses = Integer.parseInt(txtLosses.getText().trim());

            RankingPlayerGame rankingPlayerGame = new RankingPlayerGame(
                    playerId,
                    txtPlayerName.getText().trim(),
                    txtGameName.getText().trim(),
                    ranking,
                    wins,
                    losses
            );

            if (rankingCRUD.createRankingPlayerGame(rankingPlayerGame)) {
                showSuccess("Ranking agregado exitosamente!");
                clearFields();
                loadRankings();
            } else {
                showError("No se pudo agregar el ranking.", "Error de Inserción");
            }
        } catch (NumberFormatException e) {
            showError("Error de formato: ID Jugador, Ranking, Victorias y Derrotas deben ser números enteros válidos.", "Error de Formato");
        } catch (SQLException e) {
            handleSQLError(e);
        }
    }

    private void updateRanking() {
        if (txtPlayerId.getText().trim().isEmpty() || txtGameName.getText().trim().isEmpty()) {
            showError("Selecciona un ranking de la tabla para actualizar.", "Error de Selección");
            return;
        }

        if (!validateFields(false)) return;

        try {
            Integer ranking = null;
            if (!txtRanking.getText().trim().isEmpty()) {
                ranking = Integer.parseInt(txtRanking.getText().trim());
            }

            int playerId = Integer.parseInt(txtPlayerId.getText().trim());
            int wins = Integer.parseInt(txtWins.getText().trim());
            int losses = Integer.parseInt(txtLosses.getText().trim());

            RankingPlayerGame rankingPlayerGame = new RankingPlayerGame(
                    playerId,
                    txtPlayerName.getText().trim(),
                    txtGameName.getText().trim(),
                    ranking,
                    wins,
                    losses
            );

            if (rankingCRUD.updateRankingPlayerGame(rankingPlayerGame)) {
                showSuccess("Ranking actualizado exitosamente!");
                clearFields();
                loadRankings();
            } else {
                showError("No se pudo actualizar el ranking.", "Error de Actualización");
            }
        } catch (NumberFormatException e) {
            showError("Error de formato: Ranking, Victorias y Derrotas deben ser números enteros válidos.", "Error de Formato");
        } catch (SQLException e) {
            handleSQLError(e);
        }
    }

    private void deleteRanking() {
        if (txtPlayerId.getText().trim().isEmpty() || txtGameName.getText().trim().isEmpty()) {
            showError("Selecciona un ranking de la tabla para eliminar.", "Error de Selección");
            return;
        }

        try {
            Integer.parseInt(txtPlayerId.getText().trim());
        } catch (NumberFormatException e) {
            showError("El ID del Jugador debe ser un número válido.", "Error de Formato");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar este ranking?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (rankingCRUD.deleteRankingPlayerGame(
                        Integer.parseInt(txtPlayerId.getText().trim()),
                        txtGameName.getText().trim())) {
                    showSuccess("Ranking eliminado exitosamente!");
                    clearFields();
                    loadRankings();
                } else {
                    showError("No se pudo eliminar el ranking.", "Error de Eliminación");
                }
            } catch (SQLException e) {
                showError("Error al eliminar: " + e.getMessage(), "Error SQL");
            }
        }
    }

    private void clearErrorMessages() {
        lblErrorPlayerId.setText(" ");
        lblErrorPlayerName.setText(" ");
        lblErrorGameName.setText(" ");
        lblErrorRanking.setText(" ");
        lblErrorWins.setText(" ");
        lblErrorLosses.setText(" ");
    }

    private void clearFields() {
        txtPlayerId.setText("");
        txtPlayerName.setText("");
        txtGameName.setText("");
        txtRanking.setText("");
        txtWins.setText("");
        txtLosses.setText("");
        txtPlayerId.setEditable(true);
        txtGameName.setEditable(true);
        table.clearSelection();

        clearErrorMessages();
        validateFields(true); // Validar en modo Agregar
    }

    // Sobrecarga del método de validación para llamar desde listeners
    private boolean validateFields() {
        // Si el ID del jugador y el Nombre del Juego son editables, asumimos modo Agregar.
        boolean isAdding = txtPlayerId.isEditable() && txtGameName.isEditable();
        return validateFields(isAdding);
    }


    private boolean validateFields(boolean isAdding) {
        boolean isValid = true;

        String idText = txtPlayerId.getText().trim();
        String playerName = txtPlayerName.getText().trim();
        String gameName = txtGameName.getText().trim();
        String rankingText = txtRanking.getText().trim();
        String winsText = txtWins.getText().trim();
        String lossesText = txtLosses.getText().trim();

        // --- 1. ID Jugador (NOT NULL + Numérico > 0) ---
        if (idText.isEmpty()) {
            lblErrorPlayerId.setText("ID obligatorio.");
            isValid = false;
        } else {
            try {
                int playerId = Integer.parseInt(idText);
                if (playerId <= 0) {
                    lblErrorPlayerId.setText("Debe ser un número entero positivo.");
                    isValid = false;
                } else {
                    lblErrorPlayerId.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorPlayerId.setText("Debe ser un número entero válido.");
                isValid = false;
            }
        }

        // --- 2. Nombre del Jugador (NOT NULL) ---
        if (playerName.isEmpty()) {
            lblErrorPlayerName.setText("Nombre obligatorio.");
            isValid = false;
        } else {
            lblErrorPlayerName.setText(" ");
            // Nota: La validación de existencia del jugador se hace solo en addRanking/updateRanking
        }

        // --- 3. Nombre del Juego (NOT NULL) ---
        if (gameName.isEmpty()) {
            lblErrorGameName.setText("Juego obligatorio.");
            isValid = false;
        } else {
            lblErrorGameName.setText(" ");
        }

        // --- 4. Victorias (NOT NULL + CHECK >= 0) ---
        if (winsText.isEmpty()) {
            lblErrorWins.setText("Victorias obligatorio.");
            isValid = false;
        } else {
            try {
                int wins = Integer.parseInt(winsText);
                if (wins < 0) {
                    lblErrorWins.setText("Debe ser $\\geq 0$.");
                    isValid = false;
                } else {
                    lblErrorWins.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorWins.setText("Debe ser un número entero válido.");
                isValid = false;
            }
        }

        // --- 5. Derrotas (NOT NULL + CHECK >= 0) ---
        if (lossesText.isEmpty()) {
            lblErrorLosses.setText("Derrotas obligatorio.");
            isValid = false;
        } else {
            try {
                int losses = Integer.parseInt(lossesText);
                if (losses < 0) {
                    lblErrorLosses.setText("Debe ser $\\geq 0$.");
                    isValid = false;
                } else {
                    lblErrorLosses.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorLosses.setText("Debe ser un número entero válido.");
                isValid = false;
            }
        }

        // --- 6. Ranking (Opcional, si se ingresa debe ser numérico > 0) ---
        if (!rankingText.isEmpty()) {
            try {
                int ranking = Integer.parseInt(rankingText);
                if (ranking <= 0) {
                    lblErrorRanking.setText("Si se ingresa, debe ser positivo.");
                    isValid = false;
                } else {
                    lblErrorRanking.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorRanking.setText("Debe ser un número entero válido.");
                isValid = false;
            }
        } else {
            lblErrorRanking.setText(" "); // Limpiar si está vacío
        }

        // Control de botones
        boolean rowSelected = table.getSelectedRow() != -1;
        btnAdd.setEnabled(isValid && isAdding);
        btnUpdate.setEnabled(isValid && !isAdding && rowSelected);
        btnDelete.setEnabled(rowSelected);

        return isValid;
    }


    private void handleSQLError(SQLException e) {
        String errorMessage = e.getMessage();

        if (errorMessage.contains("duplicate key value") && errorMessage.contains("ranking_player_game_pkey")) {
            showError("Error: Ya existe un ranking para el par (Jugador ID, Nombre Juego). Esta combinación debe ser única.", "Error de Clave Compuesta");
        } else if (errorMessage.contains("violates foreign key constraint")) {
            if (errorMessage.contains("player_id")) {
                showError("Error de Jugador: El ID de Jugador ingresado no existe en la tabla de Jugadores.", "Error de Clave Foránea");
            } else if (errorMessage.contains("game_name")) {
                showError("Error de Juego: El Nombre de Juego ingresado no existe en la tabla de Juegos.", "Error de Clave Foránea");
            } else {
                showError("Error de Clave Foránea: Asegúrate de que el ID de Jugador y el Nombre de Juego existan.", "Error de Clave Foránea");
            }
        } else if (errorMessage.contains("violates check constraint")) {
            showError("Error de Validación: Victorias, Derrotas y Ranking deben ser números positivos o cero.", "Error de Restricción");
        } else {
            showError("Error de BD: " + errorMessage, "Error SQL");
        }
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showError(String message) {
        showError(message, "Error");
    }

    private class CustomTableHeaderRenderer extends JLabel implements TableCellRenderer {
        // ... (Clase CustomTableHeaderRenderer)
        private final Color backgroundColor;
        private final Color foregroundColor;
        private static final int RADIUS_SM = 8;


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
            return RankingsPanel.this.getBoldFont(size);
        }
    }

    // Métodos utilitarios de fuente y color (asumimos que existen o se toman de DesignConstants)
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