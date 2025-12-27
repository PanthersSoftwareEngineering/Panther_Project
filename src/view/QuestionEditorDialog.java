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
    private final JSpinner spCorrect;

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

        spCorrect = new JSpinner(new SpinnerNumberModel(isAddMode ? 0 : original.correctIndex(), 0, 3, 1));
        styleInput(spCorrect);

        // Build UI Rows
        // Row 0: ID
        addStyledRow(inputGrid, gbc, "ID (Auto):", tfId, 0, 0, 1);

        // Row 1: Question Text (Spans 3 grid columns to reach the end)
        addStyledRow(inputGrid, gbc, "Question Text:", scrollText, 0, 1, 3);

        // Row 2: Level (Left) and Correct (Right)
        addStyledRow(inputGrid, gbc, "Level:", cbLevel, 0, 2, 1);
        addStyledRow(inputGrid, gbc, "Correct (0-3):", spCorrect, 1, 2, 1);

        // Row 3: Option 1 (Left) and Option 2 (Right)
        addStyledRow(inputGrid, gbc, "Option 1 (A):", tfOpt1, 0, 3, 1);
        addStyledRow(inputGrid, gbc, "Option 2 (B):", tfOpt2, 1, 3, 1);

        // Row 4: Option 3 (Left) and Option 4 (Right)
        addStyledRow(inputGrid, gbc, "Option 3 (C):", tfOpt3, 0, 4, 1);
        addStyledRow(inputGrid, gbc, "Option 4 (D):", tfOpt4, 1, 4, 1);

        root.add(inputGrid, BorderLayout.CENTER);

        // --- Buttons Row (Using BaseGameFrame.RoundedButton) ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        buttonsPanel.setOpaque(false);

        // IMPORTANT: Accessing static inner class from BaseGameFrame
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
        } else if (comp instanceof JSpinner sp) {
            sp.setBorder(new LineBorder(GOLD_ACCENT, 1));
            JFormattedTextField field = ((JSpinner.DefaultEditor) sp.getEditor()).getTextField();
            field.setBackground(FIELD_BG);
            field.setForeground(Color.WHITE);
        }
    }

    private void addStyledRow(JPanel parent, GridBagConstraints gbc, String labelText, JComponent component, int gridx, int gridy, int width) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(GOLD_ACCENT);
        
        // --- Label Configuration ---
        gbc.gridx = gridx * 2;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;      // Reset to 1 for the label
        gbc.weightx = 0.0;      // Labels don't stretch
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 10, 8, 5);
        parent.add(label, gbc);

        // --- Component Configuration ---
        gbc.gridx = gridx * 2 + 1;
        gbc.gridy = gridy;
        gbc.gridwidth = width;  // Set the specific width (1 or 3)
        gbc.weightx = 1.0;      // Stretch the component
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill the area
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);
        parent.add(component, gbc);
        
        // Reset gridwidth to 1 after adding the component for the next call
        gbc.gridwidth = 1;
    }

    private void onSave() {
        // Collect data and set result Question object...
        this.result = new Question(tfId.getText(), taText.getText(), 
                      List.of(tfOpt1.getText(), tfOpt2.getText(), tfOpt3.getText(), tfOpt4.getText()), 
                      (Integer)spCorrect.getValue(), 
                      QuestionLevel.valueOf(cbLevel.getSelectedItem().toString()));
        dispose();
    }
}