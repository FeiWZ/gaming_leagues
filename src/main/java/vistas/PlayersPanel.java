package vistas;

import consultas.PlayerCRUD;
import tablas.Player;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import javax.swing.event.CaretListener;
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
    // Patrón de email simplificado para esta validación, aunque el patrón regex es más robusto
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^.+@.+\\..+$");

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

    // DECLARACIÓN DE LABELS DE ERROR
    private JLabel lblErrorFirstName, lblErrorLastName, lblErrorEmail, lblErrorAddress;
    private JLabel lblErrorBirthDate, lblErrorGender, lblErrorNationality;

    private Font getBodyFont(int size) { return new Font("Roboto", Font.PLAIN, size); }
    private Font getTitleFont(int size) { return new Font("Roboto Slab", Font.BOLD, size); }
    private Font getBoldFont(int size) { return new Font("Roboto", Font.BOLD, size); }
    private static final int RADIUS_SM = 8;

    private static Color darken(Color color, float factor) { return new Color(Math.max(0, (int)(color.getRed() * factor)), Math.max(0, (int)(color.getGreen() * factor)), Math.max(0, (int)(color.getBlue() * factor))); }
    private static Color brighten(Color color, float factor) { return new Color(Math.min(255, (int)(color.getRed() * factor)), Math.min(255, (int)(color.getGreen() * factor)), Math.min(255, (int)(color.getBlue() * factor))); }

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

    public PlayersPanel(Connection connection) {
        this.connection = connection;
        this.playerCRUD = new PlayerCRUD(connection);
        SDF.setLenient(false);

        // Inicializar componentes de entrada
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

        // Inicializar JLabels de error
        lblErrorFirstName = createErrorLabel();
        lblErrorLastName = createErrorLabel();
        lblErrorEmail = createErrorLabel();
        lblErrorAddress = createErrorLabel();
        lblErrorBirthDate = createErrorLabel();
        lblErrorGender = createErrorLabel();
        lblErrorNationality = createErrorLabel();

        // Asignar listeners a los campos de texto
        CaretListener listener = createCaretListener();
        txtFirstName.addCaretListener(listener);
        txtLastName.addCaretListener(listener);
        txtEmail.addCaretListener(listener);
        txtAddress.addCaretListener(listener);
        txtCustomGender.addCaretListener(listener);
        txtCustomNationality.addCaretListener(listener);

        // Asignar listeners a ComboBox y DateChooser
        cmbGender.addActionListener(e -> validateFields());
        cmbNationality.addActionListener(e -> validateFields());
        dateChooserBirthDate.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                validateFields();
            }
        });

        // Inicializar tabla y paneles
        initializeTableComponents();
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

        // Llamar a validateFields() una vez que los componentes están inicializados
        validateFields();
    }

    /**
     * Inicializa JTable y DefaultTableModel para que 'this.table' no sea null.
     */
    private void initializeTableComponents() {
        String[] columns = {"ID", "Nombre", "Apellido", "Género", "Dirección", "Nacionalidad", "Fecha Nac.", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
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

        JPanel fieldsContainer = new JPanel(new GridBagLayout());
        fieldsContainer.setBackground(BG_CARD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        cmbGender.addActionListener(e -> {
            boolean isOther = "Otro".equals(cmbGender.getSelectedItem());
            txtCustomGender.setVisible(isOther);
            if (isOther) txtCustomGender.requestFocus();
            validateFields(); // Revalidar al cambiar
            revalidate();
            repaint();
        });

        cmbNationality.addActionListener(e -> {
            boolean isOther = "Otro".equals(cmbNationality.getSelectedItem());
            txtCustomNationality.setVisible(isOther);
            if (isOther) txtCustomNationality.requestFocus();
            validateFields(); // Revalidar al cambiar
            revalidate();
            repaint();
        });


        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        gbc.gridx = 0; fieldsContainer.add(createValidatedFieldPanel("Nombre:", txtFirstName, lblErrorFirstName), gbc);
        gbc.gridx = 1; fieldsContainer.add(createValidatedFieldPanel("Apellido:", txtLastName, lblErrorLastName), gbc);
        gbc.gridx = 2; fieldsContainer.add(createValidatedFieldPanel("Email:", txtEmail, lblErrorEmail), gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, SPACING_MD, 0);
        fieldsContainer.add(createValidatedFieldPanel("Dirección:", txtAddress, lblErrorAddress), gbc);


        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, SPACING_MD, SPACING_MD);

        gbc.gridx = 0; fieldsContainer.add(createDatePanel("Fecha Nacimiento:", dateChooserBirthDate, lblErrorBirthDate), gbc);
        gbc.gridx = 1; fieldsContainer.add(createComboValidatedPanel("Género:", cmbGender, txtCustomGender, lblErrorGender), gbc);
        gbc.gridx = 2; fieldsContainer.add(createComboValidatedPanel("Nacionalidad:", cmbNationality, txtCustomNationality, lblErrorNationality), gbc);

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

    // Método para campos de texto con validación
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

    // Método para DateChooser con validación
    private JPanel createDatePanel(String labelText, JDateChooser dateChooser, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(dateChooser, BorderLayout.NORTH);
        contentPanel.add(errorLabel, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // Método para ComboBox con validación (maneja el campo auxiliar)
    private JPanel createComboValidatedPanel(String labelText, JComboBox<String> cmb, JTextField auxTxt, JLabel errorLabel) {
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

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(inputGroup, BorderLayout.NORTH);
        contentPanel.add(errorLabel, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
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

        JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getDateEditor();
        dateEditor.setFont(getBodyFont(FONT_SIZE_BODY));
        dateEditor.setForeground(TEXT_PRIMARY);
        dateEditor.setBackground(BG_INPUT);
        dateEditor.setCaretColor(ACCENT_PRIMARY);
        dateEditor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));

        dateChooser.setBorder(new EmptyBorder(0, 0, 0, 0));
        dateChooser.getJCalendar().setBackground(BG_INPUT);

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

        // JTable y tableModel inicializados en initializeTableComponents
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
        // Si el valor seleccionado es 'Otro', toma el valor del campo de texto auxiliar
        // Si el campo auxiliar está vacío, devuelve 'Otro' por defecto (check constraint lo maneja)
        if ("Otro".equals(selected)) {
            String custom = txtCustomGender.getText().trim();
            return custom.isEmpty() ? "Otro" : custom;
        }
        return selected;
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

        clearErrorMessages();
        validateFields(); // Revalidar para actualizar botones
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
        if (!validateFields(true)) return;

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
        if (!validateFields(false)) return;

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

    private void clearErrorMessages() {
        lblErrorFirstName.setText(" ");
        lblErrorLastName.setText(" ");
        lblErrorEmail.setText(" ");
        lblErrorAddress.setText(" ");
        lblErrorBirthDate.setText(" ");
        lblErrorGender.setText(" ");
        lblErrorNationality.setText(" ");
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

        clearErrorMessages();
        validateFields(); // Revalidar para actualizar botones
        revalidate();
        repaint();
    }

    // Sobrecarga para determinar el modo de operación
    private boolean validateFields() {
        return validateFields(selectedPlayerId == -1);
    }

    // Método principal de validación con las reglas CHECK
    private boolean validateFields(boolean isAdding) {
        boolean isValid = true;

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        String gender = getSelectedGender(); // Obtiene el valor del combo o del auxiliar
        String nationality = getSelectedNationality(); // Obtiene el valor del combo o del auxiliar
        Date birthDate = dateChooserBirthDate.getDate();

        // 1. First Name (NOT NULL + CHECK LENGTH >= 3)
        if (firstName.isEmpty()) {
            lblErrorFirstName.setText("El nombre es obligatorio.");
            isValid = false;
        } else if (firstName.length() < 3) {
            lblErrorFirstName.setText("Mínimo 3 caracteres.");
            isValid = false;
        } else {
            lblErrorFirstName.setText(" ");
        }

        // 2. Last Name (NOT NULL + CHECK LENGTH >= 3)
        if (lastName.isEmpty()) {
            lblErrorLastName.setText("El apellido es obligatorio.");
            isValid = false;
        } else if (lastName.length() < 3) {
            lblErrorLastName.setText("Mínimo 3 caracteres.");
            isValid = false;
        } else {
            lblErrorLastName.setText(" ");
        }

        // 3. Email (NOT NULL + CHECK LIKE '%@%.%')
        if (email.isEmpty()) {
            lblErrorEmail.setText("El email es obligatorio.");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            lblErrorEmail.setText("Formato de email inválido.");
            isValid = false;
        } else {
            lblErrorEmail.setText(" ");
        }

        // 4. Address (NOT NULL + CHECK LENGTH >= 10)
        // Se considera obligatorio ya que tiene NOT NULL y un check de longitud
        if (address.isEmpty()) {
            lblErrorAddress.setText("La dirección es obligatoria.");
            isValid = false;
        } else if (address.length() < 10) {
            lblErrorAddress.setText("Mínimo 10 caracteres.");
            isValid = false;
        } else {
            lblErrorAddress.setText(" ");
        }

        // 5. Gender (NOT NULL + CHECK IN ('M', 'F', 'Otro'))
        // El check en la DB es para los valores de M, F, Otro. Si se selecciona 'Otro' y se deja el texto en blanco,
        // el método getSelectedGender devuelve 'Otro', lo que es válido. Solo se verifica que el combo no esté nulo.
        if (cmbGender.getSelectedItem() == null) {
            lblErrorGender.setText("El género es obligatorio.");
            isValid = false;
        } else {
            lblErrorGender.setText(" ");
        }

        // 6. Nationality (NOT NULL + CHECK LENGTH >= 5)
        if (nationality.isEmpty()) {
            lblErrorNationality.setText("La nacionalidad es obligatoria.");
            isValid = false;
        } else if (nationality.length() < 5) {
            lblErrorNationality.setText("Mínimo 5 caracteres.");
            isValid = false;
        } else {
            lblErrorNationality.setText(" ");
        }

        // 7. Birthdate (NOT NULL + CHECK <= CURRENT_DATE)
        if (birthDate == null) {
            lblErrorBirthDate.setText("La fecha es obligatoria.");
            isValid = false;
        } else if (birthDate.after(new Date())) {
            lblErrorBirthDate.setText("La fecha no puede ser futura.");
            isValid = false;
        } else {
            lblErrorBirthDate.setText(" ");
        }

        // Actualización de botones
        btnAdd.setEnabled(isValid && isAdding);
        btnUpdate.setEnabled(isValid && !isAdding && table.getSelectedRow() != -1);
        btnDelete.setEnabled(table.getSelectedRow() != -1);

        return isValid;
    }

    private void handleSQLError(SQLException e) {
        String msg = e.getMessage();
        if (msg.contains("duplicate key") && msg.contains("email")) {
            showError("El Email ya está registrado (debe ser único).");
        } else if (msg.contains("violates check constraint")) {
            showError("Error de validación: Verifica la longitud de los campos o el formato.");
        }
        else {
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