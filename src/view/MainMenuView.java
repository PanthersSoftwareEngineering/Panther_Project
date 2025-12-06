package view;

import javax.swing.*;
import java.awt.*;

import controller.AppController;

public class MainMenuView extends JFrame {
    private final AppController app;

    public MainMenuView(AppController app){
        super("Minesweeper - Main");
        this.app = app;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new GridLayout(4,1,12,12));
        JButton start   = new JButton("Start Game");
        JButton history = new JButton("Game History");
        JButton qman    = new JButton("Question Management");
        JButton exit    = new JButton("Exit");

        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.add(start);
        p.add(history);
        p.add(qman);
        p.add(exit);
        setContentPane(p);

        // Start Game – close menu and open new-match screen
        start.addActionListener(e -> {
            dispose();
            app.openNewMatch();
        });

        // >>> CHANGE IS HERE <<<
        // Game History – also close menu first, then open history window
        history.addActionListener(e -> {
            dispose();
            app.openHistory();
        });

        // Question Management – close menu and open question manager
        qman.addActionListener(e -> {
            dispose();
            app.openQuestionManager();
        });

        // Exit – just close this window
        exit.addActionListener(e -> dispose());

        pack();
    }

    public void showSelf(){
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
