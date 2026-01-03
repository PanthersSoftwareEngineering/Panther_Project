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

/**
 * Management window for all questions. 
 **/
public class QuestionManagerView extends BaseGameFrame { 

    private final QuestionController controller;
    private final QuestionTable table; 
    private final QuestionTableModel model;

    private static final Color TABLE_BG = new Color(25, 28, 60); 
    private static final Color TABLE_HEADER_BG = new Color(30, 32, 70); 

    public QuestionManagerView(QuestionController controller) {
        super(AppController.getInstance(), "Question Manager");
        this.controller = controller;
        // =====  Background Setup =====
        Image bgImage = GameAssets.MATCH_BACKGROUND; 
        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new GridBagLayout());
        setContentPane(bgPanel);

        // =====  Main Content Card =====
        JPanel centerFrame = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 180)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        centerFrame.setOpaque(false); 
        centerFrame.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(50, 50, 50, 50);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; 
        bgPanel.add(centerFrame, gbc);

        // ===== Top Section (Centered Title and Back Button) =====
        JPanel top = new JPanel(new BorderLayout(15, 0));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // CENTERED TITLE
        JLabel title = new JLabel("Question Management", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 45));
        title.setForeground(UIStyles.ACCENT);
        top.add(title, BorderLayout.CENTER);

        // BACK BUTTON (Using RoundedButton inherited from BaseGameFrame)
        RoundedButton backBtn = new RoundedButton("Back to Main", 220, 55, 20);
        backBtn.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });
        top.add(backBtn, BorderLayout.EAST);
        
        // Filler panel for perfect centering
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        filler.setPreferredSize(new Dimension(220, 55));
        top.add(filler, BorderLayout.WEST);

        centerFrame.add(top, BorderLayout.NORTH);
        
        // ===== Table Setup =====
        model = new QuestionTableModel(controller.list());
        table = new QuestionTable(model); 
        
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setForeground(Color.WHITE);
        table.setBackground(TABLE_BG);
        table.setSelectionBackground(new Color(80, 120, 255, 200)); 
        table.setGridColor(new Color(255, 255, 255, 40));
        
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(new LineBorder(UIStyles.ACCENT, 3, true));
        scrollPane.getViewport().setBackground(TABLE_BG); 
        centerFrame.add(scrollPane, BorderLayout.CENTER);

        // ===== Bottom CRUD Buttons (Using RoundedButton from BaseGameFrame) =====
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        buttonsRow.setOpaque(false); 

        RoundedButton btnAdd = new RoundedButton("Add Question", 220, 60, 20);
        RoundedButton btnEdit = new RoundedButton("Edit Selected", 220, 60, 20);
        RoundedButton btnDelete = new RoundedButton("Delete Selected", 220, 60, 20);

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());

        buttonsRow.add(btnAdd);
        buttonsRow.add(btnEdit);
        buttonsRow.add(btnDelete);
        centerFrame.add(buttonsRow, BorderLayout.SOUTH);

        setupTableColumnWidths();
        SwingUtilities.invokeLater(() -> table.calculateOptimalDimensions());
    }

    // --- HELPER METHODS AND CRUD HANDLERS ---
    
    private void setupTableColumnWidths() {
        if (table.getColumnModel().getColumnCount() < 8) return;
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
    }

    private void onAdd() {
        int maxId = 0;
        for (Question q : controller.list()) {
            try {
                int idVal = Integer.parseInt(q.id());
                if (idVal > maxId) maxId = idVal;
            } catch(NumberFormatException ignored) { }
        }
        Question q = QuestionEditorDialog.showDialog(this, null, maxId); 
        if (q != null) {
            controller.add(q);
            model.reload(controller.list());
            SwingUtilities.invokeLater(() -> table.calculateOptimalDimensions());
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            StyledAlertDialog.show(this, "Missing Selection", "Select a row to edit.", false);
            return;
        }
        Question selected = model.getAt(row);
        Question updated = QuestionEditorDialog.showDialog(this, selected, 0); 
        if (updated != null) {
            controller.replace(selected.id(), updated);
            model.reload(controller.list());
            SwingUtilities.invokeLater(() -> table.calculateOptimalDimensions());
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            StyledAlertDialog.show(this, "Missing Selection", "Select a row to delete.", false);
            return;
        }
        if (model.getRowCount() <= 20) {
            StyledAlertDialog.show(this, "Cannot Delete", "Minimum 20 questions required.", true);
            return;
        }
        Question selected = model.getAt(row);
        int ok = StyledConfirmDialog.show(this, "Delete question " + selected.id() + "?", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            controller.delete(selected.id());
            model.reload(controller.list());
            SwingUtilities.invokeLater(() -> table.calculateOptimalDimensions());
        }
    }

    // --- INNER CLASSES ---

    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        public BackgroundPanel(Image bg) { this.bg = bg; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                double scale = Math.max((double) getWidth() / bg.getWidth(null), (double) getHeight() / bg.getHeight(null));
                int dW = (int) (bg.getWidth(null) * scale);
                int dH = (int) (bg.getHeight(null) * scale);
                g.drawImage(bg, (getWidth() - dW) / 2, (getHeight() - dH) / 2, dW, dH, this);
            }
        }
    }

    private static class QuestionTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Text", "Level", "A (Opt1)", "B (Opt2)", "C (Opt3)", "D (Opt4)", "Correct"};
        private List<Question> data = new ArrayList<>();

        QuestionTableModel(List<Question> initial) { reload(initial); }
        public void reload(List<Question> qs) {
            data = new ArrayList<>(qs);
            data.sort((a, b) -> Integer.compare(Integer.parseInt(a.id()), Integer.parseInt(b.id())));
            fireTableDataChanged();
        }
        public Question getAt(int row) { return data.get(row); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int row, int col) {
            Question q = data.get(row);
            return switch(col) {
                case 0 -> q.id();
                case 1 -> q.text();
                case 2 -> q.level().name();
                case 3 -> q.options().get(0);
                case 4 -> q.options().get(1);
                case 5 -> q.options().get(2);
                case 6 -> q.options().get(3);
                case 7 -> switch(q.correctIndex()) {
                    case 0 -> "A"; case 1 -> "B"; case 2 -> "C"; case 3 -> "D";
                    default -> "?";
                };
                default -> "";
            };
        }
    }
}