/** A single ship: tracks its size and how many hits it has taken. */
public class Ship {
    private final String name;
    private final int size;
    private int hits;

    public Ship(String name, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Ship size must be at least 1");
        }
        this.name = name;
        this.size = size;
        this.hits = 0;
    }

    /** Records one hit on this ship. */
    public void hit() {
        if (hits < size) {
            hits++;
        }
    }

    public boolean isSunk() {
        return hits >= size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getHits() {
        return hits;
    }
}
