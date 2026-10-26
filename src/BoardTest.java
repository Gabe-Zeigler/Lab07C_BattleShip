import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;

class BoardTest {
    @Test
    void placesExactlySeventeenShipCells() {
        for (int seed = 0; seed < 200; seed++) {
            Board b = new Board(new Random(seed));
            int count = 0;
            for (int r = 0; r < Board.SIZE; r++)
                for (int c = 0; c < Board.SIZE; c++)
                    if (b.hasShipAt(r, c)) count++;
            assertEquals(17, count, "seed " + seed);   // 5+4+3+3+2, so no overlaps
        }
    }

    @Test
    void eachShipOccupiesItsSizeInAStraightLine() {
        for (int seed = 0; seed < 100; seed++) {
            Board b = new Board(new Random(seed));
            for (Ship s : b.getShips()) {
                int minR = 99, maxR = -1, minC = 99, maxC = -1, n = 0;
                for (int r = 0; r < Board.SIZE; r++)
                    for (int c = 0; c < Board.SIZE; c++)
                        if (b.getShipAt(r, c) == s) {
                            n++;
                            minR = Math.min(minR, r); maxR = Math.max(maxR, r);
                            minC = Math.min(minC, c); maxC = Math.max(maxC, c);
                        }
                assertEquals(s.getSize(), n);
                boolean horizontal = (minR == maxR) && (maxC - minC + 1 == n);
                boolean vertical = (minC == maxC) && (maxR - minR + 1 == n);
                assertTrue(horizontal || vertical);
            }
        }
    }

    @Test
    void allCellsStartBlank() {
        Board b = new Board(new Random(1));
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                assertEquals(CellState.BLANK, b.getCellState(r, c));
    }

    @Test
    void fireMissAndHitUpdateCells() {
        Board b = new Board(new Random(5));
        boolean sawHit = false, sawMiss = false;
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++) {
                boolean ship = b.hasShipAt(r, c);
                ShotResult res = b.fire(r, c);
                if (ship) {
                    assertEquals(CellState.HIT, b.getCellState(r, c));
                    assertTrue(res == ShotResult.HIT || res == ShotResult.SUNK);
                    sawHit = true;
                } else {
                    assertEquals(ShotResult.MISS, res);
                    assertEquals(CellState.MISS, b.getCellState(r, c));
                    sawMiss = true;
                }
            }
        assertTrue(sawHit && sawMiss);
        assertTrue(b.allShipsSunk());
    }

    @Test
    void firingSameCellTwiceIsAlreadyFired() {
        Board b = new Board(new Random(2));
        b.fire(0, 0);
        assertEquals(ShotResult.ALREADY_FIRED, b.fire(0, 0));
    }

    @Test
    void outOfRangeThrows() {
        Board b = new Board(new Random(2));
        assertThrows(IndexOutOfBoundsException.class, () -> b.fire(10, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> b.fire(0, -1));
    }
}
