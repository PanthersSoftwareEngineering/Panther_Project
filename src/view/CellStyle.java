package view;

import java.awt.*;

public final class CellStyle {

    private CellStyle(){}

    // ==============================
    // 🌈 GLOWING BASE COLORS
    // ==============================
    public static final Color P1_BASE = new Color(90, 180, 255, 200);   // neon blue
    public static final Color P2_BASE = new Color(110, 255, 170, 200);  // neon green

    // Revealed neutral
    public static final Color REVEALED = new Color(255, 255, 255, 140);

    // Used question/surprise
    public static final Color USED = new Color(90, 90, 90, 190);

    // Flags & mines (strong contrast)
    public static final Color FLAG = new Color(255, 220, 120, 230);     // gold glow
    public static final Color MINE = new Color(255, 80, 80, 230);       // red glow

    // ==============================
    // COLOR LOGIC
    // ==============================
    public static Color colorForSymbol(String symbol, int playerIdx) {
        Color base = (playerIdx == 0) ? P1_BASE : P2_BASE;

        if (symbol == null) return base;
        String s = symbol.trim();

        if (s.equalsIgnoreCase("F") || s.equals("⚑")) return FLAG;
        if (s.equalsIgnoreCase("M") || s.equals("*") || s.equals("💣")) return MINE;

        return base;
    }

    public static Color textColorForSymbol(String symbol) {
        if (symbol == null) return Color.BLACK;
        String s = symbol.trim();

        if (s.matches("\\d+")) return Color.BLACK;
        if (s.equalsIgnoreCase("M") || s.equals("*") || s.equals("💣")) return Color.WHITE;

        return Color.BLACK;
    }
}
