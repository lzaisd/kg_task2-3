package drawers;

import java.awt.*;

public class BresenhamLineDrawer implements LineDrawer {
    private PixelDrawer pd;

    public BresenhamLineDrawer(PixelDrawer pd) {
        this.pd = pd;
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, Color c) {
        if (x2 < x1) {
            swapCoord(x1, x2);
            swapCoord(y1, y2);
        }
        int lengthX = Math.abs(x2 - x1);
        int lengthY = Math.abs(y2 - y1);
        int l = Math.max(lengthX, lengthY);
        if (l > 0) {
            if (lengthY < lengthX) {
                int x = x1;
                double y = y1;
                for (l++; l > 0; l--) {
                    pd.drawPixel(x, (int) y, c);
                    y += (double) lengthY / lengthX;
                    x++;
                }
            } else {
                double x = x1;
                int y = y1;
                for (l++; l > 0; l--) {
                    pd.drawPixel((int) x, y, c);
                    x += (double) lengthX / lengthY;
                    y++;
                }
            }
        }
    }

    private void swapCoord(int c1, int c2) {
        int t = c1;
        c1 = c2;
        c2 = t;
    }
}
