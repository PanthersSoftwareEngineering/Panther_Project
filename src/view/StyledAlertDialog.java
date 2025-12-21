package view;

import javax.swing.*;
import java.awt.*;

// משתמש במחלקת RoundedButton שיצרנו
import static view.QuestionManagerView.RoundedButton;

/**
 * Custom modal dialog for displaying error or warning messages, styled 
 * to match the game's Dark Teal/Turquoise theme.
 */
public class StyledAlertDialog extends JDialog {

    private static final Color DARK_BG = new Color(20, 30, 35, 250); 
    private static final Color ACCENT_COLOR = new Color(80, 200, 180); 
    private static final Color ERROR_COLOR = new Color(255, 120, 120);

    /**
     * Factory method to show the styled alert dialog.
     * @param owner The parent frame.
     * @param title The dialog title (e.g., "Input Error").
     * @param message The main message text.
     */
    public static void show(JFrame owner, String title, String message, boolean isError) {
        StyledAlertDialog dialog = new StyledAlertDialog(owner, title, message, isError);
        dialog.setVisible(true);
    }

    private StyledAlertDialog(JFrame owner, String title, String message, boolean isError) {
        super(owner, title, true);

        // --- Setup basic dialog properties ---
        setSize(550, 280);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 4, true));

        // --- Root Panel ---
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(DARK_BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // --- Title and Icon ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        header.setOpaque(false);
        
        String iconText = isError ? "X" : "!"; // regular chars to be like icons here 
        JLabel iconLabel = new JLabel(iconText); 
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 40)); 
        
        //adding background color to be similiar to emoji
        if (isError) iconLabel.setForeground(Color.RED); else iconLabel.setForeground(Color.YELLOW);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(isError ? ERROR_COLOR : ACCENT_COLOR);
        
        header.add(iconLabel);
        header.add(titleLabel);
        root.add(header, BorderLayout.NORTH);

        // --- Message ---
        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        msgArea.setForeground(Color.WHITE);
        msgArea.setBackground(DARK_BG);
        msgArea.setEditable(false);
        msgArea.setWrapStyleWord(true);
        msgArea.setLineWrap(true);
        msgArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Add padding
        
        root.add(msgArea, BorderLayout.CENTER);

        // --- OK Button ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        bottom.setOpaque(false);
        
        RoundedButton okBtn = new RoundedButton("OK", 150, 50, 20);
        okBtn.addActionListener(e -> dispose());
        
        bottom.add(okBtn);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        
        // Ensure that pressing Enter/Escape closes the dialog
        getRootPane().setDefaultButton(okBtn);
    }
}