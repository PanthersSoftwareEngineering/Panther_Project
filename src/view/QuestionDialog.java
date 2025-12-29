// =======================
// view/QuestionDialog.java
// =======================
package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDialog extends JDialog {

    private int answer = -1;          // will become 0..n-1 (no cancel)
    private boolean locked = false;

    private final List<JButton> optionButtons = new ArrayList<>();
    private final int correctIndex;

    public QuestionDialog(Window owner, QuestionDTO q, int correctIndex) {
        super(owner, "Question", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        this.correctIndex = correctIndex;

        // ✅ no closing by X / ESC
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

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
        JLabel text = new JLabel("<html><div style='width:480px'>" + q.text() + "</div></html>");
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
            btn.setOpaque(true);

            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 10));

            btn.addActionListener(e -> onPick(idx));

            optionButtons.add(btn);
            answers.add(btn);
        }

        // ================= Layout =================
        root.add(title, BorderLayout.NORTH);
        root.add(text, BorderLayout.CENTER);
        root.add(answers, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onPick(int idx) {
        if (locked) return;
        locked = true;

        answer = idx;

        // disable all
        for (JButton b : optionButtons) b.setEnabled(false);

        // reveal colors: correct green, others red
        Color green = new Color(40, 167, 69);
        Color red   = new Color(220, 53, 69);

        for (int i = 0; i < optionButtons.size(); i++) {
            JButton b = optionButtons.get(i);
            if (i == correctIndex) {
                b.setBackground(green);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(red);
                b.setForeground(Color.WHITE);
            }
        }

        // ✅ 3 seconds then close
        Timer t = new Timer(3000, ev -> {
            ((Timer) ev.getSource()).stop();
            dispose();
        });
        t.setRepeats(false);
        t.start();
    }

    public int showDialog() {
        setVisible(true); // modal
        return answer;    // always >=0 now
    }
}
