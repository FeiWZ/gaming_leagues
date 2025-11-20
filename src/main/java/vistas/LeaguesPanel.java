package vistas;

import consultas.LeagueCRUD;
import tablas.League;
// Importar DesignConstants para usar el mismo estilo que GamesPanel
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// Importación de JCalendar
import com.toedter.calendar.JDateChooser;

public class LeaguesPanel extends JPanel {

    private Connection connection;
    private LeagueCRUD leagueCRUD;

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtLeagueName, txtLeagueDetails;
    private JTextArea txtRules;

    private JTextField txtPrizeAmount;
    private JComboBox<String> cmbPrizeCurrency;

    private JDateChooser dateChooserStartedDate;
    private JDateChooser dateChooserEndDate;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private JLabel lblErrorName, lblErrorDetails, lblErrorPrize, lblErrorRules, lblErrorStart, lblErrorEnd;
    private DocumentListener validationListener;

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private final DecimalFormat prizeFormat = new DecimalFormat("#,##0");

    private final String[] CURRENCIES = {"USD", "EUR", "MXN", "COP", "Otro"};

    public LeaguesPanel(Connection connection) {
        this.connection = connection;
        this.leagueCRUD = new LeagueCRUD(connection);
        SDF.setLenient(false);
        initComponents();
        loadLeagues();
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ACCENT_DANGER);
        // CORRECCIÓN: Usar getBoldFont en lugar del método getItalicFont que no existe.
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        return label;
    }

    private DocumentListener createValidationListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateAllFields();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateAllFields();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateAllFields();
            }
        };
    }

    private void initComponents() {
        setLayout(new BorderLayout(SPACING_MD, SPACING_MD));
        setBackground(BG_DARK_SECONDARY); // Fondo Oscuro
        setBorder(new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD));

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD); // Color de tarjeta
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG)
        ));

        lblErrorName = createErrorLabel();
        lblErrorDetails = createErrorLabel();
        lblErrorPrize = createErrorLabel();
        lblErrorRules = createErrorLabel();
        lblErrorStart = createErrorLabel();
        lblErrorEnd = createErrorLabel();
        validationListener = createValidationListener();

        // Panel de campos: 2 filas x 3 columnas (secciones)
        JPanel fieldsPanel = new JPanel(new GridLayout(2, 3, SPACING_MD, SPACING_LG));
        fieldsPanel.setBackground(BG_CARD);

        txtLeagueName = createStyledTextField();
        txtLeagueDetails = createStyledTextField();
        txtPrizeAmount = createStyledTextField();
        txtRules = createStyledTextArea(2, 20); // Usar JTextArea con el estilo dark

        txtLeagueName.getDocument().addDocumentListener(validationListener);
        txtLeagueDetails.getDocument().addDocumentListener(validationListener);
        txtPrizeAmount.getDocument().addDocumentListener(validationListener);
        txtRules.getDocument().addDocumentListener(validationListener);


        fieldsPanel.add(createValidatedFieldPanel("Nombre:", txtLeagueName, lblErrorName));
        fieldsPanel.add(createValidatedFieldPanel("Detalles:", txtLeagueDetails, lblErrorDetails));
        fieldsPanel.add(createPrizePanel());

        // SEGUNDA FILA: Reglas y Fechas (Se eliminó el panel vacío que causaba el espacio extra)
        fieldsPanel.add(createRulesPanel());
        fieldsPanel.add(createDatePanel("Fecha Inicio:", lblErrorStart, true));
        fieldsPanel.add(createDatePanel("Fecha Fin:", lblErrorEnd, false));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_MD, 0));
        buttonsPanel.setBackground(BG_CARD);
        buttonsPanel.setBorder(new EmptyBorder(SPACING_MD, 0, 0, 0));

        btnAdd = createStyledButton("Agregar", ACCENT_SUCCESS);
        btnUpdate = createStyledButton("Actualizar", ACCENT_WARNING);
        btnDelete = createStyledButton("Eliminar", ACCENT_DANGER);
        btnClear = createStyledButton("Limpiar", ACCENT_PRIMARY);

        btnAdd.addActionListener(e -> addLeague());
        btnUpdate.addActionListener(e -> updateLeague());
        btnDelete.addActionListener(e -> deleteLeague());
        btnClear.addActionListener(e -> clearFields());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        validateAllFields();
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

    private JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea textArea = new JTextArea(rows, cols);
        textArea.setFont(getBodyFont(FONT_SIZE_BODY));
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setBackground(BG_INPUT);
        textArea.setCaretColor(ACCENT_PRIMARY);
        textArea.setBorder(new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD));
        return textArea;
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

    private JPanel createPrizePanel() {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel("Premio (Monto):");
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        cmbPrizeCurrency = new JComboBox<>(CURRENCIES);
        cmbPrizeCurrency.setFont(getBodyFont(FONT_SIZE_BODY));
        cmbPrizeCurrency.setBackground(BG_INPUT);
        cmbPrizeCurrency.setForeground(TEXT_PRIMARY);
        cmbPrizeCurrency.addActionListener(e -> validateAllFields());

        txtPrizeAmount.setFont(getBodyFont(FONT_SIZE_BODY));
        txtPrizeAmount.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));

        JPanel inputPanel = new JPanel(new BorderLayout(SPACING_XS, 0));
        inputPanel.setBackground(BG_INPUT);

        ((JLabel)cmbPrizeCurrency.getRenderer()).setOpaque(true);
        ((JLabel)cmbPrizeCurrency.getRenderer()).setBackground(BG_INPUT);

        inputPanel.add(txtPrizeAmount, BorderLayout.CENTER);
        inputPanel.add(cmbPrizeCurrency, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(inputPanel, BorderLayout.NORTH);
        contentPanel.add(lblErrorPrize, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRulesPanel() {
        JPanel rulesPanel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        rulesPanel.setBackground(BG_CARD);

        JLabel rulesLabel = new JLabel("Reglas:");
        rulesLabel.setFont(getBoldFont(FONT_SIZE_SMALL));
        rulesLabel.setForeground(TEXT_SECONDARY);

        JScrollPane rulesScroll = new JScrollPane(txtRules);
        rulesScroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        rulesScroll.getViewport().setBackground(BG_INPUT);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(rulesScroll, BorderLayout.NORTH);
        contentPanel.add(lblErrorRules, BorderLayout.SOUTH);

        rulesPanel.add(rulesLabel, BorderLayout.NORTH);
        rulesPanel.add(contentPanel, BorderLayout.CENTER);
        return rulesPanel;
    }

    private JPanel createDatePanel(String labelText, JLabel errorLabel, boolean isStartDate) {
        JPanel panel = new JPanel(new BorderLayout(SPACING_XS, SPACING_XS));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(labelText);
        label.setFont(getBoldFont(FONT_SIZE_SMALL));
        label.setForeground(TEXT_SECONDARY);

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setFont(getBodyFont(FONT_SIZE_BODY));

        JTextField dateField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        dateField.setBackground(BG_INPUT);
        dateField.setForeground(TEXT_PRIMARY);
        dateField.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));

        if (isStartDate) {
            dateChooserStartedDate = dateChooser;
        } else {
            dateChooserEndDate = dateChooser;
        }

        dateChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                validateAllFields();
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_CARD);
        contentPanel.add(dateChooser, BorderLayout.NORTH);
        contentPanel.add(errorLabel, BorderLayout.SOUTH);

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

        JLabel titleLabel = new JLabel("Lista de Ligas");
        titleLabel.setFont(getTitleFont(FONT_SIZE_H2));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, SPACING_MD, 0));

        String[] columns = {"ID", "Nombre", "Detalles", "Premio", "Moneda", "Reglas", "Inicio", "Fin"};
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

        // Aplicar el Custom Renderer a la cabecera
        table.getTableHeader().setFont(getBoldFont(FONT_SIZE_BODY));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new CustomTableHeaderRenderer(BG_DARK_PRIMARY, Color.WHITE));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_PRIMARY));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedLeague();
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
            // Métodos placeholder asumidos en DesignConstants o una clase auxiliar
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

    private void validateAllFields() {
        boolean isValid = true;

        String name = txtLeagueName.getText().trim();
        if (name.isEmpty()) {
            lblErrorName.setText("El nombre es obligatorio.");
            isValid = false;
        } else if (name.length() < 5) {
            lblErrorName.setText("Mínimo 5 caracteres.");
            isValid = false;
        } else {
            lblErrorName.setText(" ");
        }

        String prizeAmountText = txtPrizeAmount.getText().trim();
        if (prizeAmountText.isEmpty()) {
            lblErrorPrize.setText("El monto del premio es obligatorio.");
            isValid = false;
        } else {
            try {
                int prizePool = Integer.parseInt(prizeAmountText);
                if (prizePool < 0) {
                    lblErrorPrize.setText("Debe ser un número positivo o cero.");
                    isValid = false;
                } else {
                    lblErrorPrize.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorPrize.setText("Debe ser un número entero válido.");
                isValid = false;
            }
        }

        String rules = txtRules.getText().trim();
        if (rules.isEmpty()) {
            lblErrorRules.setText("Las reglas son obligatorias.");
            isValid = false;
        } else if (rules.length() < 10) {
            lblErrorRules.setText("Mínimo 10 caracteres.");
            isValid = false;
        } else {
            lblErrorRules.setText(" ");
        }

        Date startDate = dateChooserStartedDate.getDate();
        Date endDate = dateChooserEndDate.getDate();

        lblErrorStart.setText(" ");
        lblErrorEnd.setText(" ");

        if (startDate == null) {
            lblErrorStart.setText("La fecha de inicio es obligatoria.");
            isValid = false;
        }

        if (endDate == null) {
            lblErrorEnd.setText("La fecha de fin es obligatoria.");
            isValid = false;
        }

        if (startDate != null && endDate != null) {
            if (endDate.before(startDate)) {
                lblErrorEnd.setText("Fecha Fin no puede ser anterior a la Fecha Inicio.");
                isValid = false;
            }
        }

        btnAdd.setEnabled(isValid);
        btnUpdate.setEnabled(isValid && table.getSelectedRow() != -1);
    }

    private void loadLeagues() {
        tableModel.setRowCount(0);
        try {
            List<League> leagues = leagueCRUD.getAllLeagues();
            for (League league : leagues) {
                // Se removió la variable prizeDisplay que no se usa en el array Object[]
                Object[] row = {
                        league.getLeagueId(),
                        league.getLeagueName(),
                        league.getLeagueDetails(),
                        prizeFormat.format(league.getPrizePool()),
                        "USD",
                        league.getRules(),
                        league.getStartedDate() != null ? SDF.format(league.getStartedDate()) : "",
                        league.getEndDate() != null ? SDF.format(league.getEndDate()) : ""
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error al cargar ligas: " + e.getMessage(), "Error de Carga");
        }
    }

    private void loadSelectedLeague() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            // String leagueId = tableModel.getValueAt(selectedRow, 0).toString(); // Ya no se necesita el ID aquí

            // Limpia el formato de premio (por si incluye comas) para usar el valor numérico
            String prizeAmountText = tableModel.getValueAt(selectedRow, 3).toString().replaceAll("[^0-9]", "");
            String currency = tableModel.getValueAt(selectedRow, 4).toString(); // Moneda

            txtLeagueName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtLeagueDetails.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtPrizeAmount.setText(prizeAmountText);
            cmbPrizeCurrency.setSelectedItem(currency);
            txtRules.setText(tableModel.getValueAt(selectedRow, 5).toString());

            try {
                Date startDate = SDF.parse(tableModel.getValueAt(selectedRow, 6).toString());
                Date endDate = SDF.parse(tableModel.getValueAt(selectedRow, 7).toString());
                dateChooserStartedDate.setDate(startDate);
                dateChooserEndDate.setDate(endDate);
            } catch (ParseException | NullPointerException e) {
                dateChooserStartedDate.setDate(null);
                dateChooserEndDate.setDate(null);
            }

            btnDelete.setEnabled(true);
            validateAllFields();
        }
    }

    private void addLeague() {
        if (!btnAdd.isEnabled()) {
            showError("Corrige los errores en los campos antes de agregar.", "Validación Pendiente");
            return;
        }

        try {
            int prizePool = Integer.parseInt(txtPrizeAmount.getText().trim());

            Date startDate = dateChooserStartedDate.getDate();
            Date endDate = dateChooserEndDate.getDate();

            League league = new League(
                    -1,
                    txtLeagueName.getText().trim(),
                    txtLeagueDetails.getText().trim(),
                    prizePool,
                    txtRules.getText().trim(),
                    startDate,
                    endDate
            );

            if (leagueCRUD.createLeague(league)) {
                showSuccess("Liga agregada exitosamente!");
                clearFields();
                loadLeagues();
            } else {
                showError("No se pudo agregar la liga.", "Error de Inserción");
            }
        } catch (NumberFormatException e) {
            showError("Error de formato numérico en Premio.", "Error de Formato");
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("violates check constraint")) {
                showError("Error: La Fecha de Fin debe ser igual o posterior a la Fecha de Inicio.", "Error de Restricción");
            } else {
                showError("Error al agregar: " + errorMessage, "Error SQL");
            }
        }
    }

    private void updateLeague() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Selecciona una liga de la tabla para actualizar.", "Error de Selección");
            return;
        }
        if (!btnUpdate.isEnabled()) {
            showError("Corrige los errores en los campos antes de actualizar.", "Validación Pendiente");
            return;
        }

        try {
            int leagueId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            int prizePool = Integer.parseInt(txtPrizeAmount.getText().trim());

            Date startDate = dateChooserStartedDate.getDate();
            Date endDate = dateChooserEndDate.getDate();

            League league = new League(
                    leagueId,
                    txtLeagueName.getText().trim(),
                    txtLeagueDetails.getText().trim(),
                    prizePool,
                    txtRules.getText().trim(),
                    startDate,
                    endDate
            );

            if (leagueCRUD.updateLeague(league)) {
                showSuccess("Liga actualizada exitosamente!");
                clearFields();
                loadLeagues();
            } else {
                showError("No se pudo actualizar la liga.", "Error de Actualización");
            }
        } catch (NumberFormatException e) {
            showError("Error de formato numérico en Premio o ID.", "Error de Formato");
        } catch (SQLException e) {
            showError("Error al actualizar: " + e.getMessage(), "Error SQL");
        }
    }

    private void deleteLeague() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Selecciona una liga de la tabla para eliminar.", "Error de Selección");
            return;
        }

        String leagueIdStr = tableModel.getValueAt(selectedRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar esta liga? Esto puede afectar a juegos y matches asociados.",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int leagueId = Integer.parseInt(leagueIdStr);
                if (leagueCRUD.deleteLeague(leagueId)) {
                    showSuccess("Liga eliminada exitosamente!");
                    clearFields();
                    loadLeagues();
                } else {
                    showError("No se pudo eliminar la liga.", "Error de Eliminación");
                }
            } catch (SQLException e) {
                String errorMessage = e.getMessage();
                if (errorMessage.contains("violates foreign key constraint")) {
                    showError("Error: No puedes eliminar esta liga porque tiene Juegos o Matches asociados.", "Error de Clave Foránea");
                } else {
                    showError("Error al eliminar: " + errorMessage, "Error SQL");
                }
            }
        }
    }

    private void clearFields() {
        txtLeagueName.setText("");
        txtLeagueDetails.setText("");
        txtPrizeAmount.setText("");
        txtRules.setText("");
        cmbPrizeCurrency.setSelectedIndex(0);

        dateChooserStartedDate.setDate(null);
        dateChooserEndDate.setDate(null);

        lblErrorName.setText(" ");
        lblErrorDetails.setText(" ");
        lblErrorPrize.setText(" ");
        lblErrorRules.setText(" ");
        lblErrorStart.setText(" ");
        lblErrorEnd.setText(" ");

        table.clearSelection();
        validateAllFields();
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // CLASE INTERNA PARA FORZAR EL ESTILO DE LA CABECERA (copiada de GamesPanel)
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

        // Método placeholder (asumimos que existe en DesignConstants o en la clase principal)
        private Font getBoldFont(int size) {
            // Se usa una fuente de respaldo para que compile
            return new Font("Segoe UI", Font.BOLD, size);
        }
    }
}