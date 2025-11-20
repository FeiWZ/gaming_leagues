package vistas;

import basedatos.ConexionBD;
import static vistas.DesignConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

public class PantallaInicio extends JFrame {

    private JLabel etiquetaTitulo;
    private JLabel etiquetaSubtitulo;
    private JButton botonEntrar;
    private JPanel panelPrincipal;
    private JPanel panelCentral;

    public PantallaInicio() {
        setTitle("GameLeagues");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        panelPrincipal = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, BG_DARK_PRIMARY,
                        0, getHeight(), BG_DARK_SECONDARY
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelPrincipal.setLayout(new GridBagLayout());

        panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(60, 60, 60, 60));

        JLabel iconoGamepad = new JLabel("🎮");
        iconoGamepad.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 90));
        iconoGamepad.setAlignmentX(Component.CENTER_ALIGNMENT);

        etiquetaTitulo = new JLabel("GAME LEAGUES");
        etiquetaTitulo.setFont(getTitleFont(48));
        etiquetaTitulo.setForeground(TEXT_PRIMARY);
        etiquetaTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        etiquetaSubtitulo = new JLabel("Sistema de Gestión de Ligas y Torneos");
        etiquetaSubtitulo.setFont(getBodyFont(16));
        etiquetaSubtitulo.setForeground(TEXT_SECONDARY);
        etiquetaSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        botonEntrar = new JButton("ENTRAR AL SISTEMA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(darken(ACCENT_PRIMARY, 0.8f));
                } else if (getModel().isRollover()) {
                    g2d.setColor(brighten(ACCENT_PRIMARY, 1.1f));
                } else {
                    g2d.setColor(ACCENT_PRIMARY);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_MD, RADIUS_MD);

                g2d.setColor(BG_DARK_PRIMARY);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        botonEntrar.setFont(getBoldFont(16));
        botonEntrar.setForeground(BG_DARK_PRIMARY);
        botonEntrar.setFocusPainted(false);
        botonEntrar.setBorderPainted(false);
        botonEntrar.setContentAreaFilled(false);
        botonEntrar.setPreferredSize(new Dimension(280, 55));
        botonEntrar.setMaximumSize(new Dimension(280, 55));
        botonEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        botonEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel textoInfo = new JLabel("Presiona el botón para conectar a la base de datos");
        textoInfo.setFont(getBodyFont(12));
        textoInfo.setForeground(TEXT_DISABLED);
        textoInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelIntegrantes = new JPanel();
        panelIntegrantes.setLayout(new BoxLayout(panelIntegrantes, BoxLayout.Y_AXIS));
        panelIntegrantes.setOpaque(false);
        panelIntegrantes.setMaximumSize(new Dimension(700, 80));

        JLabel tituloIntegrantes = new JLabel("Desarrollado por:");
        tituloIntegrantes.setFont(getBoldFont(12));
        tituloIntegrantes.setForeground(TEXT_SECONDARY);
        tituloIntegrantes.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel integrante1 = new JLabel("• Sánchez Valenzuela • Sandoval López • Wu Zhang");
        integrante1.setFont(getBodyFont(13));
        integrante1.setForeground(TEXT_SECONDARY);
        integrante1.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelIntegrantes.add(tituloIntegrantes);
        panelIntegrantes.add(Box.createRigidArea(new Dimension(0, 8)));
        panelIntegrantes.add(integrante1);

        panelCentral.add(iconoGamepad);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 25)));
        panelCentral.add(etiquetaTitulo);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 10)));
        panelCentral.add(etiquetaSubtitulo);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 50)));
        panelCentral.add(botonEntrar);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));
        panelCentral.add(textoInfo);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 50)));
        panelCentral.add(panelIntegrantes);

        panelPrincipal.add(panelCentral);
        add(panelPrincipal);

        botonEntrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botonEntrar.setEnabled(false);
                botonEntrar.setText("CONECTANDO...");

                SwingWorker<Connection, Void> worker = new SwingWorker<Connection, Void>() {
                    @Override
                    protected Connection doInBackground() throws Exception {
                        Connection connection = ConexionBD.conectar();
                        if (connection == null) {
                            throw new SQLException("No se pudo establecer la conexión a la base de datos.");
                        }
                        return connection;
                    }

                    @Override
                    protected void done() {
                        try {
                            Connection connection = get();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    VentanaPrincipal ventanaPrincipal = new VentanaPrincipal(connection);
                                    ventanaPrincipal.setVisible(true);
                                    dispose();
                                }
                            });
                        } catch (Exception ex) {
                            botonEntrar.setEnabled(true);
                            botonEntrar.setText("ENTRAR AL SISTEMA");
                            JOptionPane.showMessageDialog(
                                    PantallaInicio.this,
                                    "Error al conectar a la base de datos:\n" + ex.getMessage(),
                                    "Error de Conexión",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                };
                worker.execute();
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PantallaInicio().setVisible(true);
            }
        });
    }
}