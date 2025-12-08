package view;

import javax.swing.*;
import java.awt.*;

import controller.AppController;

/**
 * Window used to configure and start a new match.
 * Lets the user enter player names and choose a difficulty level.
 */
public class NewMatchView extends JFrame {

    // Placeholder for difficulty selection
    private static final String PLACEHOLDER_DIFF = "Select difficulty";

    /** Text field for player 1 name (starts empty). */
    private final JTextField p1 = new JTextField(12);

    /** Text field for player 2 name (starts empty). */
    private final JTextField p2 = new JTextField(12);

    /** Combo box for difficulty selection (starts with placeholder). */
    private final JComboBox<String> diff =
            new JComboBox<>(new String[]{PLACEHOLDER_DIFF, "EASY", "MEDIUM", "HARD"});

    /**
     * Creates the "New Match" window and wires its UI to the AppController.
     */
    public NewMatchView(AppController app){
        super("New Match");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton back  = new JButton("Back");
        JButton start = new JButton("Start");

        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.add(new JLabel("Player 1:"));    root.add(p1);
        root.add(new JLabel("Player 2:"));    root.add(p2);
        root.add(new JLabel("Difficulty:"));  root.add(diff);
        root.add(start);                      root.add(back);
        setContentPane(root);

        // Explicitly select the placeholder difficulty
        diff.setSelectedItem(PLACEHOLDER_DIFF);
     // When "Start" is clicked, validate names and difficulty before starting the game.
        start.addActionListener(e -> {
            String name1 = p1.getText().trim();
            String name2 = p2.getText().trim();
            String level = (String) diff.getSelectedItem();

            // ===================== Player 1 checks =====================
            // 1.1 not empty
            if (name1.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Player 1 name must not be empty.",
                        "Invalid Player 1 Name",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 1.2 length <= 10
            if (name1.length() > 10) {
                JOptionPane.showMessageDialog(
                        this,
                        "Player 1 name must be at most 10 characters.",
                        "Invalid Player 1 Name",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 1.3 only letters and digits
            if (!containsOnlyLettersAndDigits(name1)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Player 1 name may contain only English letters and digits.",
                        "Invalid Player 1 Name",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // ===================== Player 2 checks =====================
            // 2.1 not empty
            if (name2.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Player 2 name must not be empty.",
                        "Invalid Player 2 Name",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 2.2 length <= 10
            if (name2.length() > 10) {
                JOptionPane.showMessageDialog(
                        this,
                        "Player 2 name must be at most 10 characters.",
                        "Invalid Player 2 Name",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 2.3 only letters and digits
            if (!containsOnlyLettersAndDigits(name2)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Player 2 name may contain only English letters and digits.",
                        "Invalid Player 2 Name",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // ===================== Both names together =====================
            if (name1.equalsIgnoreCase(name2)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Player names must be different.",
                        "Duplicate Player Names",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // ===================== Difficulty checks =====================
            if (level == null || PLACEHOLDER_DIFF.equals(level)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please choose a difficulty level before starting the game.",
                        "Difficulty Not Selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // All validations passed → start the game
            app.onStart(name1, name2, level);
            dispose();
        });


        // When "Back" is clicked, return to main menu.
        back.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    
    /** Returns true iff the name contains only letters and digits. */
    private boolean containsOnlyLettersAndDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isLetterOrDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /** Here the window is already visible, but kept for consistency. */
    public void showSelf(){ /* shown in ctor */ }
}
