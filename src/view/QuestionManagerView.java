package view;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import controller.AppController;
import controller.QuestionController;
import model.Question;
import model.QuestionLevel;

/**
 * Management window for all questions. Inherits full-screen behavior from BaseGameFrame 
 * and applies the Dark Teal/Turquoise styling.
 */
public class QuestionManagerView extends BaseGameFrame { 

    private final QuestionController controller;
    
    // !!! תיקון: הגדרת טייפ ספציפי לטבלה לשימוש במתודות המותאמות !!!
    private final QuestionTable table; 
    
    private final QuestionTableModel model;

    // --- Custom Colors  ---
    private static final Color DARK_BG = new Color(20, 30, 35, 240); 
    private static final Color ACCENT_COLOR = new Color(80, 200, 180); 
    private static final Color TABLE_BG = new Color(30, 45, 50);

    public QuestionManagerView(QuestionController controller) {
        super(AppController.getInstance(), "Question Manager");
        this.controller = controller;

        // ===== 1. Main Content Frame (Dark with Turquoise border, Centered) =====
        JPanel centerFrame = new JPanel();
        centerFrame.setLayout(new BorderLayout(15, 15));
        centerFrame.setBackground(DARK_BG);
        centerFrame.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 4, true));
        
        // Root panel for centering the centerFrame within the full-screen BaseGameFrame
        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setOpaque(true);
        rootPanel.setBackground(DARK_BG.darker());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(50, 50, 50, 50);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; 
        
        rootPanel.add(centerFrame, gbc);
        setContentPane(rootPanel);


        // ===== 2. Top Section (Title and Back Button) =====
        JPanel top = new JPanel(new BorderLayout(15, 0));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        // Title Label
        JLabel title = new JLabel("Question Management", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(ACCENT_COLOR);
        top.add(title, BorderLayout.WEST);

        // Back Button
        ButtonStyled backBtn = new ButtonStyled("Back to Main");
        backBtn.setPreferredSize(new Dimension(220, 55));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        backBtn.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });
        top.add(backBtn, BorderLayout.EAST);
        centerFrame.add(top, BorderLayout.NORTH);
        
        // ===== 3. Table in Center (Styled JTable) =====
        model = new QuestionTableModel(controller.list());
        
        // !!! תיקון: יצירת המופע של הטבלה המותאמת !!!
        table = new QuestionTable(model); 
        
        // Styling the table
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setForeground(Color.WHITE);
        table.setBackground(TABLE_BG);
        
        // Setting minimal high for table
        table.setRowHeight(30); 
        Color newSelectionColor = new Color(30, 70, 70); 
        table.setSelectionBackground(newSelectionColor); 
        table.setSelectionForeground(Color.WHITE);
        
        // Styling the table header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(TABLE_BG.brighter());
        table.getTableHeader().setForeground(ACCENT_COLOR.brighter());
        
        // Scroll pane styling
        JScrollPane scrollPane = new JScrollPane(table);
        // activaring horizontal scroller
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // activaring higeth scroller
        scrollPane.setBorder(new LineBorder(ACCENT_COLOR, 2, false));
        scrollPane.getViewport().setBackground(TABLE_BG); 
        
        centerFrame.add(scrollPane, BorderLayout.CENTER);

        
        // ===== 4. Bottom Row with CRUD Buttons (using RoundedButton) =====
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        buttonsRow.setOpaque(false); 

        // Use RoundedButton
        RoundedButton btnAdd = new RoundedButton("Add Question", 200, 60, 20);
        RoundedButton btnEdit = new RoundedButton("Edit Selected", 200, 60, 20);
        RoundedButton btnDelete = new RoundedButton("Delete Selected", 200, 60, 20);

        buttonsRow.add(btnAdd);
        buttonsRow.add(btnEdit);
        buttonsRow.add(btnDelete);
        centerFrame.add(buttonsRow, BorderLayout.SOUTH);

        // --- Fixing Table Dimensions ONCE ---
        setupTableColumnWidths();
        
        // Call the optimal calculation immediately after layout is set
        SwingUtilities.invokeLater(() -> table.calculateOptimalDimensions());
        
        // Wire up actions.
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
    }
    
    /**
     * Sets fixed widths for certain columns and larger relative widths for text columns.
     * This is the initial setting before the optimal calculation runs.
     */
    private void setupTableColumnWidths() {
        if (table.getColumnModel().getColumnCount() < 8) return;

        // set defauld width for shorter rows
        // 0: ID, 2: Level, 7: Correct
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setMaxWidth(90);

        table.getColumnModel().getColumn(7).setPreferredWidth(60);
        table.getColumnModel().getColumn(7).setMaxWidth(70);
        
        // setting width sizes (Text and Options) - setting a good initial guess
        table.getColumnModel().getColumn(1).setPreferredWidth(300); // Text Column
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
    }
    
    /** Shows the window. */
    public void showSelf(){ setVisible(true); }

    // =========================================================
    // CRUD Handlers
    // =========================================================
    
    /**
     * Handler for the "Add" button.
     */
    private void onAdd(){
        int maxId = 0;
        for (Question q : controller.list()){
            try{
                int idVal = Integer.parseInt(q.id());
                if (idVal > maxId) maxId = idVal;
            } catch(NumberFormatException ignored){ }
        }
        
        // Uses the separate QuestionEditorDialog class
        Question q = QuestionEditorDialog.showDialog(this, null, maxId); 
        
        if (q != null){
            controller.add(q);
            model.reload(controller.list());
            // recalculating table width and high again
            SwingUtilities.invokeLater(() -> table.calculateOptimalDimensions());
        }
    }

    /**
     * Handler for the "Edit" button.
     */
    private void onEdit(){
        int row = table.getSelectedRow();
        if (row < 0){
        	StyledAlertDialog.show(
                    this, 
                    "Missing Selection", 
                    "Select a row to edit.", 
                    false 
            );
            return;
        }
        Question selected = model.getAt(row);
        
        // Uses the separate QuestionEditorDialog class
        Question updated = QuestionEditorDialog.showDialog(this, selected, 0); 
        
        if (updated != null){
            controller.replace(selected.id(), updated);
            model.reload(controller.list());
            // recalculating table width and high again
            SwingUtilities.invokeLater(() -> table.calculateOptimalDimensions());
        }
    }

    /**
     * Handler for the "Delete" button.
     */
    private void onDelete(){
    	int row = table.getSelectedRow();
        if (row < 0){
            // Using StyledAlertDialog 
            StyledAlertDialog.show(this, "Missing Selection", "Select a row to delete.", false); 
            return;
        }

        int total = model.getRowCount();
        if (total <= 20){
            StyledAlertDialog.show(
                    this, 
                    "Cannot Delete Question", 
                    "You must keep at least 20 questions in the system. Delete is not allowed.", 
                    true 
            );
            return;
        }

        Question selected = model.getAt(row);
        int ok = StyledConfirmDialog.show( 
                this,
                "Are you sure you want to delete question: "+selected.id()+" ( " + selected.text().substring(0, Math.min(selected.text().length(), 20)) + ") ?) ?",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (ok == JOptionPane.OK_OPTION){
            controller.delete(selected.id());
            model.reload(controller.list());
            // recalculating table width and high again
            SwingUtilities.invokeLater(() -> table.calculateOptimalDimensions());
        }
    }

    // =========================================================
    // INNER CLASSES FOR STYLING (Copied locally)
    // =========================================================
    
    /** Rounded button */
    public static class RoundedButton extends JButton {

        private final Color baseFill  = new Color(20, 24, 32, 235); 
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final Color borderClr = new Color(80, 200, 180); 
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
    
    /** ButtonStyled */
    public static class ButtonStyled extends RoundedButton {
        public ButtonStyled(String text) {
            super(text, 320, 90, 32); 
        }
    }

    // =========================================================
    // TABLE MODEL
    // =========================================================
    
    /**
     * Table model that adapts Question objects to a tabular view.
     */
    private static class QuestionTableModel extends AbstractTableModel {
        private final String[] cols = {
                "ID","Text","Level","A (Opt1)","B (Opt2)","C (Opt3)","D (Opt4)","Correct"
        };

        private List<Question> data = new ArrayList<>();

        QuestionTableModel(List<Question> initial){
            reload(initial);
        }

        public void reload(List<Question> qs){
            data = new ArrayList<>(qs);
            data.sort((a, b) -> {
                try {
                    int idA = Integer.parseInt(a.id());
                    int idB = Integer.parseInt(b.id());
                    return Integer.compare(idA, idB);
                } catch (NumberFormatException e) {
                    return a.id().compareTo(b.id());
                }
            });
            fireTableDataChanged();
        }

        public Question getAt(int row){ return data.get(row); }
        @Override public int getRowCount(){ return data.size(); }
        @Override public int getColumnCount(){ return cols.length; }
        @Override public String getColumnName(int c){ return cols[c]; }
        
        @Override
        public Object getValueAt(int row, int col){
            Question q = data.get(row);
            return switch(col){
                case 0 -> q.id();
                case 1 -> q.text();
                case 2 -> q.level().name();
                case 3 -> q.options().get(0);
                case 4 -> q.options().get(1);
                case 5 -> q.options().get(2);
                case 6 -> q.options().get(3);
                case 7 -> {
                	// Converts 0 -> A, 1 -> B, 2 -> C, 3 -> D
                	int correctIdx = q.correctIndex();
                	yield switch(correctIdx) {
                    	case 0 -> "A";
                    	case 1 -> "B";
                    	case 2 -> "C";
                    	case 3 -> "D";
                    	default -> String.valueOf(correctIdx);
                	};
                }
                default -> "";
            };
        }
        @Override public boolean isCellEditable(int r,int c){ return false; }
    }
}