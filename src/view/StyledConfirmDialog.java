package view;

import javax.swing.*;
import java.awt.*;
import static view.QuestionManagerView.RoundedButton; // משתמש בכפתורים המקומיים

/**
 * Custom modal dialog for confirmation (Yes/No or OK/Cancel), styled to match the theme.
 * Returns JOptionPane.YES_OPTION or JOptionPane.NO_OPTION (or CANCEL/CLOSED).
 */
public class StyledConfirmDialog extends JDialog {

    private static final Color DARK_BG = new Color(20, 30, 35, 250); 
    private static final Color ACCENT_COLOR = new Color(80, 200, 180); 
    
    private int result = JOptionPane.CANCEL_OPTION; // Default result

    /**
     * Factory method to show the styled confirmation dialog.
     * @param owner The parent frame.
     * @param message The confirmation question.
     * @param options The type of options (e.g., JOptionPane.OK_CANCEL_OPTION).
     * @return The user's selection (e.g., JOptionPane.OK_OPTION).
     */
    public static int show(JFrame owner, String message, int options) {
        StyledConfirmDialog dialog = new StyledConfirmDialog(owner, message, options);
        dialog.setVisible(true);
        return dialog.result;
    }

    private StyledConfirmDialog(JFrame owner, String message, int options) {
        super(owner, "Confirm Action", true);

        // --- Setup basic dialog properties ---
        setSize(500, 250);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 4, true));

        // --- Root Panel ---
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(DARK_BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Message ---
        JLabel iconLabel = new JLabel("?"); //char to be like icon
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        iconLabel.setForeground(ACCENT_COLOR); // it keeps the turkiz color
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        iconLabel.setForeground(ACCENT_COLOR);
        
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        msgLabel.setForeground(Color.WHITE);
        
        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        msgPanel.setOpaque(false);
        msgPanel.add(iconLabel);
        msgPanel.add(msgLabel);
        root.add(msgPanel, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);

        // OK/Yes Button
        RoundedButton okBtn = new RoundedButton("OK", 120, 45, 18);
        okBtn.addActionListener(e -> {
            result = JOptionPane.OK_OPTION;
            dispose();
        });
        
        // Cancel/No Button
        RoundedButton cancelBtn = new RoundedButton("Cancel", 120, 45, 18);
        cancelBtn.addActionListener(e -> {
            result = JOptionPane.CANCEL_OPTION;
            dispose();
        });

        if (options == JOptionPane.OK_CANCEL_OPTION || options == JOptionPane.YES_NO_OPTION) {
            buttons.add(okBtn);
            buttons.add(cancelBtn);
            getRootPane().setDefaultButton(cancelBtn); // Safer default
        } else {
            buttons.add(okBtn); // Single button mode (if needed)
            getRootPane().setDefaultButton(okBtn);
        }
        
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }
}