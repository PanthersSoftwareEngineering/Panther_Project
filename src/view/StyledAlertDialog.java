package view;

import view.dialogs.AbstractStyledDialog;

import javax.swing.*;
import java.awt.*;
import static view.QuestionManagerView.RoundedButton;

/**
 * StyledAlertDialog
 * -----------------
 * Concrete implementation of AbstractStyledDialog.
 * Displays an error/warning/info message with one OK button.
 *
 * Keeps the SAME external API:
 *   StyledAlertDialog.show(owner, title, message, isError);
 */
public class StyledAlertDialog extends AbstractStyledDialog {

    private static final Color ERROR_COLOR = new Color(255, 80, 80);

    private final String titleText;
    private final String message;
    private final boolean isError;

    /**
     * Factory method to show the styled alert dialog.
     */
    public static void show(JFrame owner, String title, String message, boolean isError) {
        StyledAlertDialog dialog = new StyledAlertDialog(owner, title, message, isError);
        dialog.showDialog(); // modal
    }

    private StyledAlertDialog(JFrame owner, String title, String message, boolean isError) {
        super(owner, title);
        this.titleText = title;
        this.message = message;
        this.isError = isError;
    }

    // =========================================================
    // Template hooks
    // =========================================================

    @Override
    protected JPanel createMessagePanel() {
        JPanel root = new JPanel(new BorderLayout(15, 10));
        root.setOpaque(false);

        // --- Header (icon + title) ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        header.setOpaque(false);

        String iconText = isError ? "X" : "!";
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        iconLabel.setForeground(isError ? Color.RED : Color.YELLOW);

        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(isError ? ERROR_COLOR : accentColor());

        header.add(iconLabel);
        header.add(titleLabel);

        // --- Message ---
        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        msgArea.setForeground(Color.WHITE);
        msgArea.setBackground(backgroundColor());
        msgArea.setEditable(false);
        msgArea.setWrapStyleWord(true);
        msgArea.setLineWrap(true);
        msgArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        root.add(header, BorderLayout.NORTH);
        root.add(msgArea, BorderLayout.CENTER);

        return root;
    }

    @Override
    protected JPanel createButtonsPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        bottom.setOpaque(false);

        RoundedButton okBtn = createButton("OK", 150, 50, 20);
        okBtn.addActionListener(e -> {
            result = JOptionPane.OK_OPTION;
            dispose();
        });

        bottom.add(okBtn);
        getRootPane().setDefaultButton(okBtn);

        return bottom;
    }

    // Keep your old dialog size
    @Override
    protected Dimension dialogSize() {
        return new Dimension(550, 280);
    }
}
