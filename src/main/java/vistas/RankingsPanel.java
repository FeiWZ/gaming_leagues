package vistas;

import consultas.PlayerCRUD;
import consultas.RankingPlayerGameCRUD;
import tablas.RankingPlayerGame;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.DocumentListener;
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

    // Etiquetas de error visibles solo para campos obligatorios o claves
    private JLabel lblErrorPlayerId, lblErrorPlayerName, lblErrorGameName, lblErrorWins, lblErrorLosses;

    // Etiqueta de error para el Ranking (oculto en el panel, solo para validación lógica)
    private JLabel lblErrorRankingLogic;

    private DocumentListener validationListener;

    public RankingsPanel(Connection connection) {
        this.connection = connection;
        this.rankingCRUD = new RankingPlayerGameCRUD(connection);
        this.playerCRUD = new PlayerCRUD(connection);
        validationListener = createValidationListener();
        initComponents();
        loadRankings(); // Cargar los datos al inicio
        validateAllFields(); // Validar estados iniciales de botones
    }

    // --- MÉTODOS DE UTILIDAD PARA ESTILO Y VALIDACIÓN ---

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

    /** Crea un panel de campo que incluye la etiqueta (label), el campo de texto (textField) y el error (errorLabel) */
    private JPanel createValidatedFieldPanel(String labelText, JTextField textField, JLabel errorLabel, boolean showErrorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(textField, BorderLayout.NORTH);

        if (showErrorLabel) {
            contentPanel.add(errorLabel, BorderLayout.SOUTH);
        } else {
            // Espacio vacío para mantener alineación vertical si el error no es visible
            contentPanel.add(new JLabel(" "), BorderLayout.SOUTH);
        }


        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // --- INICIALIZACIÓN DE COMPONENTES ---

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
        lblErrorPlayerId = createErrorLabel();
        lblErrorPlayerName = createErrorLabel();
        lblErrorGameName = createErrorLabel();
        lblErrorWins = createErrorLabel();
        lblErrorLosses = createErrorLabel();
        lblErrorRankingLogic = createErrorLabel(); // Para validación interna del Ranking

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 3, SPACING_MD, SPACING_LG));
        fieldsPanel.setBackground(BG_CARD);

        txtPlayerId = createStyledTextField();
        txtPlayerName = createStyledTextField();
        txtGameName = createStyledTextField();
        txtRanking = createStyledTextField();
        txtWins = createStyledTextField();
        txtLosses = createStyledTextField();

        // Añadir Listeners a los campos relevantes
        txtPlayerId.getDocument().addDocumentListener(validationListener);
        txtPlayerName.getDocument().addDocumentListener(validationListener);
        txtGameName.getDocument().addDocumentListener(validationListener);
        txtRanking.getDocument().addDocumentListener(validationListener);
        txtWins.getDocument().addDocumentListener(validationListener);
        txtLosses.getDocument().addDocumentListener(validationListener);


        // Fila 1
        fieldsPanel.add(createValidatedFieldPanel("ID Jugador:", txtPlayerId, lblErrorPlayerId, true)); // ERROR VISIBLE
        fieldsPanel.add(createValidatedFieldPanel("Nombre del Jugador:", txtPlayerName, lblErrorPlayerName, true)); // ERROR VISIBLE
        fieldsPanel.add(createValidatedFieldPanel("Nombre del Juego:", txtGameName, lblErrorGameName, true)); // ERROR VISIBLE

        // Fila 2
        fieldsPanel.add(createValidatedFieldPanel("Ranking (Opcional):", txtRanking, lblErrorRankingLogic, false)); // ERROR OCULTO
        fieldsPanel.add(createValidatedFieldPanel("Victorias:", txtWins, lblErrorWins, true)); // ERROR VISIBLE
        fieldsPanel.add(createValidatedFieldPanel("Derrotas:", txtLosses, lblErrorLosses, true)); // ERROR VISIBLE

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

    // Se elimina el createFieldPanel simple, usando la versión validada.

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

        String[] columns = {"ID Jugador", "Nombre Jugador", "Nombre Juego", "Ranking", "Victorias", "Derrotas"};
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
            if (table != null && !e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedRanking();
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

    // --- LÓGICA DE DATOS Y VALIDACIÓN ---

    private void validateAllFields() {
        if (btnAdd == null || btnUpdate == null) return;

        boolean isValid = true;

        String idText = txtPlayerId.getText().trim();
        String playerName = txtPlayerName.getText().trim();
        String gameName = txtGameName.getText().trim();
        String rankingText = txtRanking.getText().trim();
        String winsText = txtWins.getText().trim();
        String lossesText = txtLosses.getText().trim();

        // 1. ID Jugador (Obligatorio)
        lblErrorPlayerId.setText(" ");
        if (idText.isEmpty()) {
            lblErrorPlayerId.setText("ID Jugador es obligatorio.");
            isValid = false;
        } else {
            try {
                int playerId = Integer.parseInt(idText);
                if (playerId <= 0) {
                    lblErrorPlayerId.setText("Debe ser un número positivo.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorPlayerId.setText("Debe ser un número entero válido.");
                isValid = false;
            }
        }

        // 2. Nombre del Jugador (Obligatorio)
        lblErrorPlayerName.setText(" ");
        if (playerName.isEmpty()) {
            lblErrorPlayerName.setText("Nombre Jugador es obligatorio.");
            isValid = false;
        } else if (playerName.length() < 3) {
            lblErrorPlayerName.setText("Mínimo 3 caracteres.");
            isValid = false;
        }
        // Nota: La existencia del jugador se comprueba en add/update para no ralentizar la escritura.

        // 3. Nombre del Juego (Obligatorio)
        lblErrorGameName.setText(" ");
        if (gameName.isEmpty()) {
            lblErrorGameName.setText("Nombre Juego es obligatorio.");
            isValid = false;
        } else if (gameName.length() < 2) {
            lblErrorGameName.setText("Mínimo 2 caracteres.");
            isValid = false;
        }

        // 4. Victorias (Obligatorio)
        lblErrorWins.setText(" ");
        if (winsText.isEmpty()) {
            lblErrorWins.setText("Victorias es obligatorio.");
            isValid = false;
        } else {
            try {
                int wins = Integer.parseInt(winsText);
                if (wins < 0) {
                    lblErrorWins.setText("Debe ser positivo o cero.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorWins.setText("Debe ser un número entero.");
                isValid = false;
            }
        }

        // 5. Derrotas (Obligatorio)
        lblErrorLosses.setText(" ");
        if (lossesText.isEmpty()) {
            lblErrorLosses.setText("Derrotas es obligatorio.");
            isValid = false;
        } else {
            try {
                int losses = Integer.parseInt(lossesText);
                if (losses < 0) {
                    lblErrorLosses.setText("Debe ser positivo o cero.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorLosses.setText("Debe ser un número entero.");
                isValid = false;
            }
        }

        // 6. Ranking (Opcional, pero se valida si se introduce)
        lblErrorRankingLogic.setText(" "); // Oculto, pero necesario para la lógica
        if (!rankingText.isEmpty()) {
            try {
                int ranking = Integer.parseInt(rankingText);
                if (ranking <= 0) {
                    lblErrorRankingLogic.setText("Ranking debe ser positivo.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorRankingLogic.setText("Ranking debe ser un número entero.");
                isValid = false;
            }
        }

        // Estado de botones
        boolean isRowSelected = (table != null && table.getSelectedRow() != -1);

        btnAdd.setEnabled(isValid && !isRowSelected);
        btnUpdate.setEnabled(isValid && isRowSelected);
        btnDelete.setEnabled(isRowSelected);

        // ID y Nombre Juego no son editables en modo Actualizar
        txtPlayerId.setEditable(!isRowSelected);
        txtGameName.setEditable(!isRowSelected);
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
            clearFields(); // Limpia los campos después de cargar la tabla
        } catch (SQLException e) {
            showError("Error al cargar rankings: " + e.getMessage(), "Error de Carga");
        }
    }

    private void loadSelectedRanking() {
        if (table == null) return;

        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtPlayerId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtPlayerName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtGameName.setText(tableModel.getValueAt(selectedRow, 2).toString());

            Object rankingValue = tableModel.getValueAt(selectedRow, 3);
            txtRanking.setText(rankingValue != null ? rankingValue.toString() : "");

            txtWins.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtLosses.setText(tableModel.getValueAt(selectedRow, 5).toString());

            // En modo edición, las claves no son editables
            txtPlayerId.setEditable(false);
            txtGameName.setEditable(false);

            validateAllFields();
        }
    }

    private void addRanking() {
        if (!btnAdd.isEnabled()) {
            showError("Corrige los errores en los campos antes de agregar.", "Validación Pendiente");
            return;
        }

        if (!validatePlayerExistence()) return;

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
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("duplicate key value")) {
                showError("Error: Ya existe un ranking para el par (Jugador ID, Nombre Juego). Esta combinación debe ser única.", "Error de Clave Compuesta");
            } else if (errorMessage.contains("violates foreign key constraint")) {
                showError("Error de Clave Foránea: Asegúrate de que el ID de Jugador y el Nombre de Juego existan.", "Error de Clave Foránea");
            } else if (errorMessage.contains("violates check constraint")) {
                showError("Error de Restricción: Los valores deben ser positivos o cero.", "Error de Restricción");
            } else {
                showError("Error al agregar: " + errorMessage, "Error SQL");
            }
        } catch (NumberFormatException e) {
            showError("Error de formato numérico: Los campos de números deben ser válidos.", "Error de Formato");
        }
    }

    private void updateRanking() {
        if (!btnUpdate.isEnabled()) {
            showError("Selecciona un ranking y corrige los errores antes de actualizar.", "Validación Pendiente");
            return;
        }

        if (!validatePlayerExistence()) return;

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
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("violates check constraint")) {
                showError("Error de Validación: Victorias, Derrotas y Ranking deben ser números positivos o cero.", "Error de Restricción");
            } else {
                showError("Error al actualizar: " + errorMessage, "Error SQL");
            }
        }
    }

    private void deleteRanking() {
        if (!btnDelete.isEnabled()) {
            showError("Selecciona un ranking de la tabla para eliminar.", "Error de Selección");
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
            } catch (NumberFormatException e) {
                showError("El ID del Jugador debe ser un número válido.", "Error de Formato");
            }
        }
    }

    private void clearFields() {
        txtPlayerId.setText("");
        txtPlayerName.setText("");
        txtGameName.setText("");
        txtRanking.setText("");
        txtWins.setText("");
        txtLosses.setText("");

        // Limpiar mensajes de error
        lblErrorPlayerId.setText(" ");
        lblErrorPlayerName.setText(" ");
        lblErrorGameName.setText(" ");
        lblErrorWins.setText(" ");
        lblErrorLosses.setText(" ");
        lblErrorRankingLogic.setText(" "); // Limpiar lógica interna

        txtPlayerId.setEditable(true);
        txtGameName.setEditable(true);

        if (table != null) table.clearSelection();
        validateAllFields();
    }

    private boolean validatePlayerExistence() {
        String playerName = txtPlayerName.getText().trim();
        try {
            if (!playerCRUD.playerExists(playerName)) {
                showError("El jugador '" + playerName + "' no existe en la base de datos.", "Jugador No Encontrado");
                txtPlayerName.requestFocus();
                // Forzamos el error visual para que el usuario vea qué campo falló
                lblErrorPlayerName.setText("Jugador no encontrado.");
                return false;
            }
        } catch (SQLException e) {
            showError("Error al verificar el jugador en la base de datos: " + e.getMessage(), "Error de BD");
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

        private Font getBoldFont(int size) {
            return new Font("Segoe UI", Font.BOLD, size);
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

    // Métodos para oscurecer/aclarar colores (para los botones)
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