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
    
 // =================    CRUD HANDLERS     ==================

    /**
     * Add handler:
     * Opens the editor dialog in Add mode (original = null).
     * If user confirms, adds question through controller and reloads table.
     */
    /* Add flow */
    private void onAdd() {
        Question q = showEditorDialog(null);
        if (q != null) {
            controller.add(q);
            model.reload(controller.list());
        }
    }

    /**
     * Edit handler:
     * Requires a row selection.
     * Opens editor dialog with existing question values pre-filled.
     * On confirm, replaces old question with updated one via controller.
     */
    /* Edit flow */
    private void onEdit() {
        int row = table.getSelectedRow();

        /* Validation: must select a row */
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to edit.");
            return;
        }

        /* Get the selected question */
        Question selected = model.getAt(row);

        /* Open dialog with existing data */
        Question updated = showEditorDialog(selected);

        /* Replace if user confirmed */
        if (updated != null) {
            controller.replace(selected.id(), updated);
            model.reload(controller.list());
        }
    }

    /**
     * Delete handler:
     * Requires a row selection and a confirmation dialog.
     * Controller enforces MIN_QUESTIONS limit through SysData.
     */
    /* Delete flow */
    private void onDelete() {
        int row = table.getSelectedRow();

        /* Validation: must select a row */
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        Question selected = model.getAt(row);

        /* Ask confirmation */
        int ok = JOptionPane.showConfirmDialog(
                this,
                "Delete question: " + selected.id() + " ?",
                "Confirm",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (ok == JOptionPane.OK_OPTION) {
            boolean removed = controller.delete(selected.id());

            /* If delete failed, explain MIN_QUESTIONS restriction */
            if (!removed) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cannot delete. System must keep at least 20 questions.",
                        "Delete Blocked",
                        JOptionPane.WARNING_MESSAGE
                );
            }

            model.reload(controller.list());
        }
    }

    // ==========================================================
    //                MODAL EDITOR DIALOG
    // ==========================================================

    /**
     * Opens a modal dialog for adding or editing a question.
     *
     * @param original existing Question to edit, or null for Add mode.
     * @return a newly built Question object, or null if user cancelled.
     */
    /* Dialog creation + validation logic */
    private Question showEditorDialog(Question original) {

        // ---------- Fields (created once) ----------

        String suggestedId = (original == null)
                ? String.valueOf(controller.nextId())
                : original.id();

        JTextField tfId = new JTextField(suggestedId, 20);
        tfId.setEditable(false);

        JTextField tfText = new JTextField(
                original == null ? "" : original.text(), 30
        );

        // Difficulty: placeholder + real values
        String[] levelItems = {"-- Select level --", "EASY", "MEDIUM", "HARD", "MASTER"};
        JComboBox<String> cbLevel = new JComboBox<>(levelItems);

        if (original != null) {
            cbLevel.setSelectedItem(original.level().name());
        } else {
            cbLevel.setSelectedIndex(0);
        }

        JTextField tfOpt1 = new JTextField(original == null ? "" : original.options().get(0), 20);
        JTextField tfOpt2 = new JTextField(original == null ? "" : original.options().get(1), 20);
        JTextField tfOpt3 = new JTextField(original == null ? "" : original.options().get(2), 20);
        JTextField tfOpt4 = new JTextField(original == null ? "" : original.options().get(3), 20);

        // Correct answer A–D with placeholder
        String[] correctItems = {"-- Select correct answer --", "A", "B", "C", "D"};
        JComboBox<String> cbCorrect = new JComboBox<>(correctItems);

        if (original != null) {
            cbCorrect.setSelectedIndex(original.correctIndex() + 1); // 0→A, 1→B, ...
        } else {
            cbCorrect.setSelectedIndex(0); // placeholder for NEW question
        }

        // ---------- Layout panel (also reused) ----------
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("ID:"));                   p.add(tfId);
        p.add(new JLabel("Text:"));                 p.add(tfText);
        p.add(new JLabel("Level:"));                p.add(cbLevel);
        p.add(new JLabel("Option 1:"));             p.add(tfOpt1);
        p.add(new JLabel("Option 2:"));             p.add(tfOpt2);
        p.add(new JLabel("Option 3:"));             p.add(tfOpt3);
        p.add(new JLabel("Option 4:"));             p.add(tfOpt4);
        p.add(new JLabel("Correct Answer [A–D]:")); p.add(cbCorrect);

     // ---------- Dialog + validation loop ----------
        while (true) {

            int res = JOptionPane.showConfirmDialog(
                    this, p,
                    (original == null ? "Add Question" : "Edit Question"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (res != JOptionPane.OK_OPTION) {
                // user pressed Cancel or closed the dialog
                return null;
            }

            // Read current values
            String id       = tfId.getText().trim();
            String text     = tfText.getText().trim();
            String levelStr = (String) cbLevel.getSelectedItem();
            String correctStr = (String) cbCorrect.getSelectedItem();

            String o1 = tfOpt1.getText().trim();
            String o2 = tfOpt2.getText().trim();
            String o3 = tfOpt3.getText().trim();
            String o4 = tfOpt4.getText().trim();

            // ---- VALIDATION ----

            // 1. All fields filled
            if (text.isEmpty() || o1.isEmpty() || o2.isEmpty() || o3.isEmpty() || o4.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "All fields (Text and all 4 options) must be filled.",
                        "Missing data",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            // 2. All 4 options must be different (case-insensitive)
            java.util.Set<String> uniq = new java.util.HashSet<>();
            uniq.add(o1.toLowerCase());
            uniq.add(o2.toLowerCase());
            uniq.add(o3.toLowerCase());
            uniq.add(o4.toLowerCase());

            if (uniq.size() < 4) {
                JOptionPane.showMessageDialog(
                        this,
                        "All 4 answer options must be different.\n" +
                        "Please change the duplicate answers.",
                        "Duplicate answers",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            // 3. Difficulty chosen
            if (levelStr == null || levelStr.equals("-- Select level --")) {
                JOptionPane.showMessageDialog(
                        this,
                        "You must choose a difficulty level.",
                        "Missing difficulty level",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            // 4. Correct answer chosen
            if (correctStr == null || correctStr.equals("-- Select correct answer --")) {
                JOptionPane.showMessageDialog(
                        this,
                        "You must choose the correct answer (A–D).",
                        "Missing correct answer",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }
         // ---- All validations passed ----

            int correctIndex = switch (correctStr) {
                case "A" -> 0;
                case "B" -> 1;
                case "C" -> 2;
                case "D" -> 3;
                default -> -1; // shouldn't happen
            };

            java.util.List<String> opts = new ArrayList<>(4);
            opts.add(o1);
            opts.add(o2);
            opts.add(o3);
            opts.add(o4);

            QuestionLevel lvl = QuestionLevel.valueOf(levelStr);

            return new Question(id, text, opts, correctIndex, lvl);
        }
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
        public void reload(List<Question> qs){
            data = new ArrayList<>(qs);

            // ----- SORT BY NUMERIC ID ASC -----
            data.sort((a, b) -> {
                try {
                    int idA = Integer.parseInt(a.id());
                    int idB = Integer.parseInt(b.id());
                    return Integer.compare(idA, idB);
                } catch (NumberFormatException e) {
                    // fallback to string compare
                    return a.id().compareTo(b.id());
                }
            });

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
                case 7 -> String.valueOf((char) ('A' + q.correctIndex())); // 0→A, 1→B...
                default -> "";
            };
        }


        @Override
        public boolean isCellEditable(int r, int c) {
            return false; // table is read-only
        }
    }
}

