package junit_tests;  // NOT "junit"

import controller.MatchController;
import controller.AppController;
import model.*;
import org.junit.Test;              // JUNIT 4
import static org.junit.Assert.*;   // JUNIT 4

public class Cell_Tests {

    @Test
    // checks the number of correct number of points for flag adding
    public void testCorrectFlagAddsPoint() {

        Match m = new Match(new Player("A"), new Player("B"), DifficultyLevel.EASY);
        Board board = m.board1();
        board.setCellForTest(0, 0, new MineCell());

        MatchController mc = MatchController.getInstance();
        mc.init(m, SysData.getInstance(), AppController.getInstance());

        int before = m.points();
        mc.toggleFlag(0, 0, 0);

        assertEquals(before + 1, m.points());
    }
    
    @Test
    //checks the number of lives removed after the mine was opened
    public void testRevealMineLosesOneLife() {

        Match m = new Match(new Player("A"), new Player("B"), DifficultyLevel.EASY);
        Board board = m.board1();
        board.setCellForTest(1, 1, new MineCell());

        MatchController mc = MatchController.getInstance();
        mc.init(m, SysData.getInstance(), AppController.getInstance());

        int before = m.lives();
        mc.reveal(1, 1);

        assertEquals(before - 1, m.lives());
    }

    @Test
    //Check that empty cell reveal increases points correctly
    public void testRevealEmptyIncreasesPoints() {

        Match m = new Match(new Player("A"), new Player("B"), DifficultyLevel.EASY);
        Board board = m.board1();
        board.setCellForTest(0, 0, new EmptyCell());

        MatchController mc = MatchController.getInstance();
        mc.init(m, SysData.getInstance(), AppController.getInstance());

        int before = m.points();

        mc.reveal(0, 0);   // will call floodReveal -> reveal several cells

        assertTrue(m.points() > before);   // points must increase (>= +1)
     }

    @Test
    //Check that marking flag on a wrong sell substracts exactly 3 points
    public void testFlagWrongCellSubtractsThreePoints() {

        Match m = new Match(new Player("A"), new Player("B"), DifficultyLevel.EASY);
        Board board = m.board1();

        // put a normal empty cell (not a mine)
        board.setCellForTest(1, 1, new EmptyCell());

        MatchController mc = MatchController.getInstance();
        mc.init(m, SysData.getInstance(), AppController.getInstance());

        int before = m.points();

        mc.toggleFlag(0, 1, 1);   // flag it

        assertEquals(before - 3, m.points());
    }
}