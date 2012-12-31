package eu.hansolo.fx.heatmap;

/**
 * Created by
 * User: hansolo
 * Date: 30.12.12
 * Time: 16:43
 */
public class HeatMapEvent {
    private double                  x;
    private double                  y;
    private double                  radius;
    private HeatMap.OpacityDistribution opacityDistribution;


    // ******************** Constructors **************************************
    public HeatMapEvent(final double X, final double Y) {
        this(X, Y, 15.5, HeatMap.OpacityDistribution.EXPONENTIAL);
    }

    public HeatMapEvent(final double X, final double Y, final double RADIUS) {
        this(X, Y, RADIUS, HeatMap.OpacityDistribution.EXPONENTIAL);
    }

    public HeatMapEvent(final double X, final double Y, final double RADIUS, final HeatMap.OpacityDistribution OPACITY_GRADIENT) {
        x               = X;
        y               = Y;
        radius          = RADIUS;
        opacityDistribution = OPACITY_GRADIENT;
    }


    // ******************** Methods *******************************************
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }
    public void setRadius(final double RADIUS) {
        radius = RADIUS;
    }

    public HeatMap.OpacityDistribution getOpacityDistribution() {
        return opacityDistribution;
    }
    public void setOpacityDistribution(final HeatMap.OpacityDistribution OPACITY_GRADIENT) {
        opacityDistribution = OPACITY_GRADIENT;
    }

}
