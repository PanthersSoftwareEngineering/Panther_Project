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

            // 1. Both names must be non-empty
            if (name1.isEmpty() || name2.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Both player names must be filled in (they cannot be empty).",
                        "Missing Player Name",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 2. Names must be different
            if (name1.equalsIgnoreCase(name2)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Player names must be different.\nPlease choose two different names.",
                        "Duplicate Player Names",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 3. Difficulty must be selected (not the placeholder)
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

    /** Here the window is already visible, but kept for consistency. */
    public void showSelf(){ /* shown in ctor */ }
}
