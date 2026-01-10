package view;

import view.dialogs.AbstractStyledDialog;

import javax.swing.*;
import java.awt.*;

/**
 * StyledConfirmDialog
 * ===================
 * Two-button modal dialog used when the user must confirm an action
 *
 * Returns:
 * - JOptionPane.OK_OPTION    (confirm)
 * - JOptionPane.CANCEL_OPTION (cancel)
 */
public class StyledConfirmDialog extends AbstractStyledDialog {

    private final String msg;
    private final int options;

    public static int show(JFrame owner, String message, int options) {
        return new StyledConfirmDialog(owner, message, options).showDialog();
    }

    private StyledConfirmDialog(JFrame owner, String message, int options) {
        super(owner, "Confirm");
        this.msg = (message == null) ? "" : message;
        this.options = options;

        initDialog(); // build UI after fields are ready
    }

    @Override
    protected String titleText() {
        return "Confirm";
    }

    @Override
    protected String primaryMessage() {
        return msg;
    }

    @Override
    protected JPanel createButtonsPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 10));
        bottom.setOpaque(false);

        String okText = (options == JOptionPane.YES_NO_OPTION) ? "Yes" : "OK";
        String cancelText = (options == JOptionPane.YES_NO_OPTION) ? "No" : "Cancel";

        BaseGameFrame.RoundedButton okBtn = createButton(okText, 220, 70, 26);
        BaseGameFrame.RoundedButton cancelBtn = createButton(cancelText, 220, 70, 26);

        okBtn.addActionListener(e -> {
            result = JOptionPane.OK_OPTION;
            dispose();
        });

        cancelBtn.addActionListener(e -> {
            result = JOptionPane.CANCEL_OPTION;
            dispose();
        });

        bottom.add(okBtn);
        bottom.add(cancelBtn);

        // safer default: Cancel / No
        getRootPane().setDefaultButton(cancelBtn);

        return bottom;
    }
}
