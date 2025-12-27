package view;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.JViewport;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Custom JTable implementation that calculates optimal height and width 
 * for word wrap content ONCE after data load, ensuring stability.
 * This version fixes the horizontal clinging/distortion issue and enables 
 * horizontal scrolling when needed.
 */
public class QuestionTable extends JTable {

    // Column indices for long text (1: Text, 3: Opt1, 4: Opt2, 5: Opt3, 6: Opt4)
    private static final int[] WRAP_COLUMNS = {1, 3, 4, 5, 6};
    // Assuming WordWrapCellRenderer exists in the same package
    private final WordWrapCellRenderer renderer = new WordWrapCellRenderer(); 
    private static final int MIN_COLUMN_WIDTH = 50; 
    
    public QuestionTable(TableModel dm) {
        super(dm);
        
        // Critical for horizontal scrolling: allows the table to be wider than the viewport.
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        initRenderers();
    }
    
    private void initRenderers() {
        TableColumnModel columnModel = getColumnModel();
        
        for (int colIndex : WRAP_COLUMNS) {
            if (colIndex < columnModel.getColumnCount()) {
                columnModel.getColumn(colIndex).setCellRenderer(renderer);
            }
        }
    }
    
    /**
     * Public method to calculate optimal column widths and row heights ONCE 
     * after data load or update. This fixes the clinging and scrolling issues.
     */
    public void calculateOptimalDimensions() {
        if (getRowCount() == 0 || getColumnCount() == 0) return;
        
        TableColumnModel columnModel = getColumnModel();
        int maxRows = getRowCount();
        int[] maxColumnWidths = new int[getColumnCount()];
        int totalCalculatedWidth = 0; // Total width required by the content

        // 1. Initial calculation loop (find max width for headers and content)
        for (int col = 0; col < getColumnCount(); col++) {
            
            // Start width from header size
            Component headerComp = getTableHeader().getDefaultRenderer().getTableCellRendererComponent(
                this, columnModel.getColumn(col).getHeaderValue(), false, false, -1, col
            );
            maxColumnWidths[col] = Math.max(MIN_COLUMN_WIDTH, headerComp.getPreferredSize().width);
            
            for (int row = 0; row < maxRows; row++) {
                Component comp = prepareRenderer(getCellRenderer(row, col), row, col);
                // Add padding of 10 pixels
                maxColumnWidths[col] = Math.max(maxColumnWidths[col], comp.getPreferredSize().width + 10);
            }
            // Accumulate the calculated width for this column
            totalCalculatedWidth += maxColumnWidths[col];
        }
        
        // 2. Set the stable calculated widths
        for (int col = 0; col < getColumnCount(); col++) {
            columnModel.getColumn(col).setPreferredWidth(maxColumnWidths[col]);
            columnModel.getColumn(col).setMinWidth(maxColumnWidths[col]); // Set min width for stability
        }

        // 3. --- FIX DISTORTION AND ENABLE SCROLLING ---
        
        int viewportWidth = 0;
        // Get the width of the visible area (the JViewport)
        if (getParent() instanceof JViewport) {
            viewportWidth = getParent().getWidth();
        }
        
        if (viewportWidth > totalCalculatedWidth && viewportWidth > 0) {
            // If the visible width is larger than the required width:
            // 1. Set AUTO_RESIZE_LAST_COLUMN to stretch the last column to fill the gap.
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            
            // 2. Set the PreferredSize to the viewport width to prevent clinging.
            setPreferredScrollableViewportSize(new Dimension(viewportWidth, getPreferredSize().height));
        } else {
            // If the required width is larger (or equal):
            // 1. Set AUTO_RESIZE_OFF to enable the horizontal scrollbar.
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            
            // 2. Set the PreferredSize to the total calculated width.
            setPreferredScrollableViewportSize(new Dimension(totalCalculatedWidth, getPreferredSize().height));
        }
        // ---------------------------------------------
        
        // 4. Calculate max row height based on the *new* stable column widths
        for (int row = 0; row < maxRows; row++) {
            int rowHeight = 30; // Min height
            
            for (int colIndex : WRAP_COLUMNS) {
                if (colIndex < columnModel.getColumnCount()) {
                    Component comp = prepareRenderer(getCellRenderer(row, colIndex), row, colIndex);
                    
                    // Set the component's width to the stable, calculated column width
                    // This is essential for the WordWrapCellRenderer to determine the correct height.
                    comp.setSize(columnModel.getColumn(colIndex).getWidth(), Integer.MAX_VALUE);
                    
                    rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
                }
            }
            // 5. Set stable row heights
            setRowHeight(row, rowHeight);
        }
    }
}