package view.dialogs;

import javax.swing.*;
import java.awt.*;
import static view.QuestionManagerView.RoundedButton;

/**
 * AbstractStyledDialog
 * ====================
 * Template Method base class for all game-styled dialogs.
 *
 * What this class guarantees:
 * - Consistent size, border, background and spacing
 * - Centered title + centered message layout
 * - Optional second (smaller) message line
 * - Buttons row placed at the bottom, centered
 */
public abstract class AbstractStyledDialog extends JDialog {

    protected int result = JOptionPane.CANCEL_OPTION;

    protected AbstractStyledDialog(JFrame owner, String title) {
        super(owner, title, true); 
    
    }

    /**
     * Builds the dialog UI. Call once at the END of subclass constructor,
     * after all fields are initialized.
     */
    protected final void initDialog() {
        buildDialog();
    }

    // ---------
    // ---------------------------------------------------------
    private void buildDialog() {
        setUndecorated(true);
        setSize(dialogSize());
        setLocationRelativeTo(getOwner());
        getRootPane().setBorder(BorderFactory.createLineBorder(accentColor(), 4, true));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(backgroundColor());
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 18, 28));
        setContentPane(root);

        root.add(buildContent(), BorderLayout.CENTER);
        root.add(createButtonsPanel(), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel buildContent() {
        JPanel_toggleNoScroll();

        JPanel col = new JPanel();
        col.setOpaque(false);

        // Title (centered)
        JLabel title = new JLabel(titleText(), SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(titleColor());
        title.setFont(new Font("Segoe UI", Font.BOLD, titleFontSize()));
        col.add(title);

        col.add(Box.createVerticalStrut(14));

        // Main message 
        JLabel primary = wrappedLabel(primaryMessage(), primaryFontSize(), primaryColor());
        col.add(primary);

        // Optional detail line
        String detail = secondaryMessage();
        if (detail != null && !detail.isBlank()) {
            col.add(Box.createVerticalStrut(10));
            JLabel secondary = wrappedLabel(detail, secondaryFontSize(), secondaryColor());
            col.add(secondary);
        }

        col.add(Box.createVerticalStrut(12));
        return col;
    }

    /**
     * Creates a centered, wrapped text block 
     */
    private JLabel wrappedLabel(String text, int fontSize, Color color) {
        if (text == null) text = "";

        String safe = escapeHtml(text);
        int w = messageBlockWidth();

        JLabel lbl = new JLabel(
                "<html><div style='width:" + w + "px; text-align:center;'>" + safe + "</div></html>",
                SwingConstants.CENTER
        );
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setForeground(color);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        return lbl;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        String out = s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");

        // Make newlines visible inside HTML labels
        out = out.replace("\r\n", "\n").replace("\r", "\n");
        out = out.replace("\n", "<br>");
        return out;
    }

    // ---------------------------------------------------------
    // Hooks for subclasses
    // ---------------------------------------------------------
    protected String titleText() { return getTitle(); }
    protected abstract String primaryMessage();
    protected String secondaryMessage() { return null; }
    protected abstract JPanel createButtonsPanel();

    // ---------------------------------------------------------
    // Styling defaults
    // ---------------------------------------------------------
    protected Dimension dialogSize() { return new Dimension(620, 280); }
    protected int messageBlockWidth() { return 520; }

    protected Color backgroundColor() { return new Color(15, 18, 40, 250); }
    protected Color accentColor() { return new Color(255, 195, 0); }

    protected Color titleColor() { return accentColor(); }
    protected Color primaryColor() { return Color.WHITE; }
    protected Color secondaryColor() { return new Color(210, 210, 210, 210); }

    protected int titleFontSize() { return 30; }
    protected int primaryFontSize() { return 18; }
    protected int secondaryFontSize() { return 14; }

    protected RoundedButton createButton(String text, int w, int h, int fontSize) {
        return new RoundedButton(text, w, h, fontSize);
    }

    public final int showDialog() {
        setVisible(true);
        return result;
    }

    // no-op helper (keeps intent explicit)
    private void JPanel_toggleNoScroll() {
        // dialogs are intentionally short
    }
}
