package view;

import view.dialogs.AbstractStyledDialog;

import javax.swing.*;
import java.awt.*;
import static view.QuestionManagerView.RoundedButton;

/**
 * StyledConfirmDialog
 * -------------------
 * Concrete implementation of AbstractStyledDialog.
 * Used for OK/Cancel or Yes/No style confirmations.
 *
 * Keeps the SAME external API:
 *   int res = StyledConfirmDialog.show(owner, "message", JOptionPane.OK_CANCEL_OPTION);
 */
public class StyledConfirmDialog extends AbstractStyledDialog {

    private final String message;
    private final int options;

    /**
     * Factory method to show the dialog.
     * @return JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION
     */
    public static int show(JFrame owner, String message, int options) {
        StyledConfirmDialog dialog = new StyledConfirmDialog(owner, message, options);
        return dialog.showDialog();
    }

    private StyledConfirmDialog(JFrame owner, String message, int options) {
        super(owner, "Confirm Action");
        this.message = message;
        this.options = options;
    }

    // =========================================================
    // Template hooks
    // =========================================================

    @Override
    protected JPanel createMessagePanel() {
        JLabel iconLabel = new JLabel("?");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        iconLabel.setForeground(accentColor());

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        msgLabel.setForeground(Color.WHITE);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        panel.setOpaque(false);
        panel.add(iconLabel);
        panel.add(msgLabel);

        return panel;
    }

    @Override
    protected JPanel createButtonsPanel() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);

        RoundedButton okBtn = createButton("OK", 120, 45, 18);
        okBtn.addActionListener(e -> {
            result = JOptionPane.OK_OPTION;
            dispose();
        });

        RoundedButton cancelBtn = createButton("Cancel", 120, 45, 18);
        cancelBtn.addActionListener(e -> {
            result = JOptionPane.CANCEL_OPTION;
            dispose();
        });

        if (options == JOptionPane.OK_CANCEL_OPTION || options == JOptionPane.YES_NO_OPTION) {
            buttons.add(okBtn);
            buttons.add(cancelBtn);
            getRootPane().setDefaultButton(cancelBtn); // safer default
        } else {
            // single-button mode 
            buttons.add(okBtn);
            getRootPane().setDefaultButton(okBtn);
        }

        return buttons;
    }
}
