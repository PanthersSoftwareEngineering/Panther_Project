package junit_tests;  // NOT "junit"

import controller.MatchController;
import controller.AppController;
import model.*;
import org.junit.Test;              // JUNIT 4
import static org.junit.Assert.*;   // JUNIT 4

public class Cell_Tests {

    @Test
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
}