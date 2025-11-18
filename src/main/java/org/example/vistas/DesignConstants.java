package org.example.vistas;

import java.awt.*;


public class DesignConstants {

    public static final Color BG_DARK_PRIMARY = new Color(0x0F1419);
    public static final Color BG_DARK_SECONDARY = new Color(0x1A1F2E);
    public static final Color BG_CARD = new Color(0x242B3D);
    public static final Color BG_INPUT = new Color(0x2A3142);

    public static final Color ACCENT_PRIMARY = new Color(0x00D9FF);
    public static final Color ACCENT_SECONDARY = new Color(0xFF006E);
    public static final Color ACCENT_SUCCESS = new Color(0x00F5A0);
    public static final Color ACCENT_WARNING = new Color(0xFFBD00);
    public static final Color ACCENT_DANGER = new Color(0xFF3D71);

    public static final Color TEXT_PRIMARY = new Color(0xFFFFFF);
    public static final Color TEXT_SECONDARY = new Color(0xA0AEC0);
    public static final Color TEXT_DISABLED = new Color(0x4A5568);

    public static final Color BORDER_LIGHT = new Color(0x2D3748);
    public static final Color BORDER_ACCENT = new Color(0, 217, 255, 50);

    public static final Color HOVER_OVERLAY = new Color(255, 255, 255, 10);

    public static final String FONT_FAMILY_TITLE = "Exo 2";
    public static final String FONT_FAMILY_BODY = "Inter";
    public static final String FONT_FAMILY_FALLBACK = "Segoe UI";

    public static final int FONT_SIZE_HERO = 42;
    public static final int FONT_SIZE_H1 = 24;
    public static final int FONT_SIZE_H2 = 18;
    public static final int FONT_SIZE_H3 = 14;
    public static final int FONT_SIZE_BODY = 13;
    public static final int FONT_SIZE_SMALL = 11;


    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 12;
    public static final int SPACING_LG = 16;
    public static final int SPACING_XL = 24;
    public static final int SPACING_XXL = 32;


    public static final int RADIUS_SM = 6;
    public static final int RADIUS_MD = 10;
    public static final int RADIUS_LG = 14;


    public static Font getTitleFont(int size) {
        return new Font(FONT_FAMILY_FALLBACK, Font.BOLD, size);
    }

    public static Font getBodyFont(int size) {
        return new Font(FONT_FAMILY_FALLBACK, Font.PLAIN, size);
    }

    public static Font getBoldFont(int size) {
        return new Font(FONT_FAMILY_FALLBACK, Font.BOLD, size);
    }


    public static Color withAlpha(Color color, int alpha) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0, Math.min(255, alpha))
        );
    }


    public static Color colorWithAlpha(int rgb, int alpha) {
        return new Color(
                (rgb >> 16) & 0xFF,
                (rgb >> 8) & 0xFF,
                rgb & 0xFF,
                Math.max(0, Math.min(255, alpha))
        );
    }


    public static Color darken(Color color, float factor) {
        return new Color(
                Math.max((int)(color.getRed() * factor), 0),
                Math.max((int)(color.getGreen() * factor), 0),
                Math.max((int)(color.getBlue() * factor), 0),
                color.getAlpha()
        );
    }


    public static Color brighten(Color color, float factor) {
        int r = Math.min((int)(color.getRed() * factor), 255);
        int g = Math.min((int)(color.getGreen() * factor), 255);
        int b = Math.min((int)(color.getBlue() * factor), 255);
        return new Color(r, g, b, color.getAlpha());
    }
}