package view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Custom renderer for JTable cells that wraps long text and preserves cell borders.
 * It is now passive and does NOT change row height dynamically (QuestionTable does that).
 */
public class WordWrapCellRenderer extends JTextArea implements TableCellRenderer {

    // גבול דק כהה לשימור מראה הטבלה
    private static final Border CELL_BORDER = new LineBorder(new Color(40, 60, 65), 1); 

    public WordWrapCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        setFont(new Font("Segoe UI", Font.PLAIN, 16));
        setForeground(Color.WHITE);
        setBorder(CELL_BORDER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
                                                   boolean isSelected, boolean hasFocus, 
                                                   int row, int column) {
        setText(value != null ? value.toString() : "");
        
        // --- Coloring ---
        if (isSelected) {
            setBackground(new Color(80, 120, 255, 200)); // Selection blue
            setForeground(Color.WHITE);
        } 
        else 
        {
        // Row striping logic to match HistoryView
        if (row % 2 == 0) {
          setBackground(new Color(25, 28, 60)); // Even row
        } else {
            setBackground(new Color(20, 22, 50)); // Odd row
        }
        setForeground(Color.WHITE);
        }
        // Set border to match the navy theme
        setBorder(new LineBorder(new Color(255, 255, 255, 40), 1));
        
        // IMPORTANT: We do NOT call setRowHeight here. QuestionTable handles that globally.
        return this;
    }
}