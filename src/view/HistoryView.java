package view;

import controller.AppController;
import model.SysData;
import model.SysData.GameRecord;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Displays the game history table.
 * Uses the same visual style as other screens (background image, rounded card,styled JTable, and a "Back to Main" button)
 */
public class HistoryView extends BaseGameFrame {

    /* Reference to the shared system data */
    private final SysData sys;

    /* JTable that displays the history records */
    private final JTable table;

    /* Table model backing the history table */
    private final HistoryTableModel model;

    /**
     * Creates the Game History screen
     */
    public HistoryView(SysData sys) {
        super(AppController.getInstance(), "Game History");
        this.sys = sys;

        // ===== Background =====
        // Uses a background image specific to the History screen
        Image bgImage = GameAssets.HISTORY_BACKGROUND;
        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new GridBagLayout());
        setContentPane(bgPanel);

        // ===== Main vertical container =====
        // Holds title, spacing, and the history card
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Top spacing (keeps content away from top edge)
        mainPanel.add(Box.createVerticalStrut(70));

        // Title label (text is transparent because the title is embedded in the background image)
        JLabel title = new JLabel("Game History");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(0,0,0,0)); // invisible text
        title.setFont(new Font("Segoe UI", Font.BOLD, 60));
        mainPanel.add(title);

        // Extra spacing between title area and table card
        mainPanel.add(Box.createVerticalStrut(60));

        // ===== Card panel =====
        // Semi-transparent rounded panel that contains the table and button
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 180)); // semi-transparent black
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(1150, 600)); // fixed card size

        // ===== History table =====
        // Model pulls data from SysData.history()
        model = new HistoryTableModel(sys.history());
        table = new JTable(model);
        styleTable(table);

        // Scroll pane wrapping the table
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(1100, 470));

        card.add(scroll, BorderLayout.CENTER);

        // ===== Bottom bar with Back button =====
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomBar.setOpaque(false);

        RoundedButton backBtn = new RoundedButton("Back to Main", 400, 80, 40);
        bottomBar.add(backBtn);

        card.add(bottomBar, BorderLayout.SOUTH);

        // Add card to main panel
        mainPanel.add(card);
        mainPanel.add(Box.createVerticalGlue());

        // Center main panel on background
        bgPanel.add(mainPanel, new GridBagConstraints());

        // Back button action
        backBtn.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });
    }

    /**
     * Applies visual styling to the history JTable.
     * Includes colors, fonts, row height, header styling, and row striping.
     */
    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        table.setForeground(Color.WHITE);
        table.setBackground(new Color(15, 18, 40));
        table.setGridColor(new Color(255, 255, 255, 40));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(30, 32, 70));
        ((JComponent) header).setOpaque(true);

        // Custom cell renderer for alternating row colors and selection highlight
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(CENTER);
                setOpaque(true);

                if (isSelected) {
                    c.setBackground(new Color(80, 120, 255, 200));
                } else {
                    c.setBackground(row % 2 == 0
                            ? new Color(25, 28, 60)
                            : new Color(20, 22, 50));
                }
                c.setForeground(Color.WHITE);

                return c;
            }
        };

        table.setDefaultRenderer(Object.class, cellRenderer);
    }

    /**
     * Table model that adapts GameRecord objects into table rows and columns
     */
    private static class HistoryTableModel extends AbstractTableModel {
        private final String[] cols = {
                "Player 1", "Player 2", "Difficulty",
                "Hearts", "Points", "Won?",
                "Time (sec)", "Date Saved"
        };

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
                //design prettier the won/lost in the table (to be green/red)
                case 5 -> r.won 
                ? "<html><div style='white-space:nowrap;'><b style='color:#00FF00;'>Won</b></div></html>" 
                : "<html><span style='color:#FF4444;'>Lost</span></html>";
                case 6 -> r.timeSec;
                case 7 -> new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                        .format(new java.util.Date(r.timestamp));
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int r,int c){ return false; }
    }

    /**
     * Background panel that either draws a scaled background image
     * or a gradient fallback if the image is missing
     */
    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        public BackgroundPanel(Image bg) { this.bg = bg; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg == null) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(12, 12, 20),
                        0, getHeight(), new Color(40, 40, 70)
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());
                return;
            }

            int imgW = bg.getWidth(null);
            int imgH = bg.getHeight(null);
            int panelW = getWidth();
            int panelH = getHeight();
            if (imgW <= 0 || imgH <= 0) return;

            // Scale image to fully cover the panel (wallpaper-style)
            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            g.drawImage(bg, x, y, drawW, drawH, this);
        }
    }
}
