package points;

public class ScreenPoint {
    private final int r, c;

    public ScreenPoint(int c, int r) {
        this.r = r;
        this.c = c;
    }
    public ScreenPoint() {
        this.r = 0;
        this.c = 0;
    }

    public int getR() {
        return r;
    }

    public int getC() {
        return c;
    }
}
