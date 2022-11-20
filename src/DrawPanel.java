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


    private static final int EPS = 10;

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

        for (Polygon p : allPolygons) {
            drawPolygon(g, sc, p, Color.BLACK);
        }
        if (currentPolygon != null) {
            drawCurPolygon(g, sc, currentPolygon, Color.black);
        }

        if (currentLine != null) {
            drawLine(g, sc, currentLine, Color.red);
        }

        if (editPolygon != null) {
            drawPolygon(g, sc, editPolygon, new Color(229, 0, 255));
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


    private static void drawDescribingRect(Graphics2D g, ScreenConverter sc, DescribingRectangle dRect, Color c) {
        DDALineDrawer dda = new DDALineDrawer(new GraphicsPixelDrawer(g));
//        BresenhamLineDrawer br = new BresenhamLineDrawer(new GraphicsPixelDrawer(g));
        ScreenPoint sp = sc.r2s(dRect.getUpLeftP());
        ScreenPoint bm = sc.r2s(dRect.getBottomMiddle());
        int spX = sp.getC();
        int spY = sp.getR();
        int width = sc.r2sForXLine(dRect.getWidth());
        int height = sc.r2sForYLine(dRect.getHeight());
        int bmX = bm.getC();
        int bmY = bm.getR();

        g.setColor(c);
        g.fillRect(bmX - 2, bmY - 2, 4, 4);

        dda.drawLine(spX, spY, spX + width, spY, c);
        dda.drawLine(spX + width, spY + height, spX + width, spY, c);
        dda.drawLine(spX + width, spY + height, spX, spY + height, c);
        dda.drawLine(spX, spY, spX, spY + height, c);

    }

    private static void drawPolygon(Graphics2D g, ScreenConverter sc, Polygon poly, Color c) { //todo выбор drawer'а в интерфейсе
        DDALineDrawer dda = new DDALineDrawer(new GraphicsPixelDrawer(g));
        ScreenPoint a = sc.r2s(poly.getPointList().get(0));
        ScreenPoint b = sc.r2s(poly.getLastPoint());
        dda.drawLine(a.getC(), a.getR(), b.getC(), b.getR(), c);

//        BresenhamLineDrawer br = new BresenhamLineDrawer(new GraphicsPixelDrawer(g));
        for (int i = 0; i < poly.getPointList().size() - 1; i++) {
            a = sc.r2s(poly.getPointList().get(i));
            b = sc.r2s(poly.getPointList().get(i + 1));
            dda.drawLine(a.getC(), a.getR(), b.getC(), b.getR(), c);
        }
    }
    private static void drawCurPolygon(Graphics2D g, ScreenConverter sc, Polygon poly, Color c) {
        DDALineDrawer dda = new DDALineDrawer(new GraphicsPixelDrawer(g));
        for (int i = 0; i < poly.getPointList().size() - 1; i++) {
            ScreenPoint a = sc.r2s(poly.getPointList().get(i));
            ScreenPoint b = sc.r2s(poly.getPointList().get(i + 1));
            dda.drawLine(a.getC(), a.getR(), b.getC(), b.getR(), c);
        }
    }


    private static boolean isNear(ScreenConverter sc, RealPoint rp, ScreenPoint sp, int eps) {
        ScreenPoint p = sc.r2s(rp);
        return eps * eps > (p.getR() - sp.getR()) * (p.getR() - sp.getR()) + (p.getC() - sp.getC()) * (p.getC() - sp.getC());
    }

    private static boolean isPointInRect(ScreenPoint pr1, ScreenPoint pr2, ScreenPoint cp) {
        return cp.getC() >= Math.min(pr1.getC(), pr2.getC()) &&
                cp.getC() <= Math.max(pr1.getC(), pr2.getC()) &&
                cp.getR() >= Math.min(pr1.getR(), pr2.getR()) &&
                cp.getR() <= Math.max(pr1.getR(), pr2.getR());
    }

    private static figures.Polygon findPolygon(ScreenConverter sc, java.util.List<figures.Polygon> polygons, ScreenPoint searchPoint) {
        for (figures.Polygon poly : polygons) {
            for (int i = 0; i < poly.getPointList().size(); i++) {
                if (isNear(sc, poly.getPointList().get(i), searchPoint, EPS)) {
                    return poly;
                }
            }
        }
        return null;
    }

    private static void changePointPosition(DescribingRectangle descRect, RealPoint rp, double widthD, double heightD) {
        double upLeftX = descRect.getUpLeftP().getX();
        double upLeftY = descRect.getUpLeftP().getY();
        double width = descRect.getWidth();
        double height = descRect.getHeight();
        double xCoef = (rp.getX() - upLeftX) / width;
        double yCoef = (upLeftY - rp.getY()) / height;
        rp.setX(rp.getX() + xCoef * widthD);
        rp.setY(rp.getY() + yCoef * heightD);
    }

    private static void changePolyPosition(DescribingRectangle descRect, Polygon poly, double widthD, double heightD){
        for (int i = 0; i < poly.getPointList().size(); i++) {
            changePointPosition(descRect, poly.getPointList().get(i), widthD, heightD);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private ScreenPoint prevPoint = null;

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            prevPoint = new ScreenPoint(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (descRect == null) {
                prevPoint = null;
                if (allPolygons != null && currentPolygon == null) {
                    Polygon edit = findPolygon(sc, allPolygons, new ScreenPoint(e.getX(), e.getY()));
                    if (edit != null) {
                        descRect = edit.getDescRect();
                        editPolygon = edit;
                        allPolygons.remove(edit);
                    } else {
                        editPolygon = null;
                        descRect = null;
                    }
                }
            } else {
                ScreenPoint curP  = new ScreenPoint(e.getX(), e.getY());
                if (isNear(sc, descRect.getBottomMiddle(), curP, EPS)) {
                    currentPolygon = null;
                    RealPoint upL = descRect.getUpLeftP();
                    ScreenPoint upLeft = sc.r2s(upL);
                    ScreenPoint downRight = sc.r2s(new RealPoint(upL.getX() + descRect.getWidth(), upL.getY() - descRect.getHeight()));

                    int screenWidth = downRight.getC() - upLeft.getC();
                    int screenHeight = downRight.getR() - upLeft.getR();

                    double width = sc.s2rForXLine(screenWidth);
                    double height = sc.s2rForYLine(screenHeight);

                    double widthD = descRect.getWidth() - width;
                    double heightD = descRect.getHeight() - height;

                    changePolyPosition(descRect, editPolygon, 0, heightD);

                    descRect.setWidth(width);
                    descRect.setHeight(height);
                } else {
                    allPolygons.add(editPolygon);
                    editPolygon = null;
                    descRect = null;
                }
            }
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
        } else if (SwingUtilities.isMiddleMouseButton(e)) { //todo удаление многоугольника
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
            if (descRect != null) {
                currentPolygon = null;
                ScreenPoint p = new ScreenPoint(e.getX(), e.getY());
                RealPoint leftCorner = descRect.getUpLeftP();
                if (isNear(sc, descRect.getBottomMiddle(), p, EPS)) {
                    ScreenPoint lc = sc.r2s(leftCorner);
//                    double width = Math.abs(sc.s2rForXLine(p.getC() - lc.getC()));
                    double height = Math.abs(sc.s2rForYLine(p.getR() - lc.getR()));

//                    double widthD = descRect.getWidth() - width;
                    double heightD = descRect.getHeight() - height;
                    changePolyPosition(descRect, editPolygon, 0, heightD);

                    descRect.setHeight(height);
//                    descRect.setWidth(width);
                }
                repaint();

            } else {
                ScreenPoint curPoint = new ScreenPoint(e.getX(), e.getY());
                RealPoint p1 = sc.s2r(curPoint);
                RealPoint p2 = sc.s2r(prevPoint);
                RealPoint delta = p2.minus(p1);
                sc.moveCorner(delta);
                prevPoint = curPoint;
            }
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            descRect = null;
            editPolygon = null;
            if (currentLine == null) {
                RealPoint p = sc.s2r(new ScreenPoint(e.getX(), e.getY()));
                currentLine = new Line(p, p);
                currentPolygon = new Polygon();
                currentPolygon.add(p);
            } else {
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