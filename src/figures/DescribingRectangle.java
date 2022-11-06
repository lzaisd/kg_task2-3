package figures;

import points.RealPoint;

import java.util.ArrayList;

public class DescribingRectangle extends Polygon{
    public DescribingRectangle(double minX, double maxX, double minY, double maxY) {
        this.pointList = new ArrayList<>();
        this.pointList.add(new RealPoint(minX, minY));
        this.pointList.add(new RealPoint(minX, maxY));
        this.pointList.add(new RealPoint(maxX, maxY));
        this.pointList.add(new RealPoint(maxX, minY));
    }
}
