import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private ScreenConverter sc;
    private Line ox, oy;
    private Line currentLine = null;
    private java.util.List<Line> allLines = new ArrayList<>();
    private Line editingLine = null;

    public DrawPanel() {
        sc = new ScreenConverter(-2, 2, 4, 4, 800, 600);
        ox = new Line(new RealPoint(-1, 0), new RealPoint(1, 0));
        oy = new Line(new RealPoint(0, -1), new RealPoint(0, 1));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
    }

    @Override
    public void paintComponent(Graphics origG) {
        sc.setSw(getWidth());
        sc.setSh(getHeight());

        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0 , getWidth(), getHeight());

        g.setColor(Color.BLUE);
        drawLine(g, sc, ox);
        drawLine(g, sc, oy);

        g.setColor(Color.BLACK);
        for (Line l: allLines) {
            drawLine(g, sc, l);
        }
        if (currentLine != null) {
            g.setColor(Color.red);
            drawLine(g, sc, currentLine);
        }

        if (editingLine != null) {
            g.setColor(Color.green);
            drawLine(g, sc, editingLine);
        }

        origG.drawImage(bi, 0 , 0, null);
        g.dispose();
    }

    private static void drawLine(Graphics2D g, ScreenConverter sc, Line l) {
        ScreenPoint p1 = sc.r2s(l.getP1());
        ScreenPoint p2 = sc.r2s(l.getP2());
        g.drawLine(p1.getC(), p1.getR(), p2.getC(), p2.getR());
    }

    private static boolean isNear(ScreenPoint p1, ScreenPoint p2, double eps) {
        int dx = p1.getC() - p2.getC();
        int dy = p1.getR() - p2.getR();
        int d2 = dx*dx - dy*dy;
        return d2<eps;
    }

    private static double distanceToLine (ScreenPoint lp1, ScreenPoint lp2, ScreenPoint cp) {
        double a = lp2.getR() - lp1.getR();
        double b = -(lp2.getC() - lp1.getC());
        //b*x-a*y + cp.getC()*b + cp.getR()*a = 0
        //a*x+b*y + a*lp1.getC() - b*lp1.getR() = 0
        double e = cp.getC()*b + cp.getR()*a;
        double f = a*lp1.getC() - b*lp1.getR();
        double y = (a*e - b*f) / (a*a + b*b);
        double x = (a * y - e)/b;
        return Math.sqrt((cp.getC()-x)* (cp.getC()-x) + (cp.getR() - y)*(cp.getR() - y));
    }

    private static boolean isPointInRect(ScreenPoint pr1, ScreenPoint pr2, ScreenPoint cp) {
        return cp.getC() >= Math.min(pr1.getC(), pr2.getC()) &&
               cp.getC() <= Math.max(pr1.getC(), pr2.getC()) &&
               cp.getR() >= Math.min(pr1.getR(), pr2.getR()) &&
               cp.getR() <= Math.max(pr1.getR(), pr2.getR());
    }

    private static boolean closeToLine(ScreenConverter sc, Line l, ScreenPoint p, int eps) {
        ScreenPoint a = sc.r2s(l.getP1());
        ScreenPoint b = sc.r2s(l.getP2());
        return isNear(a, p, eps) || isNear(b, p, eps) || (distanceToLine(a, b, p) < eps && isPointInRect(a, b, p));
    }

    private static Line findLine (ScreenConverter sc, java.util.List<Line> lines, ScreenPoint searchPoint,int eps) {
        for (Line l : lines) {
            if (closeToLine(sc, l, searchPoint, eps)) {
                return l;
            }
        }
        return null;
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private ScreenPoint prevPoint = null;
    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            prevPoint = new ScreenPoint(e.getX(), e.getY());
        } else if (SwingUtilities.isLeftMouseButton(e)){
            if (editingLine == null) {
                Line x = findLine(sc, allLines, new ScreenPoint(e.getX(), e.getY()), 3);
                if (x != null) {
                    editingLine = x;
                } else {
                    RealPoint p = sc.s2r(new ScreenPoint(e.getX(), e.getY()));
                    currentLine = new Line(p, p);
                }
            } else {
                if (closeToLine(sc, editingLine, new ScreenPoint(e.getX(), e.getY()), 3)) {

                } else {
                    editingLine = null;
                }
            }
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            prevPoint = null;
        } else if (SwingUtilities.isLeftMouseButton(e)){
            if (currentLine != null) {
                currentLine.setP2(sc.s2r(new ScreenPoint(e.getX(), e.getY())));
                allLines.add(currentLine);
                currentLine = null;
            }
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            if (editingLine != null) {
                if (closeToLine(sc, editingLine, new ScreenPoint(e.getX(), e.getY()), 4)) {
                    allLines.remove(editingLine);
                    editingLine = null;
                }
            }
        }
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            ScreenPoint curPoint = new ScreenPoint(e.getX(), e.getY());
            RealPoint p1 = sc.s2r(curPoint);
            RealPoint p2 = sc.s2r(prevPoint);
            RealPoint delta = p2.minus(p1);
            sc.moveCorner(delta);
            prevPoint = curPoint;
        } else if (SwingUtilities.isLeftMouseButton(e)){
            if (currentLine != null) {
                currentLine.setP2(sc.s2r(new ScreenPoint(e.getX(), e.getY())));
            } else if (editingLine != null) {

            }
        }
        repaint();

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    private static final double SCALE_STEP = 0.1;
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int clicks = e.getWheelRotation();
        double coef = 1 + SCALE_STEP*(clicks < 0? -1 : 1);
        double scale = 1;
        for (int i = Math.abs(clicks); i > 0; i--) {
            scale *= coef;
        }
        sc.changeScale(scale);
        repaint();
    }
}
