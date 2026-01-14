package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.Random;

public class CellButton extends JButton {

    private Color base;
    private boolean rounded = true;

    // =========================
    // Explosion animation state
    // =========================
    private Timer explosionTimer;
    private float explosionT = 1f; // 0..1
    private boolean exploding = false;

    private static final int PCOUNT = 12;
    private final float[] pvx = new float[PCOUNT];
    private final float[] pvy = new float[PCOUNT];
    private final Random rnd = new Random();

    // =========================
    // Sparkle animation state
    // =========================
    private Timer sparkleTimer;
    private float sparkleT = 1f; // 0..1
    private boolean sparkling = false;

    private static final int SCOUNT = 9;
    private final float[] sx = new float[SCOUNT];
    private final float[] sy = new float[SCOUNT];
    private final float[] ss = new float[SCOUNT]; // size

    public CellButton(String text, Color base) {
        super(text);
        setBaseColor(base);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        setFont(new Font("SansSerif", Font.BOLD, 16));
        setMargin(new Insets(0, 0, 0, 0));
        setHorizontalAlignment(SwingConstants.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) repaint(); }
            @Override public void mouseExited(MouseEvent e)  { if (isEnabled()) repaint(); }
            @Override public void mousePressed(MouseEvent e) { if (isEnabled()) repaint(); }
            @Override public void mouseReleased(MouseEvent e){ if (isEnabled()) repaint(); }
        });
    }

    public void setBaseColor(Color c) {
        this.base = c;
        repaint();
    }

    public Color getBaseColor() { return base; }

    public void setRounded(boolean rounded) {
        this.rounded = rounded;
        repaint();
    }

    // =========================
    // Mine explosion
    // =========================
    public void startExplosion() {
        if (explosionTimer != null && explosionTimer.isRunning()) {
            explosionTimer.stop();
        }

        exploding = true;
        explosionT = 0f;

        for (int i = 0; i < PCOUNT; i++) {
            float ang = (float) (rnd.nextFloat() * Math.PI * 2);
            float sp = 0.9f + rnd.nextFloat() * 2.0f;
            pvx[i] = (float) Math.cos(ang) * sp;
            pvy[i] = (float) Math.sin(ang) * sp;
        }

        explosionTimer = new Timer(16, e -> {
            explosionT += 0.07f;
            if (explosionT >= 1f) {
                explosionT = 1f;
                exploding = false;
                explosionTimer.stop();
            }
            repaint();
        });
        explosionTimer.start();
    }

    // =========================
    // Surprise sparkle (✨ glow)
    // =========================
    public void startSparkle() {
        if (sparkleTimer != null && sparkleTimer.isRunning()) {
            sparkleTimer.stop();
        }

        sparkling = true;
        sparkleT = 0f;

        // pre-generate sparkle positions inside the cell
        // (keep away from edges so it looks nicer)
        for (int i = 0; i < SCOUNT; i++) {
            sx[i] = 0.18f + rnd.nextFloat() * 0.64f;
            sy[i] = 0.18f + rnd.nextFloat() * 0.64f;
            ss[i] = 0.10f + rnd.nextFloat() * 0.18f;
        }

        sparkleTimer = new Timer(16, e -> {
            sparkleT += 0.06f; // ~16 frames
            if (sparkleT >= 1f) {
                sparkleT = 1f;
                sparkling = false;
                sparkleTimer.stop();
            }
            repaint();
        });
        sparkleTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // background & border
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        boolean hover   = getModel().isRollover();
        boolean pressed = getModel().isPressed();
        boolean enabled = isEnabled();

        Color fill = base;
        if (!enabled) {
            fill = new Color(120,120,120,120);
        } else if (pressed) {
            fill = darken(base, 0.15f);
        } else if (hover) {
            fill = brighten(base, 0.20f);
        }

        // subtle glow on hover
        if (enabled) {
            Color glow = brighten(fill, hover ? 0.45f : 0.30f);
            g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 90));
            g2.fillRoundRect(-2, -2, w + 4, h + 4, 20, 20);
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w - 4, h - 4, 16, 16);

        g2.setColor(new Color(255,255,255, hover ? 160 : 100));
        g2.drawRoundRect(0, 0, w - 4, h - 4, 16, 16);

        g2.dispose();

        // draw text/icon
        super.paintComponent(g);

        // sparkle overlay should be on top of the icon
        if (sparkling) {
            paintSparkles((Graphics2D) g, w, h);
        }

        // explosion overlay on top (also OK if both happen)
        if (exploding) {
            paintExplosion((Graphics2D) g, w, h);
        }
    }

    private void paintSparkles(Graphics2D g, int w, int h) {
        Graphics2D fx = (Graphics2D) g.create();
        fx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float t = sparkleT;     // 0..1
        float inv = 1f - t;

        // Glow aura that fades out
        float aura = (Math.min(w, h) * 0.12f) + (Math.min(w, h) * 0.25f) * (float)Math.sin(Math.PI * t);
        fx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * inv));
        fx.setColor(new Color(255, 245, 190)); // warm sparkle
        fx.fill(new Ellipse2D.Float(w/2f - aura, h/2f - aura, aura*2, aura*2));

        // Sparkles (small stars)
        fx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, 0.90f * inv)));
        fx.setStroke(new BasicStroke(Math.max(1.5f, Math.min(w, h) / 22f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        fx.setColor(new Color(255, 255, 230, (int)(230 * inv)));

        for (int i = 0; i < SCOUNT; i++) {
            float cx = sx[i] * w;
            float cy = sy[i] * h;

            float size = ss[i] * Math.min(w, h);
            float pulse = 0.55f + 0.45f * (float)Math.sin((t * 6f + i) * 1.1f);
            float s = size * pulse;

            // draw a 4-point sparkle (plus + x)
            fx.drawLine((int)(cx - s), (int)cy, (int)(cx + s), (int)cy);
            fx.drawLine((int)cx, (int)(cy - s), (int)cx, (int)(cy + s));
            fx.drawLine((int)(cx - s*0.75f), (int)(cy - s*0.75f), (int)(cx + s*0.75f), (int)(cy + s*0.75f));
            fx.drawLine((int)(cx - s*0.75f), (int)(cy + s*0.75f), (int)(cx + s*0.75f), (int)(cy - s*0.75f));
        }

        fx.setComposite(AlphaComposite.SrcOver);
        fx.dispose();
    }

    private void paintExplosion(Graphics2D g, int w, int h) {
        Graphics2D fx = (Graphics2D) g.create();
        fx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float t = explosionT;        // 0..1
        float inv = 1f - t;

        float cx = w / 2f;
        float cy = h / 2f;

        float min = Math.min(w, h);
        float radius = (min * 0.10f) + (min * 0.70f) * t;

        fx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, 0.95f * inv)));

        // outer ring
        fx.setStroke(new BasicStroke(Math.max(2f, min / 16f)));
        fx.setColor(new Color(255, 170, 60, (int)(220 * inv)));
        fx.draw(new Ellipse2D.Float(cx - radius, cy - radius, radius * 2, radius * 2));

        // inner flash
        fx.setColor(new Color(255, 240, 200, (int)(180 * inv)));
        fx.fill(new Ellipse2D.Float(cx - radius * 0.55f, cy - radius * 0.55f, radius * 1.1f, radius * 1.1f));

        // particles
        for (int i = 0; i < PCOUNT; i++) {
            float sxp = cx + pvx[i] * (t * (min * 0.75f));
            float syp = cy + pvy[i] * (t * (min * 0.75f));
            float pr = 2.5f + (1f - t) * 3.0f;

            Color pc = (i % 2 == 0)
                    ? new Color(255, 210, 90, (int)(200 * inv))
                    : new Color(255, 90, 60,  (int)(200 * inv));

            fx.setColor(pc);
            fx.fill(new Ellipse2D.Float(sxp - pr, syp - pr, pr * 2, pr * 2));
        }

        fx.setComposite(AlphaComposite.SrcOver);
        fx.dispose();
    }

    @Override
    public boolean isContentAreaFilled() {
        return false;
    }

    private static Color brighten(Color c, float amount) {
        int r = (int) Math.min(255, c.getRed()   + 255 * amount);
        int g = (int) Math.min(255, c.getGreen() + 255 * amount);
        int b = (int) Math.min(255, c.getBlue()  + 255 * amount);
        return new Color(r, g, b, c.getAlpha());
    }

    private static Color darken(Color c, float amount) {
        int r = (int) Math.max(0, c.getRed()   - 255 * amount);
        int g = (int) Math.max(0, c.getGreen() - 255 * amount);
        int b = (int) Math.max(0, c.getBlue()  - 255 * amount);
        return new Color(r, g, b, c.getAlpha());
    }
}
