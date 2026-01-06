package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * HeartsPanel
 * ==========
 * Renders a row of heart icons (filled/empty) and animates a "break" when a life is lost.
 *
 * - maxLives hearts are displayed (usually 10 in your model)
 * - setLives(newLives) updates the UI
 * - if lives decrease, the heart(s) animate breaking and then become empty
 */
public class HeartsPanel extends JPanel {

    private final HeartComponent[] hearts;
    private int lives; // current lives shown in UI
    private final int maxLives;

    // if multiple lives are lost at once, animate them sequentially
    private Timer multiLoseTimer;
    private int multiLoseIndex;   // which heart index we are animating now
    private int targetLivesAfter; // final lives after all animations

    public HeartsPanel(int maxLives, int initialLives) {
        this.maxLives = Math.max(1, maxLives);
        this.lives = Math.max(0, Math.min(initialLives, this.maxLives));

        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

        hearts = new HeartComponent[this.maxLives];
        for (int i = 0; i < this.maxLives; i++) {
            HeartComponent h = new HeartComponent();
            h.setFilled(i < this.lives);
            hearts[i] = h;
            add(h);
        }
    }

    public int getLives() {
        return lives;
    }

    /**
     * Update displayed lives.
     * If lives decreased, animate the lost heart(s) breaking.
     */
    public void setLives(int newLives) {
        newLives = Math.max(0, Math.min(newLives, maxLives));

        if (newLives == this.lives) {
            // nothing changed
            return;
        }

        if (newLives > this.lives) {
            // Gaining lives: fill immediately (no break animation)
            this.lives = newLives;
            applyFillState();
            return;
        }

        // Losing lives: animate each lost heart sequentially
        // Example: lives 7 -> 5 means hearts at indices 6 and 5 break and become empty.
        startMultiLoseAnimation(newLives);
    }

    private void applyFillState() {
        for (int i = 0; i < maxLives; i++) {
            // if a heart is currently animating, don't override it mid-animation
            if (!hearts[i].isAnimating()) {
                hearts[i].setFilled(i < lives);
            }
        }
        revalidate();
        repaint();
    }

    private void startMultiLoseAnimation(int newLives) {
        // stop any previous sequence
        if (multiLoseTimer != null) {
            multiLoseTimer.stop();
            multiLoseTimer = null;
        }

        targetLivesAfter = newLives;

        // first heart to break is the "last filled" one: lives-1
        multiLoseIndex = this.lives - 1;

        multiLoseTimer = new Timer(60, e -> {
            if (multiLoseIndex < targetLivesAfter) {
                // done animating all lost hearts
                multiLoseTimer.stop();
                multiLoseTimer = null;

                this.lives = targetLivesAfter;
                applyFillState();
                return;
            }

            HeartComponent h = hearts[multiLoseIndex];

            // Only animate if it is currently filled and not already animating
            if (h.isFilled() && !h.isAnimating()) {
                h.animateBreakThenEmpty();
            }

            multiLoseIndex--;
        });

        multiLoseTimer.setRepeats(true);
        multiLoseTimer.start();
    }

    // ------------------------------------------------------------
    // Inner component: one heart icon with break animation
    // ------------------------------------------------------------
    private static class HeartComponent extends JComponent {

        private static final int W = 22;
        private static final int H = 20;

        // Colors (tweak if you want)
        private static final Color FILLED = new Color(220, 40, 60);
        private static final Color EMPTY_STROKE = new Color(230, 235, 255, 170);
        private static final Color EMPTY_FILL = new Color(0, 0, 0, 0);

        private boolean filled = true;

        // animation state
        private boolean animating = false;
        private float t = 0f; // 0..1
        private Timer animTimer;

        HeartComponent() {
            setOpaque(false);
            setPreferredSize(new Dimension(W, H));
            setMinimumSize(new Dimension(W, H));
            setMaximumSize(new Dimension(W, H));
        }

        boolean isFilled() {
            return filled;
        }

        void setFilled(boolean filled) {
            this.filled = filled;
            repaint();
        }

        boolean isAnimating() {
            return animating;
        }

        void animateBreakThenEmpty() {
            if (animating) return;

            animating = true;
            t = 0f;

            if (animTimer != null) animTimer.stop();

            animTimer = new Timer(30, e -> {
                t += 0.08f;
                if (t >= 1f) {
                    t = 1f;
                    animTimer.stop();
                    animTimer = null;

                    // after break animation ends -> become empty
                    filled = false;
                    animating = false;
                    repaint();
                    return;
                }
                repaint();
            });

            animTimer.setRepeats(true);
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = 0;
            int y = 0;
            int w = getWidth();
            int h = getHeight();

            // subtle shadow
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fill(new RoundRectangle2D.Float(x + 1, y + 2, w - 2, h - 2, 8, 8));

            if (!animating) {
                // normal state: filled or empty
                paintWholeHeart(g2, x, y, w, h, filled);
            } else {
                // breaking state:
                // draw two halves splitting apart horizontally, with a jagged crack in the middle
                float split = 6f * t;      // how far halves move
                float fade = 1f - 0.25f*t; // slight fade

                Shape heart = heartShape(x, y, w, h);

                // left half clip
                Shape leftClip = new Rectangle(x - 2, y - 2, w / 2 + 2, h + 4);
                // right half clip
                Shape rightClip = new Rectangle(x + w / 2, y - 2, w / 2 + 2, h + 4);

                // left half
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
                g2.translate(-split, 0);
                g2.setClip(leftClip);
                fillAndStrokeHeart(g2, heart, true);
                g2.translate(split, 0);

                // right half
                g2.translate(split, 0);
                g2.setClip(rightClip);
                fillAndStrokeHeart(g2, heart, true);
                g2.translate(-split, 0);

                // reset clip
                g2.setClip(null);
                g2.setComposite(AlphaComposite.SrcOver);

                // crack line
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(255, 255, 255, (int) (160 * (1f - t))));
                int cx = x + w / 2;
                int top = y + 3;
                int bottom = y + h - 3;

                // simple zig-zag crack
                g2.drawLine(cx, top, cx - 3, top + 5);
                g2.drawLine(cx - 3, top + 5, cx + 2, top + 10);
                g2.drawLine(cx + 2, top + 10, cx - 2, top + 14);
                g2.drawLine(cx - 2, top + 14, cx + 3, bottom);

                // small "shards" effect near the crack
                g2.setColor(new Color(255, 255, 255, (int) (120 * (1f - t))));
                g2.fillOval(cx - 2, y + h / 2 - 2, 3, 3);
                g2.fillOval(cx + 3, y + h / 2 + 1, 2, 2);
            }

            g2.dispose();
        }

        private void paintWholeHeart(Graphics2D g2, int x, int y, int w, int h, boolean filled) {
            Shape heart = heartShape(x, y, w, h);

            if (filled) {
                fillAndStrokeHeart(g2, heart, true);
            } else {
                // empty heart (outline)
                g2.setColor(EMPTY_FILL);
                g2.fill(heart);

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(EMPTY_STROKE);
                g2.draw(heart);
            }
        }

        private void fillAndStrokeHeart(Graphics2D g2, Shape heart, boolean filled) {
            if (filled) {
                g2.setColor(FILLED);
                g2.fill(heart);

                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(255, 255, 255, 120));
                g2.draw(heart);
            } else {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(EMPTY_STROKE);
                g2.draw(heart);
            }
        }

        /**
         * A heart shape built using a Path2D (scales to component size).
         */
        private Shape heartShape(int x, int y, int w, int h) {
            // Parametric heart curve:
            // X = 16 sin^3(t)
            // Y = 13 cos(t) - 5 cos(2t) - 2 cos(3t) - cos(4t)
            // We'll normalize it into the component bounds.

            Path2D p = new Path2D.Float();

            // These constants come from the known curve range:
            // X is about [-16..16], Y is about [-17..13] (depending on curve sampling)
            // We'll fit into the box with margins.
            double minX = -16, maxX = 16;
            double minY = -17, maxY = 13;

            double sx = (w - 2.0) / (maxX - minX);
            double sy = (h - 2.0) / (maxY - minY);

            boolean first = true;
            for (int deg = 0; deg <= 360; deg += 4) {
                double t = Math.toRadians(deg);
                double X = 16 * Math.pow(Math.sin(t), 3);
                double Y = 13 * Math.cos(t) - 5 * Math.cos(2*t) - 2 * Math.cos(3*t) - Math.cos(4*t);

                double px = x + 1 + (X - minX) * sx;
                double py = y + 1 + (maxY - Y) * sy; // invert Y so top is smaller

                if (first) {
                    p.moveTo(px, py);
                    first = false;
                } else {
                    p.lineTo(px, py);
                }
            }
            p.closePath();
            return p;
        }

    }
}
