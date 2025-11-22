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

    /* Table model that adapts Question objects to table rows/cols */
    private /*final parametr*/ QuestionTableModel model;
    
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

     // ===================== TABLE =====================

        /* Create model from controller list */
        model = new QuestionTableModel(controller.list());

        /* Create table using the model */
        table = new JTable(model);
        table.setFillsViewportHeight(true);

        /*  Put table in a scroll pane */
        add(new JScrollPane(table), BorderLayout.CENTER);

// ===================== BUTTONS BAR =====================

        /* Bottom panel for actions */
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        /* CRUD buttons */
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");

        buttons.add(btnAdd);
        buttons.add(btnEdit);
        buttons.add(btnDelete);

        add(buttons, BorderLayout.SOUTH);

        /* Wire buttons to handlers */
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        
      pack();
        setLocationRelativeTo(null);
    }

    /** Shows this window */
  
    public void showSelf() {
        setVisible(true);
    }
    

    
    
    
    
 // ==================     TABLE MODEL     ===================

    /**
     * Internal table model used only by QuestionManagerView.
     * Stores a local snapshot of Question list.
     */
    /* Full TableModel responsibility */
    private static class QuestionTableModel extends AbstractTableModel {

        /* Column names */
        private final String[] cols = {
                "ID", "Text", "Level",
                "Opt1", "Opt2", "Opt3", "Opt4",
                "Correct"
        };

        /* Data snapshot shown in the table */
        private List<Question> data = new ArrayList<>();

        /* Constructor loads initial snapshot */
        QuestionTableModel(List<Question> initial) {
            reload(initial);
        }

        /**
         * Reloads the table data from a "fresh" controller list.
         */
        public void reload(List<Question> qs) {
            data = new ArrayList<>(qs);
            fireTableDataChanged();
        }

        /**
         * Returns the question at a specific row.
         */
        public Question getAt(int row) {
            return data.get(row);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Question q = data.get(row);
            return switch (col) {
                case 0 -> q.id();
                case 1 -> q.text();
                case 2 -> q.level().name();
                case 3 -> q.options().get(0);
                case 4 -> q.options().get(1);
                case 5 -> q.options().get(2);
                case 6 -> q.options().get(3);
                case 7 -> q.correctIndex();
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false; // table is read-only
        }
    }
}

