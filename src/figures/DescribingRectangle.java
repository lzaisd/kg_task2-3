package figures;

import points.RealPoint;

public class DescribingRectangle {
    private RealPoint upLeftP;
    private double width;
    private double height;

    public DescribingRectangle(RealPoint upLeftP, double width, double height) {
        this.upLeftP = upLeftP;
        this.width = width;
        this.height = height;
    }

    public RealPoint getUpLeftP() {
        return upLeftP;
    }

    public void setUpLeftP(RealPoint upLeftP) {
        this.upLeftP = upLeftP;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public RealPoint getBottomMiddle() {
        return new RealPoint(upLeftP.getX() + width / 2, upLeftP.getY() - height);
    }
//    public RealPoint getUpMiddle() {
//        return upMiddle;
//    }
    public RealPoint getRightMiddle() {
        return new RealPoint(upLeftP.getX() + width, upLeftP.getY() - height / 2);
    }
//    public RealPoint getLeftMiddle() {
//        return leftMiddle;
//    }
}
