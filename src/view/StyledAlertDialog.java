package view;

import view.dialogs.AbstractStyledDialog;

import javax.swing.*;
import java.awt.*;
import static view.QuestionManagerView.RoundedButton;

/**
 * StyledAlertDialog
 * =================
 * One-button modal dialog used for errors and user guidance.
 */
public class StyledAlertDialog extends AbstractStyledDialog {

    private static final Color ERROR_COLOR = new Color(255, 80, 80);

    private final String dialogTitle;
    private final String mainMsg;
    private final String detailMsg;
    private final boolean isError;

    public static void show(JFrame owner, String title, String message, boolean isError) {
        new StyledAlertDialog(owner, title, message, null, isError).showDialog();
    }

    public static void show(JFrame owner, String title, String message, String detail, boolean isError) {
        new StyledAlertDialog(owner, title, message, detail, isError).showDialog();
    }

    private StyledAlertDialog(JFrame owner, String title, String message, String detail, boolean isError) {
        super(owner, title == null ? "" : title);
        this.dialogTitle = title == null ? "" : title;
        this.mainMsg = message == null ? "" : message;
        this.detailMsg = (detail != null && !detail.isBlank()) ? detail : null;
        this.isError = isError;

        initDialog(); // build UI after fields are ready
    }

    @Override
    protected String titleText() {
        return dialogTitle;
    }

    @Override
    protected Color titleColor() {
        return isError ? ERROR_COLOR : accentColor();
    }

    @Override
    protected String primaryMessage() {
        return mainMsg;
    }

    @Override
    protected String secondaryMessage() {
        return detailMsg;
    }

    @Override
    protected JPanel createButtonsPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        bottom.setOpaque(false);

        RoundedButton okBtn = createButton("OK", 220, 70, 26);
        okBtn.addActionListener(e -> {
            result = JOptionPane.OK_OPTION;
            dispose();
        });

        bottom.add(okBtn);
        getRootPane().setDefaultButton(okBtn);
        return bottom;
    }
}
