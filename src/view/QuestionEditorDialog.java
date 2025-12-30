package view;

import model.Question;
import model.QuestionLevel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Styled modal dialog for adding/editing questions.
 * Fully aligned with Navy/Gold theme and BaseGameFrame buttons.
 */
public class QuestionEditorDialog extends JDialog {

    private Question result = null;
    private final boolean isAddMode;

    // --- Components ---
    private final JTextField tfId;
    private final JTextArea taText;
    private final JComboBox<String> cbLevel;
    private final JTextField tfOpt1, tfOpt2, tfOpt3, tfOpt4;
    private final JComboBox<String> cbCorrect;

    // --- Navy/Gold Styling (Matching History/Main Menu) ---
    private static final Color DARK_BG = new Color(15, 18, 40, 250);
    private static final Color GOLD_ACCENT = new Color(255, 190, 60);
    private static final Color FIELD_BG = new Color(30, 32, 70);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    public static Question showDialog(JFrame owner, Question original, int maxExistingId) {
        QuestionEditorDialog dialog = new QuestionEditorDialog(owner, original, maxExistingId);
        dialog.setVisible(true);
        return dialog.result;
    }

    private QuestionEditorDialog(JFrame owner, Question original, int maxExistingId) {
        super(owner, original == null ? "Add Question" : "Edit Question", true);
        this.isAddMode = (original == null);

        setUndecorated(true);
        setSize(750, 650);
        setLocationRelativeTo(owner);

        // --- Layout Setup ---
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(DARK_BG);
        root.setBorder(new LineBorder(GOLD_ACCENT, 4, true));

        // Header
        JLabel titleLabel = new JLabel(isAddMode ? "ADD NEW QUESTION" : "EDIT QUESTION");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(GOLD_ACCENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        root.add(titleLabel, BorderLayout.NORTH);

        // Grid for inputs
        JPanel inputGrid = new JPanel(new GridBagLayout());
        inputGrid.setOpaque(false);
        inputGrid.setBorder(new EmptyBorder(10, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // --- Component Initialization ---
        String idValue = isAddMode ? String.valueOf(maxExistingId + 1) : original.id();
        tfId = new JTextField(idValue);
        tfId.setEditable(false);
        styleInput(tfId);

        taText = new JTextArea(isAddMode ? "" : original.text(), 4, 30);
        taText.setLineWrap(true);
        taText.setWrapStyleWord(true);
        styleInput(taText);
        JScrollPane scrollText = new JScrollPane(taText);
        scrollText.setBorder(new LineBorder(GOLD_ACCENT, 1));

        cbLevel = new JComboBox<>(new String[]{"EASY", "MEDIUM", "HARD", "MASTER"});
        if (!isAddMode) cbLevel.setSelectedItem(original.level().name());
        styleInput(cbLevel);

        tfOpt1 = createOptionField(original, 0);
        tfOpt2 = createOptionField(original, 1);
        tfOpt3 = createOptionField(original, 2);
        tfOpt4 = createOptionField(original, 3);

        // Create a dropdown with A, B, C, D
        cbCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        // If editing, set the current correct answer
        if (!isAddMode) {
            cbCorrect.setSelectedIndex(original.correctIndex());
        }
        styleInput(cbCorrect);

        // Build UI Rows
        addStyledRow(inputGrid, gbc, "ID (Auto):", tfId, 0, 0, 1);
        addStyledRow(inputGrid, gbc, "Question Text:", scrollText, 0, 1, 3);
        addStyledRow(inputGrid, gbc, "Level:", cbLevel, 0, 2, 1);
        addStyledRow(inputGrid, gbc, "Correct (A-D):", cbCorrect, 1, 2, 1);
        addStyledRow(inputGrid, gbc, "Option 1 (A):", tfOpt1, 0, 3, 1);
        addStyledRow(inputGrid, gbc, "Option 2 (B):", tfOpt2, 1, 3, 1);
        addStyledRow(inputGrid, gbc, "Option 3 (C):", tfOpt3, 0, 4, 1);
        addStyledRow(inputGrid, gbc, "Option 4 (D):", tfOpt4, 1, 4, 1);

        root.add(inputGrid, BorderLayout.CENTER);

        // --- Buttons Row (Using BaseGameFrame.RoundedButton) ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        buttonsPanel.setOpaque(false);

        BaseGameFrame.RoundedButton btnSave = new BaseGameFrame.RoundedButton("Save", 180, 55, 20);
        BaseGameFrame.RoundedButton btnCancel = new BaseGameFrame.RoundedButton("Cancel", 180, 55, 20);

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnCancel);
        root.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JTextField createOptionField(Question original, int index) {
        String text = (original == null || original.options().size() <= index) ? "" : original.options().get(index);
        JTextField tf = new JTextField(text);
        styleInput(tf);
        return tf;
    }

    private void styleInput(JComponent comp) {
        comp.setFont(INPUT_FONT);
        comp.setForeground(Color.WHITE);
        comp.setBackground(FIELD_BG);

        if (comp instanceof JTextField tf) {
            tf.setCaretColor(GOLD_ACCENT);
            tf.setBorder(new LineBorder(GOLD_ACCENT, 1));
        } else if (comp instanceof JComboBox<?> cb) {
            cb.setBorder(new LineBorder(GOLD_ACCENT, 1));
            // This ensures the items inside the dropdown look good too
            cb.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    l.setBackground(isSelected ? GOLD_ACCENT : FIELD_BG);
                    l.setForeground(isSelected ? Color.BLACK : Color.WHITE);
                    return l;
                }
            });
        }
    
    }

    private void addStyledRow(JPanel parent, GridBagConstraints gbc, String labelText, JComponent component,
                              int gridx, int gridy, int width) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(GOLD_ACCENT);

        gbc.gridx = gridx * 2;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 10, 8, 5);
        parent.add(label, gbc);

        gbc.gridx = gridx * 2 + 1;
        gbc.gridy = gridy;
        gbc.gridwidth = width;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);
        parent.add(component, gbc);

        gbc.gridwidth = 1;
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Show validation error in the same theme (visible on top of this dialog).
     */
    private void showValidationError(String msg) {
        JFrame owner = (getOwner() instanceof JFrame jf) ? jf : null;
        StyledAlertDialog.show(owner, "Input Error", msg, true);
    }

    private void onSave() {
        String id = tfId.getText();
        String text = norm(taText.getText());

        String o1 = norm(tfOpt1.getText());
        String o2 = norm(tfOpt2.getText());
        String o3 = norm(tfOpt3.getText());
        String o4 = norm(tfOpt4.getText());

        // --- Validation rules ---
        if (text.isEmpty()) {
            showValidationError("Question text cannot be empty.");
            taText.requestFocusInWindow();
            return;
        }

        if (o1.isEmpty() || o2.isEmpty() || o3.isEmpty() || o4.isEmpty()) {
            showValidationError("All 4 options must be filled (A, B, C, D).");
            if (o1.isEmpty()) tfOpt1.requestFocusInWindow();
            else if (o2.isEmpty()) tfOpt2.requestFocusInWindow();
            else if (o3.isEmpty()) tfOpt3.requestFocusInWindow();
            else tfOpt4.requestFocusInWindow();
            return;
        }

        var opts = List.of(o1, o2, o3, o4);
        long distinctCount = opts.stream().map(String::toLowerCase).distinct().count();
        if (distinctCount < 4) {
            showValidationError("Options must be different from each other.");
            return;
        }

        // Get the index (0 for A, 1 for B, etc.)
        int correctIdx = cbCorrect.getSelectedIndex(); 

        // Build the result object (removed duplicate assignment)
        this.result = new Question(
                id,
                text,
                opts,
                correctIdx, // Use the correct variable name here
                QuestionLevel.valueOf(cbLevel.getSelectedItem().toString())
        );
        
        dispose();
    }
}
