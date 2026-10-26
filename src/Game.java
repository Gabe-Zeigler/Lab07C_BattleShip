import java.util.Random;

/** Game rules and counters. Wraps a Board and decides win / loss. */
public class Game {
    public static final int MISSES_PER_STRIKE = 5;
    public static final int MAX_STRIKES = 3;

    public enum Status { PLAYING, WON, LOST }

    private final Board board;
    private int missCount;
    private int strikeCount;
    private int totalMiss;
    private int totalHit;
    private Status status;

    public Game() {
        this(new Board());
    }

    public Game(Random rng) {
        this(new Board(rng));
    }

    public Game(Board board) {
        this.board = board;
        this.status = Status.PLAYING;
    }

    /** Starts a fresh game: new ship layout, all counters zero. */
    public void newGame() {
        board.reset();
        missCount = 0;
        strikeCount = 0;
        totalMiss = 0;
        totalHit = 0;
        status = Status.PLAYING;
    }

    /** Fires at a cell and applies the scoring rules. */
    public ShotResult fire(int row, int col) {
        if (status != Status.PLAYING) {
            return ShotResult.ALREADY_FIRED;
        }
        ShotResult result = board.fire(row, col);
        switch (result) {
            case HIT:
            case SUNK:
                totalHit++;
                missCount = 0;
                if (board.allShipsSunk()) {
                    status = Status.WON;
                }
                break;
            case MISS:
                missCount++;
                totalMiss++;
                if (missCount >= MISSES_PER_STRIKE) {
                    strikeCount++;
                    missCount = 0;
                    if (strikeCount >= MAX_STRIKES) {
                        status = Status.LOST;
                    }
                }
                break;
            default:
                break;   // ALREADY_FIRED: not a move
        }
        return result;
    }

    public Board getBoard()      { return board; }
    public int getMissCount()    { return missCount; }
    public int getStrikeCount()  { return strikeCount; }
    public int getTotalMiss()    { return totalMiss; }
    public int getTotalHit()     { return totalHit; }
    public Status getStatus()    { return status; }
    public boolean isOver()      { return status != Status.PLAYING; }
}
