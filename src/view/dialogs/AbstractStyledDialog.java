package view.dialogs;

import javax.swing.*;
import java.awt.*;
import static view.QuestionManagerView.RoundedButton;

/**
 * AbstractStyledDialog (Template Method Pattern)
 * ---------------------------------------------
 * This class defines the FIXED algorithm for building all styled dialogs.
 *
 * Template method (fixed):
 * 1) Setup dialog window
 * 2) Build root panel (background + border)
 * 3) Add message section  (HOOK)
 * 4) Add buttons section  (HOOK)
 * 5) Show dialog and return result
 *
 * Hook methods (subclasses override):
 * - createMessagePanel()
 * - createButtonsPanel()
 */
public abstract class AbstractStyledDialog extends JDialog {

    protected int result = JOptionPane.CANCEL_OPTION;

    protected AbstractStyledDialog(JFrame owner, String title) {
        super(owner, title, true);
        buildDialog(); // TEMPLATE METHOD
    }

    // =========================================================
    // TEMPLATE METHOD
    // =========================================================
    private final void buildDialog() {

        // 1) Window setup
        setSize(dialogSize());
        setLocationRelativeTo(getOwner());
        setUndecorated(true);
        getRootPane().setBorder(
                BorderFactory.createLineBorder(accentColor(), borderThickness(), true)
        );

        // 2) Root panel
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(backgroundColor());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 3) Message section (HOOK)
        root.add(createMessagePanel(), BorderLayout.CENTER);

        // 4) Buttons section (HOOK)
        root.add(createButtonsPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    // =========================================================
    // HOOK METHODS (subclasses must implement)
    // =========================================================
    protected abstract JPanel createMessagePanel();
    protected abstract JPanel createButtonsPanel();

    // =========================================================
    // Style hooks (defaults) - subclasses may override if needed
    // =========================================================
    protected Dimension dialogSize() {
        return new Dimension(500, 250);
    }

    protected Color backgroundColor() {
        return new Color(15, 18, 40, 250); // deep navy
    }

    protected Color accentColor() {
        return new Color(255, 195, 0); // gold accent
    }

    protected int borderThickness() {
        return 4;
    }

    // =========================================================
    // Utility: create a RoundedButton with consistent sizing
    // =========================================================
    protected RoundedButton createButton(String text, int w, int h, int fontSize) {
        return new RoundedButton(text, w, h, fontSize);
    }

    /**
     * Show dialog (modal) and return the result code (JOptionPane.* constants).
     */
    public final int showDialog() {
        setVisible(true);
        return result;
    }
}
