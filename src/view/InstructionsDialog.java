package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/*
 * InstructionsDialog provides a graphical overlay displaying the game rules.
 * It outlines the core mechanics of the 2-player Minesweeper mode.
 * The dialog uses a borderless, dark-themed UI with dynamic accent coloring 
 * to maintain consistency with the application's visual style.
*/
public class InstructionsDialog extends JDialog {

    public InstructionsDialog(JFrame owner) {
        super(owner, "Game Instructions", true);
        setUndecorated(true); 
        
        // Set width to 850 to allow text to be fully seen without need of scroll bar
        setSize(850, 750); 
        setLocationRelativeTo(owner);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(15, 18, 40));
        contentPanel.setBorder(BorderFactory.createLineBorder(UIStyles.ACCENT, 3));

        JLabel title = new JLabel(":: How to Play ::", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(UIStyles.ACCENT);
        title.setBorder(new EmptyBorder(25, 0, 15, 0));
        contentPanel.add(title, BorderLayout.NORTH);

        // Dynamic Color Conversion: Convert UIStyles.ACCENT to a hex string for HTML - in order for headlines to be pretty too
        String accentHex = String.format("#%02x%02x%02x", 
                UIStyles.ACCENT.getRed(), UIStyles.ACCENT.getGreen(), UIStyles.ACCENT.getBlue());

        // Formats the instructions using HTML to allow rich text styling and layout within the JLabel.
        String instructionsText = "<html><body style='width: 750px; font-family: Segoe UI; color: white; font-size: 14px;'>" +
                "<h2 style='color: " + accentHex + ";'>📝 General Description</h2>" +
                "Minesweeper is a classic logic game. Reveal squares while avoiding hidden mines to win!<br><br>" +
                
                "<h2 style='color: " + accentHex + ";'>👥 Multiplayer Rules</h2>" +
                "<ul>" +
                "<li>The game is played by <b>two players</b> with separate boards.</li>" +
                "<li>Turns alternate after every move.</li>" +
                "<li>Players share total <b>Lives</b> ❤️ and <b>Score</b> 💰.</li>" +
                "</ul>" +
                
                "<h2 style='color: " + accentHex + ";'>🔳 Cell Types</h2>" +
                "<ul>" +
                "<li><b>Mines 💣:</b> Stepping on a mine loses 1 life (-1 ❤️).</li>" +
                "<li><b>Numbers 🔢:</b> Indicate how many mines are nearby (1-8).</li>" +
                "<li><b>Surprise Cells 🎁:</b> 50/50 chance for a good or bad effect.</li>" +
                "<li><b>Question Cells ❓:</b> Answer a trivia question to gain bonuses!</li>" +
                "</ul>" +
                
                "<h2 style='color: " + accentHex + ";'>🏆 Winning</h2>" +
                "The game ends when all mines are found on a board OR when hearts reach zero.<br>" +
                "Remaining hearts are converted into bonus points at the end!" +
                "</body></html>";

        JLabel textLabel = new JLabel(instructionsText);
        textLabel.setVerticalAlignment(SwingConstants.TOP);
        
        // ScrollPane setup - scrollbar is hidden if not needed to see the text in the instructions
        JScrollPane scrollPane = new JScrollPane(textLabel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(new EmptyBorder(0, 40, 0, 40));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        BaseGameFrame.RoundedButton closeBtn = new BaseGameFrame.RoundedButton("Got it!", 220, 70, 35);
        closeBtn.addActionListener(e -> dispose());
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(20, 0, 30, 0));
        btnPanel.add(closeBtn);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
    }
}