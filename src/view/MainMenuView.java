package view;

import javax.swing.*;
import java.awt.*;

import controller.AppController;

public class MainMenuView extends JFrame {

    private final AppController app;

    public MainMenuView(AppController app) {
        super("Minesweeper - Main");
        this.app = app;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new GridLayout(4, 1, 12, 12));

        JButton start   = new JButton("Start Game");
        JButton history = new JButton("Game History");
        JButton qman    = new JButton("Question Management");
        JButton exit    = new JButton("Exit");

        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(start);
        p.add(history);
        p.add(qman);
        p.add(exit);

        setContentPane(p);

        // Start game – not implemented yet
        start.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Game module is not implemented yet (Iteration 2).",
                        "Coming Soon",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        // History – not implemented yet
        history.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "History module is not implemented yet (Iteration 2).",
                        "Coming Soon",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        // Question Management – close menu and open question manager
        qman.addActionListener(e -> {
            dispose();
            app.openQuestionManager();
        });

        // Exit – just close this window
        exit.addActionListener(e -> dispose());

        pack();
    }

    public void showSelf() {
        setLocationRelativeTo(null);
        setVisible(true);
    }
}