package view;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import controller.AppController;
import controller.QuestionController;
import model.Question;
import model.QuestionLevel;

/**
 * Question management window 
 * Allows viewing, adding, editing, and deleting trivia questions.
 *
 * This view does NOT access SysData directly
 * All operations go through QuestionController
 */
public class QuestionManagerView extends JFrame {

    /* Controller reference for all CRUD actions */
    private final QuestionController controller;

    
    
    /* Swing table showing all questions */
    private /*final parametr*/ JTable table;


    /**
     * Builds the Question Manager UI:
     * - Top bar with title and Back button
     * - Center table with all questions
     * - Bottom bar with Add/Edit/Delete buttons
     */
 
    public QuestionManagerView(QuestionController controller) {
        super("Question Manager");

        /* Save controller reference */
        this.controller = controller;

        /* Basic Swing window settings */
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        setMinimumSize(new Dimension(820, 420));

        // ===================== TOP BAR =====================

        /* Top panel holds title + Back button */
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        /* Back button returns to main menu */
        JButton back = new JButton("Back to Main");
        back.addActionListener(e -> {
            dispose();
            AppController.getInstance().showMainMenu();
        });

        /* Title label */
        JLabel title = new JLabel("Question Management", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        top.add(title, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

      pack();
        setLocationRelativeTo(null);
    }

    /** Shows this window */
  
    public void showSelf() {
        setVisible(true);
    }
}