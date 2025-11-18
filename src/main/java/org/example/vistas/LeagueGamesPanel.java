package org.example.vistas;

import org.example.consultas.GameLeagueCRUD;
import org.example.tablas.GameLeague;
import static org.example.vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LeagueGamesPanel extends JPanel {

    private Connection connection;
    private GameLeagueCRUD leagueGameCRUD;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtLeagueId, txtGameCode;

    private CaretListener validationListener;

    private JButton btnAdd, btnDelete, btnClear, btnRefresh;

    private Font getBodyFont(int size) { return new Font("Roboto", Font.PLAIN, size); }
    private Font getTitleFont(int size) { return new Font("Roboto Slab", Font.BOLD, size); }
    private Font getBoldFont(int size) { return new Font("Roboto", Font.BOLD, size); }


    public LeagueGamesPanel(Connection connection) {
        this.connection = connection;
        this.leagueGameCRUD = new GameLeagueCRUD(connection);

        validationListener = e -> updateButtonStates();

        initComponents();
    }

    public void initializePanelData() {
        loadLeagueGames();
        updateButtonStates();
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


        JPanel fieldsPanel = new JPanel(new GridLayout(1, 2, SPACING_LG, SPACING_MD));
        fieldsPanel.setBackground(BG_CARD);
        fieldsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        txtLeagueId = createStyledTextField();
        txtGameCode = createStyledTextField();

        txtLeagueId.addCaretListener(validationListener);
        txtGameCode.addCaretListener(validationListener);

        fieldsPanel.add(createFieldPanel("ID de la Liga:", txtLeagueId));
        fieldsPanel.add(createFieldPanel("Código del Juego:", txtGameCode));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Asociar", ACCENT_SUCCESS);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);
        btnRefresh = createStyledButton("Refrescar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addLeagueGame());
        btnDelete.addActionListener(e -> deleteLeagueGame());
        btnClear.addActionListener(e -> clearFields());
        btnRefresh.addActionListener(e -> loadLeagueGames());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);
        buttonsPanel.add(btnRefresh);

        JPanel centerPanel = new JPanel(new BorderLayout(0, SPACING_MD));
        centerPanel.setBackground(BG_CARD);
        centerPanel.add(fieldsPanel, BorderLayout.NORTH);

        panel.add(centerPanel, BorderLayout.CENTER);
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
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedLeagueGame();
                updateButtonStates();
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
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtLeagueId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtGameCode.setText(tableModel.getValueAt(selectedRow, 1).toString());
        }
    }

    private void addLeagueGame() {
        if (!validateAndShowErrors()) return;
    }

    private void deleteLeagueGame() {
    }

    private void clearFields() {
        txtLeagueId.setText("");
        txtGameCode.setText("");

        if (table != null) {
            table.clearSelection();
        }
        updateButtonStates();
    }


    private boolean validateAndShowErrors() {
        String leagueIdText = txtLeagueId.getText().trim();
        String gameCode = txtGameCode.getText().trim();

        if (leagueIdText.isEmpty() || gameCode.isEmpty()) {
            showError("Ambos campos (ID Liga y Código Juego) son obligatorios.", "Error de Validación");
            return false;
        }

        try {
            if (Integer.parseInt(leagueIdText) <= 0) {
                showError("El ID de la Liga debe ser un número positivo.", "Error de Validación");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("El ID de la Liga debe ser un valor numérico entero.", "Error de Validación");
            return false;
        }

        return true;
    }


    private void updateButtonStates() {
        if (table == null || btnAdd == null || btnDelete == null || btnRefresh == null) {
            return;
        }

        String leagueIdText = txtLeagueId.getText().trim();
        String gameCode = txtGameCode.getText().trim();

        boolean isValidId = false;
        try {
            int id = Integer.parseInt(leagueIdText);
            if (id > 0) isValidId = true;
        } catch (NumberFormatException e) {
            isValidId = false;
        }

        boolean fieldsAreValid = isValidId && !gameCode.isEmpty();
        boolean isSelected = table.getSelectedRow() != -1;

        btnAdd.setEnabled(fieldsAreValid && !isSelected);
        btnDelete.setEnabled(isSelected);
        btnRefresh.setEnabled(true);
    }


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
            return LeagueGamesPanel.this.getBoldFont(size);
        }
    }
}