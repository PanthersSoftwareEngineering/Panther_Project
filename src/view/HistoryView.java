package view;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

import model.SysData;
import model.SysData.GameRecord;
import controller.AppController;

/**
 * Window that displays the list of past game records as a table.
 * Includes a "Back to Main" button to return to the main menu.
 */
public class HistoryView extends JFrame {

    /** Reference to the shared SysData. */
    private final SysData sys;

    /** Table used to show the game records. */
    private final JTable table;

    /** Table model backing the history table. */
    private final HistoryTableModel model;

    /**
     * Creates the history view and configures its layout.
     */
    public HistoryView(SysData sys) {
        super("Game History");
        this.sys = sys;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
        setMinimumSize(new Dimension(800, 420));

        // Top bar: title + back button
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8,8,0,8));

        JLabel title = new JLabel("Game History", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JButton back = new JButton("Back to Main");
        back.addActionListener(e -> {
            dispose();
            AppController.getInstance().showMainMenu();
        });

        top.add(title, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table area with scroll pane.
        model = new HistoryTableModel(sys.history());
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    /** Shows the window. */
    public void showSelf(){ setVisible(true); }

    /**
     * Table model that adapts GameRecord objects to table rows and columns.
     */
    private static class HistoryTableModel extends AbstractTableModel {
        private final String[] cols = {
            "Player 1", "Player 2", "Difficulty",
            "Hearts", "Points", "Won?",
            "Time (sec)", "Date Saved"
        };

        /** Snapshot of the history data. */
        private final List<GameRecord> data;

        HistoryTableModel(List<GameRecord> d){
            this.data = d;
        }

        @Override
        public int getRowCount(){ return data.size(); }

        @Override
        public int getColumnCount(){ return cols.length; }

        @Override
        public String getColumnName(int c){ return cols[c]; }

        @Override
        public Object getValueAt(int row, int col){
            GameRecord r = data.get(row);
            return switch(col){
                case 0 -> r.p1;
                case 1 -> r.p2;
                case 2 -> r.level.name();
                case 3 -> r.hearts;
                case 4 -> r.points;
                case 5 -> r.won ? "✅" : "❌";
                case 6 -> r.timeSec;
                case 7 -> new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                             .format(new java.util.Date(r.timestamp));
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int r,int c){ return false; }
    }
}
