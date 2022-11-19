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
}
