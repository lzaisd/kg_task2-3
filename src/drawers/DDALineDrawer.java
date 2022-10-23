package drawers;

import java.awt.*;

public class DDALineDrawer implements LineDrawer {
    private PixelDrawer pd;

    public DDALineDrawer(PixelDrawer pd) {
        this.pd = pd;
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, Color c) {
        int lx = Math.abs(x2 - x1);
        int ly = Math.abs(y2 - y1);

        int l = Math.max(lx, ly);
        if (l != 0) {
            double dx = (double) (x2 - x1) / l;
            double dy = (double) (y2 - y1) / l;
            double x = x1;
            double y = y1;

            for (l ++; l> 0; l--) {
                x += dx;
                y += dy;
                pd.drawPixel((int) x, (int) y, c);
            }
        }
    }
}
