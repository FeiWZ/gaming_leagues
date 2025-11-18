package org.example.vistas;

import org.example.consultas.RankingPlayerGameCRUD;
import org.example.tablas.RankingPlayerGame;
import static org.example.vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RankingsPanel extends JPanel {

    private Connection connection;
    private RankingPlayerGameCRUD rankingCRUD;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtPlayerId, txtGameCode, txtRanking, txtWins, txtLosses;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public RankingsPanel(Connection connection) {
        this.connection = connection;
        this.rankingCRUD = new RankingPlayerGameCRUD(connection);
        initComponents();
        loadRankings();
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

        JPanel fieldsPanel = new JPanel(new GridLayout(1, 5, SPACING_MD, SPACING_LG));
        fieldsPanel.setBackground(BG_CARD);

        txtPlayerId = createStyledTextField();
        txtGameCode = createStyledTextField();
        txtRanking = createStyledTextField();
        txtWins = createStyledTextField();
        txtLosses = createStyledTextField();

        fieldsPanel.add(createFieldPanel("ID Jugador:", txtPlayerId));
        fieldsPanel.add(createFieldPanel("Código Juego:", txtGameCode));
        fieldsPanel.add(createFieldPanel("Ranking (Opcional):", txtRanking));
        fieldsPanel.add(createFieldPanel("Victorias:", txtWins));
        fieldsPanel.add(createFieldPanel("Derrotas:", txtLosses));

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

        String[] columns = {"ID Jugador", "Código Juego", "Ranking", "Victorias", "Derrotas"};
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
                        ranking.getGameCode(),
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
            txtGameCode.setText(tableModel.getValueAt(selectedRow, 1).toString());

            Object rankingValue = tableModel.getValueAt(selectedRow, 2);
            txtRanking.setText(rankingValue != null ? rankingValue.toString() : "");

            txtWins.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtLosses.setText(tableModel.getValueAt(selectedRow, 4).toString());

            txtPlayerId.setEditable(false);
            txtGameCode.setEditable(false);

            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
            btnAdd.setEnabled(false);
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
                    txtGameCode.getText().trim(),
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

            if (errorMessage.contains("duplicate key value") && errorMessage.contains("ranking_player_game_pkey")) {
                showError("Error: Ya existe un ranking para el par (Jugador ID, Código Juego). Esta combinación debe ser única.", "Error de Clave Compuesta");
            } else if (errorMessage.contains("violates foreign key constraint")) {
                if (errorMessage.contains("player_id")) {
                    showError("Error de Jugador: El ID de Jugador ingresado no existe en la tabla de Jugadores.", "Error de Clave Foránea");
                } else if (errorMessage.contains("game_code")) {
                    showError("Error de Juego: El Código de Juego ingresado no existe en la tabla de Juegos.", "Error de Clave Foránea");
                } else {
                    showError("Error de Clave Foránea: Asegúrate de que el ID de Jugador y el Código de Juego existan.", "Error de Clave Foránea");
                }
            } else if (errorMessage.contains("violates check constraint")) {
                showError("Error de Validación: Victorias, Derrotas y Ranking deben ser números positivos o cero.", "Error de Restricción");
            } else {
                showError("Error al agregar: " + errorMessage, "Error SQL");
            }
        }
    }

    private void updateRanking() {
        if (txtPlayerId.getText().trim().isEmpty() || txtGameCode.getText().trim().isEmpty()) {
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
                    txtGameCode.getText().trim(),
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
        if (txtPlayerId.getText().trim().isEmpty() || txtGameCode.getText().trim().isEmpty()) {
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
                        txtGameCode.getText().trim())) {
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

    private void clearFields() {
        txtPlayerId.setText("");
        txtGameCode.setText("");
        txtRanking.setText("");
        txtWins.setText("");
        txtLosses.setText("");
        txtPlayerId.setEditable(true);
        txtGameCode.setEditable(true);
        table.clearSelection();

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    private boolean validateFields(boolean isAdd) {
        String idText = txtPlayerId.getText().trim();
        String gameCode = txtGameCode.getText().trim();
        String rankingText = txtRanking.getText().trim();
        String winsText = txtWins.getText().trim();
        String lossesText = txtLosses.getText().trim();

        if (idText.isEmpty() || gameCode.isEmpty() || winsText.isEmpty() || lossesText.isEmpty()) {
            showError("ID Jugador, Código Juego, Victorias y Derrotas son obligatorios.", "Campos Incompletos");
            return false;
        }

        try {
            int playerId = Integer.parseInt(idText);
            if (playerId <= 0) {
                showError("El ID del Jugador debe ser un número entero positivo.", "Error de Formato");
                txtPlayerId.requestFocus();
                return false;
            }

            int wins = Integer.parseInt(winsText);
            if (wins < 0) {
                showError("Victorias debe ser un número entero mayor o igual a cero.", "Error de Formato");
                txtWins.requestFocus();
                return false;
            }

            int losses = Integer.parseInt(lossesText);
            if (losses < 0) {
                showError("Derrotas debe ser un número entero mayor o igual a cero.", "Error de Formato");
                txtLosses.requestFocus();
                return false;
            }

            if (!rankingText.isEmpty()) {
                int ranking = Integer.parseInt(rankingText);
                if (ranking <= 0) {
                    showError("Ranking, si se ingresa, debe ser un número entero positivo.", "Error de Formato");
                    txtRanking.requestFocus();
                    return false;
                }
            }

        } catch (NumberFormatException e) {
            showError("ID Jugador, Ranking, Victorias y Derrotas deben ser números enteros válidos.", "Error de Formato");
            if (!idText.matches("\\d+")) txtPlayerId.requestFocus();
            else if (!winsText.matches("\\d+")) txtWins.requestFocus();
            else if (!lossesText.matches("\\d+")) txtLosses.requestFocus();
            else if (!rankingText.isEmpty() && !rankingText.matches("\\d+")) txtRanking.requestFocus();
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
}