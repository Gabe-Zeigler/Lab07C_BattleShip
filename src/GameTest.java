import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;

class GameTest {
    /** Fires at the first open-water cell not yet fired at. */
    private static ShotResult fireMiss(Game g) {
        Board b = g.getBoard();
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                if (!b.hasShipAt(r, c) && b.getCellState(r, c) == CellState.BLANK)
                    return g.fire(r, c);
        throw new IllegalStateException("no water left");
    }

    private static ShotResult fireHit(Game g) {
        Board b = g.getBoard();
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                if (b.hasShipAt(r, c) && b.getCellState(r, c) == CellState.BLANK)
                    return g.fire(r, c);
        throw new IllegalStateException("no ship cells left");
    }

    @Test
    void countersStartAtZero() {
        Game g = new Game(new Random(1));
        assertEquals(0, g.getMissCount());
        assertEquals(0, g.getStrikeCount());
        assertEquals(0, g.getTotalMiss());
        assertEquals(0, g.getTotalHit());
        assertEquals(Game.Status.PLAYING, g.getStatus());
    }

    @Test
    void missIncrementsMissAndTotalMiss() {
        Game g = new Game(new Random(1));
        fireMiss(g);
        assertEquals(1, g.getMissCount());
        assertEquals(1, g.getTotalMiss());
    }

    @Test
    void hitResetsMissCounterAndIncrementsTotalHit() {
        Game g = new Game(new Random(1));
        fireMiss(g); fireMiss(g);
        fireHit(g);
        assertEquals(0, g.getMissCount());
        assertEquals(2, g.getTotalMiss());
        assertEquals(1, g.getTotalHit());
    }

    @Test
    void fiveMissesInARowGivesStrikeAndResetsMissCounter() {
        Game g = new Game(new Random(1));
        for (int i = 0; i < 5; i++) fireMiss(g);
        assertEquals(1, g.getStrikeCount());
        assertEquals(0, g.getMissCount());
        assertEquals(5, g.getTotalMiss());
    }

    @Test
    void threeStrikesLosesTheGame() {
        Game g = new Game(new Random(1));
        for (int i = 0; i < 15; i++) fireMiss(g);
        assertEquals(Game.Status.LOST, g.getStatus());
        assertTrue(g.isOver());
    }

    @Test
    void hitBetweenMissesPreventsStrike() {
        Game g = new Game(new Random(1));
        for (int round = 0; round < 3; round++) {
            for (int i = 0; i < 4; i++) fireMiss(g);
            fireHit(g);
        }
        assertEquals(0, g.getStrikeCount());
    }

    @Test
    void sinkingEveryShipWins() {
        Game g = new Game(new Random(3));
        ShotResult last = null;
        for (int i = 0; i < 17; i++) last = fireHit(g);
        assertEquals(ShotResult.SUNK, last);
        assertEquals(17, g.getTotalHit());
        assertEquals(Game.Status.WON, g.getStatus());
    }

    @Test
    void repeatClickDoesNotCountAsMove() {
        Game g = new Game(new Random(1));
        fireMiss(g);
        int[] cell = firstFiredCell(g);
        assertEquals(ShotResult.ALREADY_FIRED, g.fire(cell[0], cell[1]));
        assertEquals(1, g.getTotalMiss());
        assertEquals(1, g.getMissCount());
    }

    private static int[] firstFiredCell(Game g) {
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                if (g.getBoard().getCellState(r, c) != CellState.BLANK)
                    return new int[] {r, c};
        return null;
    }

    @Test
    void newGameResetsEverything() {
        Game g = new Game(new Random(1));
        for (int i = 0; i < 15; i++) fireMiss(g);
        g.newGame();
        assertEquals(Game.Status.PLAYING, g.getStatus());
        assertEquals(0, g.getTotalMiss());
        assertEquals(0, g.getStrikeCount());
        assertEquals(CellState.BLANK, g.getBoard().getCellState(0, 0));
    }

    @Test
    void noMovesAcceptedAfterGameOver() {
        Game g = new Game(new Random(1));
        for (int i = 0; i < 15; i++) fireMiss(g);
        int before = g.getTotalMiss();
        fireMiss(g);
        assertEquals(before, g.getTotalMiss());
    }
}
