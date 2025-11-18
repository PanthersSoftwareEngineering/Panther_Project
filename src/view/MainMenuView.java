package view;
import javax.swing.*; import java.awt.*;
import controller.AppController;

public class MainMenuView extends JFrame {

    private final AppController app;

    // [Iter1 - Maor] – בניית מסך פתיחה עם ארבעה כפתורים
    public MainMenuView(AppController app){
        super("Minesweeper - Main");
        this.app=app;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p=new JPanel(new GridLayout(4,1,12,12));
        JButton start=new JButton("Start Game");
        JButton history=new JButton("Game History");
        JButton qman=new JButton("Question Management");
        JButton exit=new JButton("Exit");

        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.add(start);
        p.add(history);
        p.add(qman);
        p.add(exit);
        setContentPane(p);




        pack();
    }

    // [Iter1 - Maor] – הצגת התפריט הראשי במרכז המסך
    public void showSelf(){ setLocationRelativeTo(null); setVisible(true); }
}
