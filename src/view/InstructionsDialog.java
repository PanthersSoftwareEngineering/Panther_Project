package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InstructionsDialog extends JDialog {

    public InstructionsDialog(JFrame owner) {
        super(owner, "Game Instructions", true);
        setUndecorated(true); 
        setSize(650, 750); // Increased size slightly for better font spacing
        setLocationRelativeTo(owner);

        // Main panel with dark theme and golden border
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(15, 18, 40));
        contentPanel.setBorder(BorderFactory.createLineBorder(UIStyles.ACCENT, 3));

        // Headline with Emoji
        JLabel title = new JLabel(":: How to Play ::", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(UIStyles.ACCENT);
        title.setBorder(new EmptyBorder(25, 0, 15, 0));
        contentPanel.add(title, BorderLayout.NORTH);

        // Instruction text with enhanced styling and Emojis
        // Based on system requirements: Multiplayer, Cell types, and Finish conditions
        String instructionsText = "<html><body style='width: 500px; font-family: Segoe UI; color: white; font-size: 13px;'>" +
                "<h2 style='color: #FFBE3C;'>📝 General Description</h2>" +
                "Minesweeper is a classic logic game. Reveal squares while avoiding hidden mines to win!<br><br>" +
                
                "<h2 style='color: #FFBE3C;'>👥 Multiplayer Rules</h2>" +
                "<ul>" +
                "<li>The game is played by <b>two players</b> with separate boards.</li>" +
                "<li>Turns alternate after every move.</li>" +
                "<li>Players share total <b>Lives</b> ❤️ and <b>Score</b> 💰.</li>" +
                "</ul>" +
                
                "<h2 style='color: #FFBE3C;'>🔳 Cell Types</h2>" +
                "<ul>" +
                "<li><b>Mines 💣:</b> Stepping on a mine loses 1 life (-1 ❤️).</li>" +
                "<li><b>Numbers 🔢:</b> Indicate how many mines are nearby (1-8).</li>" +
                "<li><b>Surprise Cells 🎁:</b> 50/50 chance for a good or bad effect.</li>" +
                "<li><b>Question Cells ❓:</b> Answer a trivia question to gain bonuses!</li>" +
                "</ul>" +
                
                "<h2 style='color: #FFBE3C;'>🏆 Winning</h2>" +
                "The game ends when all mines are found on a board OR when hearts reach zero.<br>" +
                "Remaining hearts are converted into bonus points at the end! " +
                "</body></html>";

        JLabel textLabel = new JLabel(instructionsText);
        textLabel.setVerticalAlignment(SwingConstants.TOP);
        
        JScrollPane scrollPane = new JScrollPane(textLabel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(new EmptyBorder(0, 30, 0, 30));
        
        // Customizing scrollbar visibility
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Closing screen button
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