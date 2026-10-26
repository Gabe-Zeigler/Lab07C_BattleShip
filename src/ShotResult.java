/** The outcome of firing a missile at a cell. */
public enum ShotResult {
    MISS,           // nothing but water
    HIT,            // part of a ship was hit
    SUNK,           // the hit finished off a ship
    ALREADY_FIRED   // cell was fired at before; does not count as a move
}
