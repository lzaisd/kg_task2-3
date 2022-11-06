package figures;

import points.RealPoint;

import java.awt.*;
import java.util.ArrayList;


public class Polygon {
    protected ArrayList<RealPoint> pointList;

    public Polygon() {
        this.pointList = new ArrayList<>();
    }

    public ArrayList<RealPoint> getPointList() {
        return pointList;
    }

    public void setPointList(ArrayList<RealPoint> pl) {
        this.pointList = pl;
    }

    public void add(RealPoint p) {
        this.pointList.add(p);
    }

    public RealPoint getLastPoint() {
        return this.getPointList().get(this.getPointList().size() - 1);
    }

    public RealPoint getFirstPoint() {
        return this.getPointList().get(0);
    }

    public DescribingRectangle getDescRect() {
        double minX = this.getFirstPoint().getX();
        double maxX = this.getFirstPoint().getX();
        double minY = this.getFirstPoint().getY();
        double maxY = this.getFirstPoint().getY();
        for (int i = 1; i < this.getPointList().size(); i ++){
            if (this.getPointList().get(i).getX() < minX) {
                minX = this.getPointList().get(i).getX();
            }
            if (this.getPointList().get(i).getX() > maxX) {
                maxX = this.getPointList().get(i).getX();
            }
            if (this.getPointList().get(i).getY() < minY) {
                minY = this.getPointList().get(i).getY();
            }
            if (this.getPointList().get(i).getY() > maxY) {
                maxY = this.getPointList().get(i).getY();
            }
        }
        return new DescribingRectangle(minX, maxX, minY, maxY);
    }
}
