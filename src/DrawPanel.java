import drawers.*;
import figures.DescribingRectangle;
import figures.Line;
import figures.Polygon;
import points.RealPoint;
import points.ScreenConverter;
import points.ScreenPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private final ScreenConverter sc;
    private final Line ox;
    private final Line oy;
    private Line currentLine = null;
    private figures.Polygon currentPolygon = null;
    private java.util.List<Line> allLines = new ArrayList<>();
    private java.util.List<figures.Polygon> allPolygons = new ArrayList<>();
    private figures.Polygon editPolygon = null;
    private DescribingRectangle descRect = null;


    private final int EPS = 5;

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
        g.fillRect(0, 0, getWidth(), getHeight());

        drawLine(g, sc, ox, Color.BLUE);
        drawLine(g, sc, oy, Color.BLUE);

        for (Line l : allLines) {
            drawLine(g, sc, l, Color.BLACK);
        }

        if (currentLine != null) {
            drawLine(g, sc, currentLine, Color.red);
        }

        if (currentPolygon != null) {
            drawPolygon(g, sc, currentPolygon, Color.green);
        }

        if (descRect != null) {
            drawDescribingRect(g, sc, descRect, Color.green);
        }


        origG.drawImage(bi, 0, 0, null);
        g.dispose();
    }

    private static void drawLine(Graphics2D g, ScreenConverter sc, Line l, Color c) {
        ScreenPoint p1 = sc.r2s(l.getP1());
        ScreenPoint p2 = sc.r2s(l.getP2());
        DDALineDrawer dda = new DDALineDrawer(new GraphicsPixelDrawer(g));
//        BresenhamLineDrawer br = new BresenhamLineDrawer(new GraphicsPixelDrawer(g));
        dda.drawLine(p1.getC(), p1.getR(), p2.getC(), p2.getR(), c);
    }

    private static void drawEditPoint(Graphics2D g, ScreenConverter sc, RealPoint rp, Color c) {
        ScreenPoint p = sc.r2s(rp);
        DDALineDrawer dda = new DDALineDrawer(new GraphicsPixelDrawer(g));
//        BresenhamLineDrawer br = new BresenhamLineDrawer(new GraphicsPixelDrawer(g));

        dda.drawLine(p.getC() - 3, p.getR(), p.getC() + 3, p.getR(), c);
        dda.drawLine(p.getC(), p.getR() - 3, p.getC(), p.getR() + 3, c);
    }

    private static void drawDescribingRect(Graphics2D g, ScreenConverter sc, DescribingRectangle dRect, Color c) {
        DDALineDrawer dda = new DDALineDrawer(new GraphicsPixelDrawer(g));
//        BresenhamLineDrawer br = new BresenhamLineDrawer(new GraphicsPixelDrawer(g));
        for (int i = 0; i < dRect.getPointList().size() - 1; i++) {
            dda.drawLine((int) dRect.getPointList().get(i).getX(), (int) dRect.getPointList().get(i).getY(), (int) dRect.getPointList().get(i + 1).getX(), (int) dRect.getPointList().get(i + 1).getY(), c);
        }
    }

    private static void drawPolygon(Graphics2D g, ScreenConverter sc, Polygon poly, Color c) {
        DDALineDrawer dda = new DDALineDrawer(new GraphicsPixelDrawer(g));
//        BresenhamLineDrawer br = new BresenhamLineDrawer(new GraphicsPixelDrawer(g));
        for (int i = 0; i < poly.getPointList().size() - 1; i++) {
            dda.drawLine((int) poly.getPointList().get(i).getX(), (int) poly.getPointList().get(i).getY(), (int) poly.getPointList().get(i + 1).getX(), (int) poly.getPointList().get(i + 1).getY(), c);
        }
    }


    private static boolean isNear(ScreenConverter sc, RealPoint rp, ScreenPoint sp, int eps) {
        ScreenPoint p = sc.r2s(rp);
        return eps * eps > (p.getR() - sp.getR()) * (p.getR() - sp.getR()) + (p.getC() - sp.getC()) * (p.getC() - sp.getC());
    }

    private static double distanceToLine(ScreenPoint lp1, ScreenPoint lp2, ScreenPoint cp) {
        double a = lp2.getR() - lp1.getR();
        double b = -(lp2.getC() - lp1.getC());
        //b*x-a*y + cp.getC()*b + cp.getR()*a = 0
        //a*x+b*y + a*lp1.getC() - b*lp1.getR() = 0
        double e = cp.getC() * b + cp.getR() * a;
        double f = a * lp1.getC() - b * lp1.getR();
        double y = (a * e - b * f) / (a * a + b * b);
        double x = (a * y - e) / b;
        return Math.sqrt((cp.getC() - x) * (cp.getC() - x) + (cp.getR() - y) * (cp.getR() - y));
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
        RealPoint ra = l.getP1();
        RealPoint rb = l.getP2();
        return isNear(sc, ra, p, eps) || isNear(sc, rb, p, eps) || (distanceToLine(a, b, p) < eps && isPointInRect(a, b, p));
    }

    private static Line findLine(ScreenConverter sc, java.util.List<Line> lines, ScreenPoint searchPoint, int eps) {
        Line answer = null;
        for (Line l : lines) {
            if (closeToLine(sc, l, searchPoint, eps)) {
                return l;
            }
        }
        return null;
    }

    private static figures.Polygon findPolygon(ScreenConverter sc, java.util.List<figures.Polygon> polygons, ScreenPoint searchPoint, int eps) {
        for (figures.Polygon poly : polygons) {
            for (int i = 1; i < poly.getPointList().size() - 1; i++) {
                /*if (closeToLine(sc, new Line(poly.getPointList().get(i - 1), poly.getPointList().get(i)), searchPoint, eps)) {
                    return poly;
                }*/
                if (isNear(sc, poly.getPointList().get(i), searchPoint, eps)) {
                    return poly;
                }
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
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (currentLine == null) {
                RealPoint p = sc.s2r(new ScreenPoint(e.getX(), e.getY()));
                currentLine = new Line(p, p);
                currentPolygon = new Polygon();
                currentPolygon.add(p);
            } else if (allPolygons.size() != 0) {
                Polygon edit = findPolygon(sc, allPolygons, new ScreenPoint(e.getX(), e.getY()), EPS);
                if (edit != null) {
                    descRect = edit.getDescRect();
                    editPolygon = edit;
                } else editPolygon = null;
            }
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            prevPoint = null;
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (currentLine != null) {
                if ( isNear(sc, currentPolygon.getFirstPoint(), new ScreenPoint(e.getX(), e.getY()), EPS)) {
                    currentLine.setP2(currentPolygon.getFirstPoint());
                    allLines.add(currentLine);
                    currentLine = null;
                    allPolygons.add(currentPolygon);
                    currentPolygon = null;
                } else {
                    currentLine.setP2(sc.s2r(new ScreenPoint(e.getX(), e.getY())));
                    allLines.add(currentLine);
                    currentLine = null;
                    currentPolygon.add(sc.s2r(new ScreenPoint(e.getX(), e.getY())));
                }
            } else {

                repaint();
            }
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            /*if (editingLine != null) {
                if (closeToLine(sc, editingLine, new points.ScreenPoint(e.getX(), e.getY()), EPS)) {
                    allLines.remove(editingLine);
                    editingLine = null;
                }
            }*/
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
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (currentLine != null && allPolygons.size() == 0) {
                currentLine.setP2(sc.s2r(new ScreenPoint(e.getX(), e.getY())));
            }

        }
        repaint();

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (currentPolygon != null) {
            currentLine = new Line(currentPolygon.getLastPoint(), sc.s2r(new ScreenPoint(e.getX(), e.getY())));
        }
        repaint();
    }

    private static final double SCALE_STEP = 0.1;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int clicks = e.getWheelRotation();
        double coef = 1 + SCALE_STEP * (clicks < 0 ? -1 : 1);
        double scale = 1;
        for (int i = Math.abs(clicks); i > 0; i--) {
            scale *= coef;
        }
        sc.changeScale(scale);
        repaint();
    }
}