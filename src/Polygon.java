import java.awt.*;
import java.util.ArrayList;

public class Polygon {
    private ArrayList<RealPoint> pointList;

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
}
