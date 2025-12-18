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
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground()); 
            setForeground(table.getForeground());
        }
        
        // IMPORTANT: We do NOT call setRowHeight here. QuestionTable handles that globally.
        
        return this;
    }
}