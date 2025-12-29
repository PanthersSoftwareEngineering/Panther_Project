package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class QuestionDialog extends JDialog {

    private int answer = -1; // ברירת מחדל = Cancel

    public QuestionDialog(JFrame owner, QuestionDTO q) {
        super(owner, "Question", true);
        setUndecorated(true);

        // ================= Root =================
        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                g2.setStroke(new BasicStroke(3f));
                g2.setColor(UIStyles.GOLD_TEXT);
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 24, 24);

                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(UIStyles.pad(20, 24, 20, 24));
        root.setLayout(new BorderLayout(14, 14));

        // ================= Title =================
        JLabel title = new JLabel("Difficulty: " + q.levelLabel());
        title.setForeground(UIStyles.GOLD_TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // ================= Question text =================
        JLabel text = new JLabel(
                "<html><div style='width:480px'>" + q.text() + "</div></html>"
        );
        text.setForeground(UIStyles.GOLD_TEXT);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        // ================= Answers =================
        JPanel answers = new JPanel(new GridLayout(q.options().size(), 1, 10, 10));
        answers.setOpaque(false);

        for (int i = 0; i < q.options().size(); i++) {
            int idx = i;
            char letter = (char) ('A' + i);

            JButton btn = new JButton(letter + ") " + q.options().get(i));
            btn.setFocusPainted(false);
            btn.setFont(UIStyles.HUD_FONT);
            btn.setForeground(UIStyles.GOLD_TEXT);
            btn.setBackground(new Color(60, 60, 60));

            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setHorizontalTextPosition(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 10));

            btn.addActionListener(e -> {
                answer = idx;
                dispose();
            });

            answers.add(btn);
        }

        // ================= Cancel button =================
        BaseGameFrame.RoundedButton cancelBtn =
                new BaseGameFrame.RoundedButton("Cancel", 200, 56, 20);

        cancelBtn.addActionListener(e -> {
            answer = -1;
            dispose();
        });

        JPanel cancelWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cancelWrap.setOpaque(false);
        cancelWrap.add(cancelBtn);

        // ================= Combine bottom =================
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(answers);
        bottom.add(Box.createVerticalStrut(14));
        bottom.add(cancelWrap);

        // ================= Layout =================
        root.add(title, BorderLayout.NORTH);
        root.add(text, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);

        // ❌ סגירה עם X = Cancel
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                answer = -1;
            }
        });
    }

    public int showDialog() {
        setVisible(true); // modal
        return answer;
    }
}
