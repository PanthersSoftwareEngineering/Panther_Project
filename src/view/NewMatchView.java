package view;

import javax.swing.*;
import java.awt.*;

import controller.AppController;

/**
 * Window used to configure and start a new match.
 * Lets the user enter player names and choose a difficulty level.
 */
public class NewMatchView extends JFrame {
    /** Text field for player 1 name. */
    private final JTextField p1 = new JTextField("Player A",12);

    /** Text field for player 2 name. */
    private final JTextField p2 = new JTextField("Player B",12);

    /** Combo box for difficulty selection. */
    private final JComboBox<String> diff =
            new JComboBox<>(new String[]{"EASY","MEDIUM","HARD"});

    /**
     * Creates the "New Match" window and wires its UI to the AppController.
     */
    public NewMatchView(AppController app){
        super("New Match");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        JButton back  = new JButton("Back");
        JButton start = new JButton("Start");

        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.add(new JLabel("Player 1:"));    root.add(p1);
        root.add(new JLabel("Player 2:"));    root.add(p2);
        root.add(new JLabel("Difficulty:"));  root.add(diff);
        root.add(start);                      root.add(back);
        setContentPane(root);

        // When "Start" is clicked, validate names and only then start the game.
        start.addActionListener(e -> {
            String name1 = p1.getText().trim();
            String name2 = p2.getText().trim();

            // Require both player names
            if (name1.isEmpty() || name2.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter names for both players.",
                        "Missing Player Name",
                        JOptionPane.WARNING_MESSAGE
                );
                return; // do not start the game
            }

            app.onStart(
                    name1,
                    name2,
                    (String)diff.getSelectedItem()
            );
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
