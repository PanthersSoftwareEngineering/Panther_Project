package view;

import javax.swing.*;
import java.awt.*;

import controller.AppController;
import model.SysData;

/**
 * Small window shown at the end of a game.
 * Displays final result and offers a button to return to the main menu.
 */
public class EndView extends JFrame {

    /**
     * Creates an end-of-game window with a summary of the given GameRecord.
     */
    public EndView(AppController app, SysData.GameRecord rec){
        super("Game Over");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String result = rec.won ? "WON" : "LOST";

        JPanel p = new JPanel(new GridLayout(0,1,4,4));
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        p.add(new JLabel("Result: " + result));
        p.add(new JLabel("Players: " + rec.p1 + " vs " + rec.p2));
        p.add(new JLabel("Difficulty: " + rec.level));
        p.add(new JLabel("Points: " + rec.points));
        p.add(new JLabel("Hearts left: " + rec.hearts));
        p.add(new JLabel("Time: " + rec.timeSec + "s"));

        JButton back = new JButton("Back to Main");
        back.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });

        setLayout(new BorderLayout(8,8));
        add(p, BorderLayout.CENTER);
        add(back, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Included for consistency with other views; here the frame is already visible.
     */
    public void showSelf(){ }
}
