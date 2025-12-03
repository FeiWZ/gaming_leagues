package vistas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import static vistas.DesignConstants.*;

import consultas.GameCRUD;
import vistas.InformesPanel;

public class VentanaPrincipal extends JFrame {

    private JPanel panelPrincipal;
    private JPanel panelEncabezado;
    private JLabel etiquetaTitulo;
    private JLabel etiquetaSubtitulo;
    private JTabbedPane tabbedPane;
    private Connection connection;
    private GameCRUD gameCRUD;

    public VentanaPrincipal(Connection connection) {
        setTitle("Game Leagues - Sistema de Gestión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        this.connection = connection;
        this.gameCRUD = new GameCRUD(connection);

        inicializarComponentes();

        try {
            cargarPaneles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al crear los paneles: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void inicializarComponentes() {
        panelPrincipal = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                GradientPaint gradient = new GradientPaint(
                        0, 0, BG_DARK_PRIMARY,
                        0, getHeight(), BG_DARK_SECONDARY
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelPrincipal.setLayout(new BorderLayout());

        panelEncabezado = crearPanelEncabezado();
        tabbedPane = crearTabbedPanePersonalizado();

        panelPrincipal.add(panelEncabezado, BorderLayout.NORTH);
        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);

        add(panelPrincipal);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(SPACING_LG, SPACING_XL, SPACING_LG, SPACING_XL));

        JPanel panelTitulos = new JPanel();
        panelTitulos.setLayout(new BoxLayout(panelTitulos, BoxLayout.Y_AXIS));
        panelTitulos.setOpaque(false);

        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, SPACING_MD, 0));
        panelTitulo.setOpaque(false);

        JLabel iconoTitulo = new JLabel("🎮");
        iconoTitulo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        etiquetaTitulo = new JLabel("GAME LEAGUES");
        etiquetaTitulo.setFont(getTitleFont(FONT_SIZE_HERO - 8));
        etiquetaTitulo.setForeground(TEXT_PRIMARY);

        panelTitulo.add(iconoTitulo);
        panelTitulo.add(etiquetaTitulo);

        etiquetaSubtitulo = new JLabel("Sistema de Gestión de Ligas");
        etiquetaSubtitulo.setFont(getBodyFont(FONT_SIZE_BODY));
        etiquetaSubtitulo.setForeground(TEXT_SECONDARY);
        etiquetaSubtitulo.setBorder(new EmptyBorder(SPACING_XS, SPACING_XS, 0, 0));

        panelTitulos.add(panelTitulo);
        panelTitulos.add(etiquetaSubtitulo);

        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setOpaque(false);

        JLabel estadoConexion = new JLabel("● Conectado");
        estadoConexion.setFont(getBoldFont(FONT_SIZE_SMALL + 1));
        estadoConexion.setForeground(ACCENT_SUCCESS);
        estadoConexion.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JButton botonSalir = new JButton(" SALIR ");

        botonSalir.setFont(getBoldFont(FONT_SIZE_BODY + 2));

        botonSalir.setForeground(Color.BLACK);
        botonSalir.setBackground(Color.WHITE);

        botonSalir.setFocusPainted(false);
        botonSalir.setBorder(BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD));
        botonSalir.setAlignmentX(Component.RIGHT_ALIGNMENT);

        botonSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmacion = JOptionPane.showConfirmDialog(VentanaPrincipal.this,
                        "¿Estás seguro de que quieres cerrar la aplicación?",
                        "Confirmar Salida",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        panelInfo.add(estadoConexion);
        panelInfo.add(Box.createVerticalStrut(SPACING_XS));
        panelInfo.add(botonSalir);

        panel.add(panelTitulos, BorderLayout.WEST);
        panel.add(panelInfo, BorderLayout.EAST);

        JSeparator separador = new JSeparator();
        separador.setForeground(BORDER_LIGHT);
        panel.add(separador, BorderLayout.SOUTH);

        return panel;
    }

    private JTabbedPane crearTabbedPanePersonalizado() {
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(getBoldFont(FONT_SIZE_H3));
        tabPane.setBackground(BG_DARK_SECONDARY);
        tabPane.setForeground(TEXT_PRIMARY);
        tabPane.setTabPlacement(JTabbedPane.TOP);
        tabPane.setBorder(new EmptyBorder(SPACING_MD, SPACING_XL, SPACING_XL, SPACING_XL));

        tabPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2d.setColor(ACCENT_PRIMARY);
                    g2d.fillRoundRect(x, y, w, h - 2, RADIUS_SM, RADIUS_SM);
                } else {
                    g2d.setColor(withAlpha(BG_CARD, 100));
                    g2d.fillRoundRect(x, y, w, h - 2, RADIUS_SM, RADIUS_SM);
                }
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                               int tabIndex, Rectangle iconRect, Rectangle textRect,
                                               boolean isSelected) {
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            }

            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return 42;
            }

            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 35;
            }
        });

        return tabPane;
    }

    private void cargarPaneles() {
        GamesPanel gamesPanel = new GamesPanel(connection);
        LeaguesPanel leaguesPanel = new LeaguesPanel(connection);
        PlayersPanel playersPanel = new PlayersPanel(connection);
        RankingsPanel rankingsPanel = new RankingsPanel(connection);
        TeamsPanel teamsPanel = new TeamsPanel(connection);
        MatchesPanel matchesPanel = new MatchesPanel(connection, this.gameCRUD);

        InformesPanel informesPanel = new InformesPanel(connection);

        tabbedPane.addTab(" Games", gamesPanel);
        tabbedPane.addTab(" Leagues", leaguesPanel);
        tabbedPane.addTab(" Players", playersPanel);
        tabbedPane.addTab(" Rankings", rankingsPanel);
        tabbedPane.addTab(" Teams", teamsPanel);
        tabbedPane.addTab(" Matches", matchesPanel);

        tabbedPane.addTab(" Informes", informesPanel);

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setForegroundAt(i, TEXT_PRIMARY);
            tabbedPane.setBackgroundAt(i, BG_DARK_SECONDARY);
        }
    }

    public void actualizarEstadoConexion(boolean conectado) {
        SwingUtilities.invokeLater(() -> {
            JPanel panelInfo = (JPanel) panelEncabezado.getComponent(1);
            Component[] components = panelInfo.getComponents();

            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if (conectado) {
                        label.setText("● Conectado");
                        label.setForeground(ACCENT_SUCCESS);
                    } else {
                        label.setText("● Desconectado");
                        label.setForeground(ACCENT_DANGER);
                    }
                    break;
                }
            }
        });
    }
}