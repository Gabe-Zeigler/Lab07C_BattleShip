import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ShipTest {
    @Test
    void newShipIsNotSunk() {
        Ship s = new Ship("Destroyer", 2);
        assertFalse(s.isSunk());
        assertEquals(0, s.getHits());
    }

    @Test
    void shipSinksAfterSizeHits() {
        Ship s = new Ship("Destroyer", 2);
        s.hit();
        assertFalse(s.isSunk());
        s.hit();
        assertTrue(s.isSunk());
    }

    @Test
    void extraHitsDoNotExceedSize() {
        Ship s = new Ship("Destroyer", 2);
        s.hit(); s.hit(); s.hit();
        assertEquals(2, s.getHits());
    }

    @Test
    void invalidSizeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Ship("Bad", 0));
    }
}
