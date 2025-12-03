package vistas;

import com.toedter.calendar.JCalendar;
import consultas.PlayerCRUD;
import tablas.Player;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class PlayersPanel extends JPanel {

    private Connection connection;
    private PlayerCRUD playerCRUD;

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtFirstName, txtLastName, txtAddress, txtEmail;
    private JComboBox<String> cmbGender, cmbNationality;
    private JTextField txtCustomGender, txtCustomNationality;
    private JDateChooser dateChooserBirthDate;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private final String[] GENDERS = {"M", "F", "Otro"};
    private final String[] COMMON_NATIONALITIES = {"México", "Estados Unidos", "España", "Argentina", "Colombia", "Otro"};
    private int selectedPlayerId = -1;

    private Font getBodyFont(int size) { return new Font("Roboto", Font.PLAIN, size); }
    private Font getTitleFont(int size) { return new Font("Roboto Slab", Font.BOLD, size); }
    private Font getBoldFont(int size) { return new Font("Roboto", Font.BOLD, size); }
    private static final int RADIUS_SM = 8;

    private static Color darken(Color color, float factor) { return new Color(Math.max(0, (int)(color.getRed() * factor)), Math.max(0, (int)(color.getGreen() * factor)), Math.max(0, (int)(color.getBlue() * factor))); }
    private static Color brighten(Color color, float factor) { return new Color(Math.min(255, (int)(color.getRed() * factor)), Math.min(255, (int)(color.getGreen() * factor)), Math.min(255, (int)(color.getBlue() * factor))); }


    public PlayersPanel(Connection connection) {
        this.connection = connection;
        this.playerCRUD = new PlayerCRUD(connection);
        SDF.setLenient(false);

        txtFirstName = createStyledTextField();
        txtLastName = createStyledTextField();
        txtEmail = createStyledTextField();
        txtAddress = createStyledTextField();
        dateChooserBirthDate = createStyledDateChooser();
        cmbGender = createStyledComboBox(GENDERS);
        cmbNationality = createStyledComboBox(COMMON_NATIONALITIES);
        txtCustomGender = createStyledTextField();
        txtCustomGender.setVisible(false);
        txtCustomNationality = createStyledTextField();
        txtCustomNationality.setVisible(false);

        initComponents();
        loadPlayers();
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
        gbc.weightx = 1.0;

        cmbGender.addActionListener(e -> {
            boolean isOther = "Otro".equals(cmbGender.getSelectedItem());
            txtCustomGender.setVisible(isOther);
            if (isOther) txtCustomGender.requestFocus();
            revalidate();
            repaint();
        });

        cmbNationality.addActionListener(e -> {
            boolean isOther = "Otro".equals(cmbNationality.getSelectedItem());
            txtCustomNationality.setVisible(isOther);
            if (isOther) txtCustomNationality.requestFocus();
            revalidate();
            repaint();
        });


        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        gbc.gridx = 0; fieldsContainer.add(createFieldPanel("Nombre:", txtFirstName), gbc);
        gbc.gridx = 1; fieldsContainer.add(createFieldPanel("Apellido:", txtLastName), gbc);
        gbc.gridx = 2; fieldsContainer.add(createFieldPanel("Email:", txtEmail), gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(createFieldPanel("Dirección:", txtAddress), gbc);


        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        gbc.gridx = 0; fieldsContainer.add(createFieldPanel("Fecha Nacimiento:", dateChooserBirthDate), gbc);
        gbc.gridx = 1; fieldsContainer.add(createComboFieldPanel("Género:", cmbGender, txtCustomGender), gbc);
        gbc.gridx = 2; fieldsContainer.add(createComboFieldPanel("Nacionalidad:", cmbNationality, txtCustomNationality), gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(new JPanel() {{ setBackground(BG_CARD); }}, gbc);


        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Agregar", ACCENT_SUCCESS);
        btnUpdate = createStyledButton("Actualizar", ACCENT_WARNING);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addPlayer());
        btnUpdate.addActionListener(e -> updatePlayer());
        btnDelete.addActionListener(e -> deletePlayer());
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

    private JPanel createComboFieldPanel(String labelText, JComboBox<String> cmb, JTextField auxTxt) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel inputGroup = new JPanel();
        inputGroup.setLayout(new BoxLayout(inputGroup, BoxLayout.Y_AXIS));
        inputGroup.setOpaque(false);

        inputGroup.add(cmb);
        inputGroup.add(Box.createVerticalStrut(SPACING_XS));
        inputGroup.add(auxTxt);

        auxTxt.setMaximumSize(new Dimension(Integer.MAX_VALUE, auxTxt.getPreferredSize().height));

        panel.add(label, BorderLayout.NORTH);
        panel.add(inputGroup, BorderLayout.CENTER);
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

    private JDateChooser createStyledDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");

        dateChooser.setPreferredSize(new Dimension(150, 35));
        dateChooser.setMinimumSize(new Dimension(150, 35));

        JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getDateEditor();

        dateEditor.setFont(getBodyFont(FONT_SIZE_BODY));

        dateEditor.setForeground(Color.WHITE);

        dateEditor.setBackground(BG_INPUT);
        dateEditor.setCaretColor(ACCENT_PRIMARY);
        dateEditor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));

        Component editorComponent = dateChooser.getDateEditor().getUiComponent();
        if (editorComponent instanceof JTextField) {
            JTextField textField = (JTextField) editorComponent;
            textField.setPreferredSize(new Dimension(150, 35));
            textField.setMinimumSize(new Dimension(150, 35));
        }

        dateChooser.setBorder(new EmptyBorder(0, 0, 0, 0));

        JCalendar calendar = dateChooser.getJCalendar();
        calendar.setBackground(BG_INPUT);

        calendar.getDayChooser().getDayPanel().setBackground(BG_INPUT);
        calendar.getDayChooser().setForeground(Color.WHITE);
        calendar.getDayChooser().setBackground(BG_INPUT);
        calendar.getDayChooser().setDecorationBackgroundColor(BG_INPUT);
        calendar.getDayChooser().setDecorationBordersVisible(false);

        dateChooser.addPropertyChangeListener("date", evt -> {
            SwingUtilities.invokeLater(() -> {
                dateEditor.setForeground(Color.WHITE);
                if (editorComponent instanceof JTextField) {
                    ((JTextField) editorComponent).setForeground(Color.WHITE);
                }
            });
        });

        dateEditor.addPropertyChangeListener(evt -> {
            if ("foreground".equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(() -> dateEditor.setForeground(Color.WHITE));
            }
        });

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

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        JLabel titleLabel = new JLabel("Lista de Jugadores");
        titleLabel.setFont(getTitleFont(FONT_SIZE_H2));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, SPACING_MD, 0));

        String[] columns = {"ID", "Nombre", "Apellido", "Género", "Dirección", "Nacionalidad", "Fecha Nac.", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(getBodyFont(FONT_SIZE_BODY - 1));
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_INPUT);
        table.setRowHeight(32);
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
                loadSelectedPlayer();
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
        button.setPreferredSize(new Dimension(120, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private String getSelectedGender() {
        String selected = (String) cmbGender.getSelectedItem();
        return "Otro".equals(selected) ? txtCustomGender.getText().trim() : selected;
    }

    private String getSelectedNationality() {
        String selected = (String) cmbNationality.getSelectedItem();
        return "Otro".equals(selected) ? txtCustomNationality.getText().trim() : selected;
    }

    private void loadPlayers() {
        tableModel.setRowCount(0);
        try {
            List<Player> players = playerCRUD.getAllPlayers();
            for (Player player : players) {
                Object[] row = {
                        player.getPlayerId(),
                        player.getFirstName(),
                        player.getLastName(),
                        player.getGender(),
                        player.getAddress(),
                        player.getNationality(),
                        player.getBirthdate() != null ? SDF.format(player.getBirthdate()) : "",
                        player.getEmail()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error al cargar jugadores: " + e.getMessage());
        }
    }

    private void loadSelectedPlayer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            selectedPlayerId = -1;
            return;
        }

        Object idValue = tableModel.getValueAt(selectedRow, 0);
        selectedPlayerId = (idValue instanceof Integer) ? (Integer) idValue :
                (idValue != null ? Integer.parseInt(idValue.toString()) : -1);

        txtFirstName.setText(tableModel.getValueAt(selectedRow, 1).toString());
        txtLastName.setText(tableModel.getValueAt(selectedRow, 2).toString());
        String currentGender = tableModel.getValueAt(selectedRow, 3).toString();
        txtAddress.setText(tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString() : "");
        String currentNationality = tableModel.getValueAt(selectedRow, 5).toString();
        txtEmail.setText(tableModel.getValueAt(selectedRow, 7).toString());

        loadCustomValue(cmbGender, txtCustomGender, GENDERS, currentGender);
        loadCustomValue(cmbNationality, txtCustomNationality, COMMON_NATIONALITIES, currentNationality);

        try {
            Date birthDate = SDF.parse(tableModel.getValueAt(selectedRow, 6).toString());
            dateChooserBirthDate.setDate(birthDate);
        } catch (Exception e) {
            dateChooserBirthDate.setDate(null);
        }
    }

    private void loadCustomValue(JComboBox<String> cmb, JTextField txt, String[] commonValues, String currentValue) {
        boolean found = false;
        for (String val : commonValues) {
            if (val.equalsIgnoreCase(currentValue)) {
                cmb.setSelectedItem(val);
                txt.setText("");
                txt.setVisible(false);
                found = true;
                break;
            }
        }
        if (!found) {
            cmb.setSelectedItem("Otro");
            txt.setText(currentValue);
            txt.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void addPlayer() {
        if (!validateFields()) return;

        try {
            Player player = new Player(
                    0,
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    getSelectedGender(),
                    txtAddress.getText().trim().isEmpty() ? null : txtAddress.getText().trim(),
                    getSelectedNationality(),
                    dateChooserBirthDate.getDate(),
                    txtEmail.getText().trim()
            );

            if (playerCRUD.createPlayer(player)) {
                showSuccess("¡Jugador agregado exitosamente!");
                clearFields();
                loadPlayers();
            } else {
                showError("No se pudo agregar el jugador.");
            }
        } catch (SQLException e) {
            handleSQLError(e);
        }
    }

    private void updatePlayer() {
        if (selectedPlayerId == -1) {
            showError("Selecciona un jugador de la tabla para actualizar.");
            return;
        }
        if (!validateFields()) return;

        try {
            Player player = new Player(
                    selectedPlayerId,
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    getSelectedGender(),
                    txtAddress.getText().trim().isEmpty() ? null : txtAddress.getText().trim(),
                    getSelectedNationality(),
                    dateChooserBirthDate.getDate(),
                    txtEmail.getText().trim()
            );

            if (playerCRUD.updatePlayer(player)) {
                showSuccess("¡Jugador actualizado exitosamente!");
                clearFields();
                loadPlayers();
            } else {
                showError("No se pudo actualizar el jugador.");
            }
        } catch (SQLException e) {
            handleSQLError(e);
        }
    }

    private void deletePlayer() {
        if (selectedPlayerId == -1) {
            showError("Selecciona un jugador de la tabla para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de eliminar a este jugador?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (playerCRUD.deletePlayer(selectedPlayerId)) {
                    showSuccess("¡Jugador eliminado exitosamente!");
                    clearFields();
                    loadPlayers();
                } else {
                    showError("No se pudo eliminar el jugador.");
                }
            } catch (SQLException e) {
                showError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        selectedPlayerId = -1;
        txtFirstName.setText("");
        txtLastName.setText("");
        txtAddress.setText("");
        txtEmail.setText("");
        cmbGender.setSelectedIndex(0);
        txtCustomGender.setText("");
        txtCustomGender.setVisible(false);
        cmbNationality.setSelectedIndex(0);
        txtCustomNationality.setText("");
        txtCustomNationality.setVisible(false);
        dateChooserBirthDate.setDate(null);
        table.clearSelection();
        revalidate();
        repaint();
    }

    private boolean validateFields() {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String gender = getSelectedGender();
        String nationality = getSelectedNationality();
        Date birthDate = dateChooserBirthDate.getDate();

        if (firstName.isEmpty() || firstName.length() < 2) {
            showError("Nombre obligatorio (Mín. 2 caracteres).");
            return false;
        }
        if (lastName.isEmpty() || lastName.length() < 2) {
            showError("Apellido obligatorio (Mín. 2 caracteres).");
            return false;
        }
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            showError("Email inválido.");
            return false;
        }
        if (gender.isEmpty()) {
            showError("El género es obligatorio.");
            return false;
        }
        if (nationality.isEmpty() || nationality.length() < 2) {
            showError("Nacionalidad obligatoria (Mín. 2 caracteres).");
            return false;
        }
        if (birthDate == null) {
            showError("La fecha de nacimiento es obligatoria.");
            return false;
        }
        if (birthDate.after(new Date())) {
            showError("La fecha no puede ser futura.");
            return false;
        }
        return true;
    }

    private void handleSQLError(SQLException e) {
        String msg = e.getMessage();
        if (msg.contains("duplicate key") && msg.contains("email")) {
            showError("El Email ya está registrado.");
        } else {
            showError("Error: " + msg);
        }
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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
            return PlayersPanel.this.getBoldFont(size);
        }
    }
}