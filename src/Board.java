import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The 10x10 grid. Owns the ships, places them randomly, and resolves shots. */
public class Board {
    public static final int SIZE = 10;
    public static final int[] SHIP_SIZES = {5, 4, 3, 3, 2};   // descending order
    public static final String[] SHIP_NAMES =
        {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};

    private final Ship[][] shipAt = new Ship[SIZE][SIZE];
    private final CellState[][] visible = new CellState[SIZE][SIZE];
    private final List<Ship> ships = new ArrayList<>();
    private final Random rng;

    public Board() {
        this(new Random());
    }

    /** Seeded constructor so tests can get repeatable layouts. */
    public Board(Random rng) {
        this.rng = rng;
        reset();
    }

    /** Clears the board and randomly places all five ships. */
    public void reset() {
        ships.clear();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                shipAt[r][c] = null;
                visible[r][c] = CellState.BLANK;
            }
        }
        placeShips();
    }

    private void placeShips() {
        for (int i = 0; i < SHIP_SIZES.length; i++) {
            Ship ship = new Ship(SHIP_NAMES[i], SHIP_SIZES[i]);
            placeOne(ship);
            ships.add(ship);
        }
    }

    /**
     * Picks a random orientation and row/column, scans that line for every run of
     * free cells long enough for the ship, and picks one of those runs at random.
     * If the chosen line has no room, try another line.
     */
    private void placeOne(Ship ship) {
        int len = ship.getSize();
        while (true) {
            boolean horizontal = rng.nextBoolean();
            int line = rng.nextInt(SIZE);

            List<Integer> starts = new ArrayList<>();
            for (int start = 0; start + len <= SIZE; start++) {
                boolean free = true;
                for (int k = 0; k < len && free; k++) {
                    int r = horizontal ? line : start + k;
                    int c = horizontal ? start + k : line;
                    if (shipAt[r][c] != null) {
                        free = false;
                    }
                }
                if (free) {
                    starts.add(start);
                }
            }

            if (!starts.isEmpty()) {
                int start = starts.get(rng.nextInt(starts.size()));
                for (int k = 0; k < len; k++) {
                    int r = horizontal ? line : start + k;
                    int c = horizontal ? start + k : line;
                    shipAt[r][c] = ship;
                }
                return;
            }
        }
    }

    /** Fires at (row, col) and updates the visible state of that cell. */
    public ShotResult fire(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IndexOutOfBoundsException("Cell out of range: " + row + "," + col);
        }
        if (visible[row][col] != CellState.BLANK) {
            return ShotResult.ALREADY_FIRED;
        }
        Ship ship = shipAt[row][col];
        if (ship == null) {
            visible[row][col] = CellState.MISS;
            return ShotResult.MISS;
        }
        visible[row][col] = CellState.HIT;
        ship.hit();
        return ship.isSunk() ? ShotResult.SUNK : ShotResult.HIT;
    }

    public CellState getCellState(int row, int col) {
        return visible[row][col];
    }

    /** The ship at a cell, or null if it is open water. */
    public Ship getShipAt(int row, int col) {
        return shipAt[row][col];
    }

    public boolean hasShipAt(int row, int col) {
        return shipAt[row][col] != null;
    }

    public boolean allShipsSunk() {
        for (Ship s : ships) {
            if (!s.isSunk()) {
                return false;
            }
        }
        return true;
    }

    public List<Ship> getShips() {
        return new ArrayList<>(ships);
    }

    public static int totalShipCells() {
        int total = 0;
        for (int s : SHIP_SIZES) {
            total += s;
        }
        return total;
    }
}
