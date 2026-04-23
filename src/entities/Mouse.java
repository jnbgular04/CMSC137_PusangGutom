package entities;

/**
 * Dummy data model for target coordinates.
 * Holds X, Y coordinates and active state.
 */
public class Mouse {
    public int x;
    public int y;
    public boolean isActive;

    public Mouse(int x, int y) {
        this.x = x;
        this.y = y;
        this.isActive = true;
    }
}