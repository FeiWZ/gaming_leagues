package vistas;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.view.JasperViewer;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.io.File;
import java.awt.Desktop;
import static vistas.DesignConstants.*;

public class InformesPanel extends JPanel {
    private Connection connection;

    public InformesPanel(Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout());
        setBackground(BG_DARK_SECONDARY);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        initComponents();
    }

    private void initComponents() {
        JLabel title = new JLabel("Generación de Informes");
        title.setFont(getTitleFont(FONT_SIZE_H2));
        title.setForeground(TEXT_PRIMARY);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(BG_DARK_SECONDARY);
        titlePanel.add(title);

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        cardsPanel.setBackground(BG_DARK_SECONDARY);

        cardsPanel.add(crearTarjeta("RANKING", "Top 15 Jugadores", "RankingJugadores"));
        cardsPanel.add(crearTarjeta("LIGAS", "Resumen de Premios", "ResumenLigas"));
        cardsPanel.add(crearTarjeta("EQUIPOS", "Estructura Equipos", "EstructuraEquipos"));

        add(titlePanel, BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(String titulo, String subtitulo, String reporte) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(withAlpha(BG_CARD, 150));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_PRIMARY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(280, 90));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(titulo);
        titleLabel.setFont(getBoldFont(FONT_SIZE_BODY + 2));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel(subtitulo);
        subLabel.setFont(getBodyFont(FONT_SIZE_BODY - 1));
        subLabel.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(card.getBackground());
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subLabel);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                generarReporte(reporte);
            }
        });

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private void generarReporte(String nombreReporte) {
        if (connection == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay conexión a la base de datos.\nConéctate primero desde el menú principal.",
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProgressDialog progress = new ProgressDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Generando Reporte"
        );

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                String ruta = "src/main/informes/" + nombreReporte + ".jasper";
                File archivo = new File(ruta);

                if (!archivo.exists()) {
                    throw new Exception("Archivo no encontrado: " + ruta);
                }

                return JasperFillManager.fillReport(archivo.getAbsolutePath(), null, connection);
            }

            @Override
            protected void done() {
                progress.dispose();
                try {
                    JasperPrint print = get();
                    mostrarOpcionesOriginal(print);
                } catch (Exception e) {
                    mostrarError("Error: " + e.getMessage());
                }
            }
        };

        worker.execute();
        progress.setVisible(true);
    }

    private void mostrarOpcionesOriginal(JasperPrint print) {
        SwingUtilities.invokeLater(() -> {
            String[] opciones = {"Ver en pantalla", "Exportar a PDF", "Cancelar"};
            int opcion = JOptionPane.showOptionDialog(this,
                    "¿Cómo deseas ver el reporte?",
                    "Opciones de Reporte",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, opciones, opciones[0]);

            switch (opcion) {
                case 0:
                    mostrarPantalla(print);
                    break;
                case 1:
                    exportarPDF(print);
                    break;
                case 2:
                    break;
            }
        });
    }

    private void mostrarPantalla(JasperPrint print) {
        SwingUtilities.invokeLater(() -> {
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("GameLeagues - Reporte");
            viewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            viewer.setVisible(true);
        });
    }

    private void exportarPDF(JasperPrint print) {
        try {
            File carpeta = new File("reportes_pdf");
            if (!carpeta.exists()) carpeta.mkdirs();

            String fecha = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String archivo = "reportes_pdf/Reporte_" + fecha + ".pdf";

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(archivo));

            SimplePdfExporterConfiguration config = new SimplePdfExporterConfiguration();
            config.setCompressed(true);
            exporter.setConfiguration(config);
            exporter.exportReport();

            int respuesta = JOptionPane.showConfirmDialog(this,
                    "✅ PDF generado: " + archivo + "\n\n¿Abrir archivo?",
                    "PDF Generado", JOptionPane.YES_NO_OPTION);

            if (respuesta == JOptionPane.YES_OPTION) {
                File pdf = new File(archivo);
                if (Desktop.isDesktopSupported() && pdf.exists()) {
                    Desktop.getDesktop().open(pdf);
                }
            }

        } catch (Exception e) {
            mostrarError("Error al exportar PDF: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private class ProgressDialog extends JDialog {
        private JLabel mensaje;

        public ProgressDialog(JFrame parent, String titulo) {
            super(parent, titulo, true);
            setSize(350, 120);
            setLocationRelativeTo(parent);
            setResizable(false);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);

            mensaje = new JLabel("Generando reporte...");
            mensaje.setFont(new Font("Arial", Font.PLAIN, 14));

            JProgressBar barra = new JProgressBar();
            barra.setIndeterminate(true);

            panel.add(mensaje, BorderLayout.NORTH);
            panel.add(barra, BorderLayout.CENTER);
            add(panel);
        }

        public void setMessage(String texto) {
            mensaje.setText(texto);
        }
    }
}