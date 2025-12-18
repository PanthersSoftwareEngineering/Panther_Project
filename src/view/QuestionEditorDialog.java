package view;

import model.Question;
import model.QuestionLevel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom styled modal dialog for adding or editing a single Question object.
 * This replaces the standard, unstyled JOptionPane formerly used.
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

    // --- Styling ---
    private static final Color DARK_BG = new Color(20, 30, 35, 250); // Darker BG
    private static final Color ACCENT_COLOR = new Color(80, 200, 180); // Turquoise
    private static final Color TEXT_FIELD_BG = new Color(30, 45, 50);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    /**
     * Shows the modal editor dialog.
     *
     * @param owner The parent frame.
     * @param original The question to edit, or null for add mode.
     * @param maxExistingId The highest numeric ID found in the system (used for Add mode).
     * @return The new or updated Question object if saved, or null if canceled.
     */
    public static Question showDialog(JFrame owner, Question original, int maxExistingId) {
        QuestionEditorDialog dialog = new QuestionEditorDialog(owner, original, maxExistingId);
        dialog.setVisible(true);
        return dialog.result;
    }

    private QuestionEditorDialog(JFrame owner, Question original, int maxExistingId) {
        super(owner, original == null ? "Add Question" : "Edit Question", true);
        this.isAddMode = (original == null);
        
        // Use the local RoundedButton defined below
        RoundedButton btnSave = new RoundedButton("Save", 160, 50, 18);
        RoundedButton btnCancel = new RoundedButton("Cancel", 160, 50, 18);
        
        // --- Setup basic dialog properties ---
        setSize(750, 650);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 4, true));

        // --- Initialize Components ---

        // ID Field
        String idValue = isAddMode ? String.valueOf(maxExistingId + 1) : original.id();
        tfId = new JTextField(idValue);
        tfId.setEditable(false);
        styleInput(tfId, true); 

        // Question Text Area
        taText = new JTextArea(original == null ? "" : original.text(), 4, 30);
        taText.setLineWrap(true);
        taText.setWrapStyleWord(true);
        styleInput(taText, false);
        JScrollPane scrollText = new JScrollPane(taText);
        scrollText.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1)); 
        scrollText.getViewport().setBackground(TEXT_FIELD_BG);
        
        // Level Combo Box
        cbLevel = new JComboBox<>(new String[]{"EASY", "MEDIUM", "HARD", "MASTER"});
        if (original != null) cbLevel.setSelectedItem(original.level().name());
        styleInput(cbLevel, false);
        
        // Option Text Fields
        tfOpt1 = createOptionField(original, 0);
        tfOpt2 = createOptionField(original, 1);
        tfOpt3 = createOptionField(original, 2);
        tfOpt4 = createOptionField(original, 3);
        
        // Correct Index Spinner
        SpinnerNumberModel snm = new SpinnerNumberModel(
                original == null ? 0 : original.correctIndex(), 0, 3, 1
        );
        spCorrect = new JSpinner(snm);
        styleInput(spCorrect, false);

        // --- Layout the components ---
        
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(DARK_BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(original == null ? "ADD NEW QUESTION" : "EDIT QUESTION ID: " + idValue);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(titleLabel, BorderLayout.NORTH);
        
        // Grid for inputs
        JPanel inputGrid = new JPanel(new GridBagLayout());
        inputGrid.setOpaque(false);
        inputGrid.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 5, 8, 5); 

        // Helper to add label and component
        addStyledRow(inputGrid, gbc, "ID (Auto):", tfId, 0, 0, 1);
        addStyledRow(inputGrid, gbc, "Question Text:", scrollText, 0, 1, 3); 
        addStyledRow(inputGrid, gbc, "Level:", cbLevel, 0, 2, 1);
        addStyledRow(inputGrid, gbc, "Correct Index [0..3]:", spCorrect, 1, 2, 1);
        
        gbc.gridwidth = 1; 
        addStyledRow(inputGrid, gbc, "Option 1:", tfOpt1, 0, 3, 1);
        addStyledRow(inputGrid, gbc, "Option 2:", tfOpt2, 1, 3, 1);
        addStyledRow(inputGrid, gbc, "Option 3:", tfOpt3, 0, 4, 1);
        addStyledRow(inputGrid, gbc, "Option 4:", tfOpt4, 1, 4, 1);

        root.add(inputGrid, BorderLayout.CENTER);

        // Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnCancel);
        root.add(buttonsPanel, BorderLayout.SOUTH);
        
        setContentPane(root);
        
        // --- Action Listeners ---
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> onCancel());
    }
    
    // --- Helper methods for styling and component creation ---

    private JTextField createOptionField(Question original, int index) {
        String text = (original == null || original.options().size() <= index) 
                      ? "" 
                      : original.options().get(index);
        JTextField tf = new JTextField(text);
        styleInput(tf, false);
        return tf;
    }
    
    private void styleInput(JComponent comp, boolean readOnly) {
        comp.setFont(INPUT_FONT);
        comp.setForeground(Color.WHITE);
        comp.setBackground(TEXT_FIELD_BG);
        
        if (comp instanceof JTextField tf) {
            tf.setCaretColor(ACCENT_COLOR); 
            Border border = BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_COLOR.darker(), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            );
            tf.setBorder(readOnly ? BorderFactory.createLineBorder(Color.GRAY.darker()) : border);
        } else if (comp instanceof JTextArea ta) {
             ta.setCaretColor(ACCENT_COLOR);
             ta.setMargin(new Insets(5, 10, 5, 10));
        } else if (comp instanceof JComboBox<?> cb) {
            cb.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1, true));
        } else if (comp instanceof JSpinner sp) {
            sp.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1, true));
            JFormattedTextField field = ((JSpinner.DefaultEditor) sp.getEditor()).getTextField();
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setBackground(TEXT_FIELD_BG);
            field.setForeground(Color.WHITE);
            field.setCaretColor(ACCENT_COLOR);
        }
    }
    
    private void addStyledRow(JPanel parent, GridBagConstraints gbc, String labelText, JComponent component, int gridx, int gridy, int width) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(ACCENT_COLOR);
        
        gbc.gridx = gridx * 2;
        gbc.gridy = gridy;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        parent.add(label, gbc);
        
        gbc.gridx = gridx * 2 + 1;
        gbc.gridy = gridy;
        gbc.weightx = 1.0;
        gbc.gridwidth = width;
        gbc.anchor = GridBagConstraints.EAST;
        parent.add(component, gbc);
    }

    // --- Action Handlers ---

    private void onSave() {
        // Validation logic
        String id = tfId.getText().trim();
        String text = taText.getText().trim();
        String lvlS = cbLevel.getSelectedItem().toString();
        
        int correct;
        try {
            correct = (Integer) spCorrect.getValue();
        } catch (Exception e) {
            showError("Invalid Correct Index value.");
            return;
        }

        String opt1 = tfOpt1.getText().trim();
        String opt2 = tfOpt2.getText().trim();
        String opt3 = tfOpt3.getText().trim();
        String opt4 = tfOpt4.getText().trim();

        if (text.isEmpty() || opt1.isEmpty() || opt2.isEmpty() || opt3.isEmpty() || opt4.isEmpty()) {
            showError("All fields must be filled: Question Text and all 4 Options.");
            return;
        }

        if (correct < 0 || correct > 3) {
            showError("Correct index must be between 0 and 3.");
            return;
        }

        List<String> opts = new ArrayList<>(4);
        opts.add(opt1); opts.add(opt2); opts.add(opt3); opts.add(opt4);

        QuestionLevel lvl = QuestionLevel.valueOf(lvlS);
        
        this.result = new Question(id, text, opts, correct, lvl);
        dispose();
    }

    private void onCancel() {
        this.result = null; 
        dispose();
    }
    
    /** Shows the error message using the project's custom styling */
    private void showError(String message) {
    	StyledAlertDialog.show(
                (JFrame) SwingUtilities.getWindowAncestor(this), // transform the basic class alert to this designed one (owner)
                "Input Error", 
                message, 
                true // isError = true
            );
    }
    
    // =========================================================
    // INNER CLASS: RoundedButton (Copied locally for self-containment)
    // =========================================================
    public static class RoundedButton extends JButton {

        private final Color baseFill  = new Color(20, 24, 32, 235); 
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final Color borderClr = new Color(80, 200, 180); // Turquoise ACCENT
        private final int radius = 65;

        public RoundedButton(String text, int width, int height, int fontSize) {
            super(text);

            setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            setForeground(Color.WHITE);

            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);

            setPreferredSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = getModel().isRollover() ? hoverFill : baseFill;
            int w = getWidth();
            int h = getHeight();

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            g2.setStroke(new BasicStroke(4f));
            g2.setColor(borderClr); 
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}