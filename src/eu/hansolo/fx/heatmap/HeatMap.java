package eu.hansolo.fx.heatmap;

import javafx.animation.Interpolator;
import javafx.scene.SnapshotParametersBuilder;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by
 * User: hansolo
 * Date: 27.12.12
 * Time: 05:46
 */
public class HeatMap {
    public static enum ColorMapping {
        LIME_YELLOW_RED(new Stop(0.0, Color.LIME), new Stop(0.8, Color.YELLOW), new Stop(1.0, Color.RED)),
        BLUE_CYAN_GREEN_YELLOW_RED(new Stop(0.0, Color.BLUE), new Stop(0.25, Color.CYAN), new Stop(0.5, Color.LIME), new Stop(0.75, Color.YELLOW), new Stop(1.0, Color.RED)),
        INFRARED_1(new Stop(0.0, Color.BLACK), new Stop(0.1, Color.rgb(25, 20, 126)), new Stop(0.3, Color.rgb(192, 40, 150)), new Stop(0.5, Color.rgb(234, 82, 10)), new Stop(0.85, Color.rgb(255, 220, 25)), new Stop(1.0, Color.WHITE)),
        INFRARED_2(new Stop(0.0, Color.BLACK), new Stop(0.1, Color.rgb(1, 20, 127)), new Stop(0.2, Color.rgb(1, 13, 100)), new Stop(0.4, Color.rgb(95, 172, 68)), new Stop(0.5, Color.rgb(210, 197, 12)), new Stop(0.65, Color.rgb(225, 53, 56)), new Stop(1.0, Color.WHITE)),
        BLACK_WHITE(new Stop(0.0, Color.BLACK), new Stop(1.0, Color.WHITE)),
        WHITE_BLACK(new Stop(0.0, Color.WHITE), new Stop(1.0, Color.BLACK));

        public LinearGradient mapping;

        ColorMapping(final Stop... STOPS) {
            mapping = LinearGradientBuilder.create()
                                           .startX(0).startY(0)
                                           .endX(100).endY(0)
                                           .proportional(false)
                                           .cycleMethod(CycleMethod.NO_CYCLE)
                                           .stops(STOPS)
                                           .build();
        }
    }
    public static enum OpacityDistribution {
        EXPONENTIAL(0.90, 0.37, 0.14, 0.05, 0.02, 0.006, 0.002, 0.001, 0.0003, 0.0001, 0.0),
        TAN_HYP(0.90, 0.53, 0.24, 0.10, 0.04, 0.01, 0.005, 0.002, 0.0007, 0.0002, 0.0),
        CUSTOM(0.90, 0.56, 0.40, 0.28, 0.20, 0.14, 0.10, 0.07, 0.05, 0.03, 0.0),
        LINEAR(0.90, 0.81, 0.72, 0.63, 0.54, 0.45, 0.36, 0.27, 0.18, 0.09, 0.0);

        public double[] distribution;

        private OpacityDistribution(final double... DISTRIBUTION) {
            distribution = DISTRIBUTION;
        }

    }
    private List<HeatMapEvent>  eventList;
    private Map<String, Image>  eventImages;
    private ColorMapping        colorMapping;
    private LinearGradient      mappingGradient;
    private boolean             fadeColors;
    private double              radius;
    private OpacityDistribution opacityDistribution;
    private Image               eventImage;
    private Canvas              monochrome;
    private GraphicsContext     ctx;
    private WritableImage       monochromeImage;
    private WritableImage       heatMap;
    private ImageView           heatMapView;


    // ******************** Constructors **************************************
    public HeatMap(final double WIDTH, final double HEIGHT) {
        this(WIDTH, HEIGHT, ColorMapping.LIME_YELLOW_RED);
    }

    public HeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING) {
        this(WIDTH, HEIGHT, COLOR_MAPPING, 15.5);
    }

    public HeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING, final double EVENT_RADIUS) {
        this(WIDTH, HEIGHT, COLOR_MAPPING, EVENT_RADIUS, true);
    }

    public HeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING, final double EVENT_RADIUS, final boolean FADE_COLORS) {
        eventList           = new ArrayList<>();
        eventImages         = new HashMap<>();
        colorMapping        = COLOR_MAPPING;
        mappingGradient     = colorMapping.mapping;
        fadeColors          = FADE_COLORS;
        radius              = EVENT_RADIUS;
        opacityDistribution = OpacityDistribution.CUSTOM;
        eventImage          = createEventImage(radius, opacityDistribution);
        monochrome          = new Canvas(WIDTH, HEIGHT);
        ctx                 = monochrome.getGraphicsContext2D();
        heatMapView         = new ImageView(heatMap);
        heatMapView.setMouseTransparent(true);
        heatMapView.setOpacity(0.5);
    }


    // ******************** Methods *******************************************
    /**
     * Returns the image view that contains the heat map
     * @return the image view that contains the heat map
     */
    public ImageView getHeatMapImage() {
        return heatMapView;
    }

    /**
     * Visualizes an event with the given radius and opacity gradient
     * @param X
     * @param Y
     * @param OFFSET_X
     * @param OFFSET_Y
     * @param RADIUS
     * @param OPACITY_GRADIENT
     */
    public void addEvent(final double X, final double Y, final double OFFSET_X, final double OFFSET_Y, final double RADIUS, final OpacityDistribution OPACITY_GRADIENT) {
        eventImage = createEventImage(RADIUS, OPACITY_GRADIENT);
        addEvent(X, Y, eventImage, OFFSET_X, OFFSET_Y);
    }

    /**
     * Visualizes an event with a given image at the given position and with
     * the given offset. So one could use different weighted images for different
     * kinds of events (e.g. important events more opaque as unimportant events)
     * @param X
     * @param Y
     * @param EVENT_IMAGE
     * @param OFFSET_X
     * @param OFFSET_Y
     */
    public void addEvent(final double X, final double Y, final Image EVENT_IMAGE, final double OFFSET_X, final double OFFSET_Y) {
        eventList.add(new HeatMapEvent(X, Y, radius, opacityDistribution));
        ctx.drawImage(EVENT_IMAGE, X - OFFSET_X, Y - OFFSET_Y);
        updateHeatMap();
    }

    /**
     * If you don't need to weight events you could use this method which
     * will create events that always use the global weight
     * @param X
     * @param Y
     */
    public void addEvent(final double X, final double Y) {
        addEvent(X, Y, eventImage, radius, radius);
    }

    /**
     * Calling this method will lead to a clean new heat map without any data
     */
    public void clearHeatMap() {
        eventList.clear();
        ctx.clearRect(0, 0, monochrome.getWidth(), monochrome.getHeight());
        monochromeImage = new WritableImage(monochrome.widthProperty().intValue(), monochrome.heightProperty().intValue());
        updateHeatMap();
    }

    /**
     * Returns the current opacity of the heat map image view
     * @return the current opacity of the heat map image view
     */
    public double getHeatMapOpacity() {
        return heatMapView.getOpacity();
    }

    /**
     * In principle this method is not needed because you could adjust the opacity
     * of the heat map directly (it's a simple image view)
     * @param HEAT_MAP_OPACITY
     */
    public void setHeatMapOpacity(final double HEAT_MAP_OPACITY) {
        double opacity = HEAT_MAP_OPACITY < 0 ? 0 : (HEAT_MAP_OPACITY > 1 ? 1 : HEAT_MAP_OPACITY);
        heatMapView.setOpacity(opacity);
    }

    /**
     * Returns the used color mapping with the gradient that is used
     * to visualize the data
     * @return
     */
    public ColorMapping getColorMapping() {
        return colorMapping;
    }

    /**
     * The ColorMapping enum contains some examples for color mappings
     * that might be useful to visualize data and here you could set
     * the one you like most. Setting another color mapping will recreate
     * the heat map automatically.
     * @param COLOR_MAPPING
     */
    public void setColorMapping(final ColorMapping COLOR_MAPPING) {
        colorMapping    = COLOR_MAPPING;
        mappingGradient = COLOR_MAPPING.mapping;
        updateHeatMap();
    }

    /**
     * Returns true if the heat map is used to visualize frequencies (default)
     * @return true if the heat map is used to visualize frequencies
     */
    public boolean isFadeColors() {
        return fadeColors;
    }

    /**
     * If true each event will be visualized by a radial gradient
     * with the colors from the given color mapping and decreasing
     * opacity from the inside to the outside. If you set it to false
     * the color opacity won't fade out but will be opaque. This might
     * be handy if you would like to visualize the density instead of
     * the frequency
     * @param FADE_COLORS
     */
    public void setFadeColors(final boolean FADE_COLORS) {
        fadeColors = FADE_COLORS;
        updateHeatMap();
    }

    /**
     * Returns the radius of the circle that is used to visualize an
     * event.
     * @return the radius of the circle that is used to visualize an event
     */
    public double getEventRadius() {
        return radius;
    }

    /**
     * Each event will be visualized by a circle filled with a radial
     * gradient with decreasing opacity from the inside to the outside.
     * If you have lot's of events it makes sense to set the event radius
     * to a smaller value. The default value is 15.5
     * @param RADIUS
     */
    public void setEventRadius(final double RADIUS) {
        radius     = RADIUS < 1 ? 1 : RADIUS;
        eventImage = createEventImage(radius, opacityDistribution);
    }

    /**
     * Returns the opacity distribution that will be used to visualize
     * the events in the monochrome map. If you have lot's of events
     * it makes sense to reduce the radius and the set the opacity
     * distribution to exponential.
     * @return the opacity distribution of events in the monochrome map
     */
    public OpacityDistribution getOpacityDistribution() {
        return opacityDistribution;
    }

    /**
     * Changing the opacity distribution will affect the smoothing of
     * the heat map. If you choose a linear opacity distribution you will
     * see bigger colored dots for each event than using the exponential
     * opacity distribution (at the same event radius).
     * @param OPACITY_DISTRIBUTION
     */
    public void setOpacityDistribution(final OpacityDistribution OPACITY_DISTRIBUTION) {
        opacityDistribution = OPACITY_DISTRIBUTION;
        eventImage          = createEventImage(radius, opacityDistribution);
    }

    /**
     * Because the heat map is based on images you have to create a new
     * writeable image each time you would like to change the size of
     * the heatmap
     * @param WIDTH
     * @param HEIGHT
     */
    public void setSize(final double WIDTH, final double HEIGHT) {
        monochrome.setWidth(WIDTH);
        monochrome.setHeight(HEIGHT);
        if (WIDTH > 0 && HEIGHT > 0) {
            monochromeImage = new WritableImage(monochrome.widthProperty().intValue(), monochrome.heightProperty().intValue());
            updateHeatMap();
        }
    }

    /**
     * Create an image that contains a circle filled with a
     * radial gradient from white to transparent
     * @param RADIUS
     * @return an image that contains a filled circle
     */
    public Image createEventImage(final double RADIUS, final OpacityDistribution OPACITY_GRADIENT) {
        Double radius = RADIUS < 1 ? 1 : RADIUS;
        if (eventImages.containsKey(OPACITY_GRADIENT.name() + radius)) {
            return eventImages.get(OPACITY_GRADIENT.name() + radius);
        }
        Stop[] stops = new Stop[11];
        for (int i = 0 ; i < 11 ; i++) {
            stops[i] = new Stop(i * 0.1, Color.rgb(255, 255, 255, OPACITY_GRADIENT.distribution[i]));
        }
        int           size          = (int) (radius * 2);
        WritableImage raster        = new WritableImage(size, size);
        PixelWriter pixelWriter   = raster.getPixelWriter();
        double        maxDistFactor = 1 / radius;
        Color         pixelColor;
        for (int y = 0 ; y < size ; y++) {
            for (int x = 0 ; x < size ; x++) {
                double distanceX = radius - x;
                double distanceY = radius - y;
                double distance = Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));
                double fraction = maxDistFactor * distance;
                for (int i = 0 ; i < 10 ; i++) {
                    if (Double.compare(fraction, stops[i].getOffset()) >= 0 && Double.compare(fraction, stops[i + 1].getOffset()) <= 0) {
                        pixelColor  = (Color) Interpolator.LINEAR.interpolate(stops[i].getColor(), stops[i + 1].getColor(), (fraction - stops[i].getOffset()) / 0.1);
                        pixelWriter.setColor(x, y, pixelColor);
                        break;
                    }
                }
            }
        }
        eventImages.put(OPACITY_GRADIENT.name() + radius, raster);
        return raster;
    }

    /**
     * Updates each event in the monochrome map to the given opacity gradient
     * which could be useful to reduce oversmoothing
     * @param OPACITY_GRADIENT
     */
    public void updateMonochromeMap(final OpacityDistribution OPACITY_GRADIENT) {
        ctx.clearRect(0, 0, monochrome.getWidth(), monochrome.getHeight());
        for (HeatMapEvent event : eventList) {
            event.setOpacityDistribution(OPACITY_GRADIENT);
            ctx.drawImage(createEventImage(event.getRadius(), event.getOpacityDistribution()), event.getX() - event.getRadius() * 0.5, event.getY() - event.getRadius() * 0.5);
        }
        updateHeatMap();
    }

    /**
     * Recreates the heatmap based on the current monochrome map.
     * Using this approach makes it easy to change the used color
     * mapping.
     */
    private void updateHeatMap() {
        monochrome.snapshot(SnapshotParametersBuilder.create().fill(Color.TRANSPARENT).build(), monochromeImage);
        heatMap = new WritableImage(monochromeImage.widthProperty().intValue(), monochromeImage.heightProperty().intValue());
        PixelWriter pixelWriter = heatMap.getPixelWriter();
        PixelReader pixelReader = monochromeImage.getPixelReader();
        Color colorFromMonoChromeImage;
        double brightness;
        Color mappedColor;
        for (int y = 0 ; y < monochromeImage.getHeight() ; y++) {
            for (int x = 0 ; x < monochromeImage.getWidth(); x++) {
                colorFromMonoChromeImage = pixelReader.getColor(x, y);
                //double brightness = computeLuminance(colorFromMonoChromeImage.getRed(), colorFromMonoChromeImage.getGreen(), colorFromMonoChromeImage.getBlue());
                //double brightness = computeBrightness(colorFromMonoChromeImage.getRed(), colorFromMonoChromeImage.getGreen(), colorFromMonoChromeImage.getBlue());
                brightness = computeBrightnessFast(colorFromMonoChromeImage.getRed(), colorFromMonoChromeImage.getGreen(), colorFromMonoChromeImage.getBlue());
                mappedColor = getColorAt(mappingGradient, brightness);
                if (fadeColors) {
                    pixelWriter.setColor(x, y, Color.color(mappedColor.getRed(), mappedColor.getGreen(), mappedColor.getBlue(), brightness));
                    //pixelWriter.setColor(x, y, Color.color(mappedColor.getRed(), mappedColor.getGreen(), mappedColor.getBlue(), colorFromMonoChromeImage.getOpacity()));
                } else {
                    pixelWriter.setColor(x, y, mappedColor);
                }
            }
        }
        heatMapView.setImage(heatMap);
    }

    /**
     * Photometric/digital ITU-R
     * @param RED
     * @param GREEN
     * @param BLUE
     * @return Photometric/digital ITU-R (0...1)
     */
    private double computeBrightness(final double RED, final double GREEN, final double BLUE) {
        return  (0.2126 * RED + 0.7152 * GREEN + 0.0722 * BLUE);
    }

    /**
     * Faster approximation of Photometric/digital ITU-R
     * @param RED
     * @param GREEN
     * @param BLUE
     * @return fast approximation of photometric/digital ITU-R (0...1)
     */
    private double computeBrightnessFast(final double RED, final double GREEN, final double BLUE) {
        return ((RED + RED + BLUE + GREEN + GREEN + GREEN) / 6.0);
    }

    /**
     * Digital CCIR601 (more weight to red and blue)
     * @param RED
     * @param GREEN
     * @param BLUE
     * @return Digital CCIR601 (0...1)
     */
    private double computePerceivedBrightness(final double RED, final double GREEN, final double BLUE) {
        return ((0.299 * RED) + (0.587 * GREEN) + (0.114 * BLUE));
    }

    /**
     * Faster approximation of Digital CCIR601
     * @param RED
     * @param GREEN
     * @param BLUE
     * @return fast approximation of Digital CCIR601 (0...1)
     */
    private double computePerceivedBrightnessFast(final double RED, final double GREEN, final double BLUE) {
        return ((RED + RED + RED  + BLUE + GREEN + GREEN + GREEN + GREEN) * 0.5);
    }

    /**
     * Luminance
     * @param RED
     * @param GREEN
     * @param BLUE
     * @return luminance (0...1)
     */
    private double computeLuminance(final double RED, final double GREEN, final double BLUE) {
        return Math.sqrt(0.241 * (RED * RED) + 0.691 * (GREEN * GREEN) + 0.068 * (BLUE * BLUE));
    }

    /**
     * Calculates the color in a linear gradient at the given fraction
     * @param GRADIENT
     * @param FRACTION
     * @return the color in a linear gradient at the given fraction
     */
    private Color getColorAt(final LinearGradient GRADIENT, final double FRACTION) {
        double fraction = FRACTION < 0f ? 0f : (FRACTION > 1 ? 1 : FRACTION);
        double lowerLimit = 0;
        int    lowerIndex = 0;
        double upperLimit = 1;
        int    upperIndex = 1;
        int    index = 0;
        List<Stop> stops     = GRADIENT.getStops();
        List<Color>  colors    = new LinkedList<>();
        List<Double> fractions = new LinkedList<>();
        for (Stop stop : stops) {
            fractions.add(stop.getOffset());
            colors.add(stop.getColor());
        }
        for (double currentFraction : fractions) {
            if (Double.compare(currentFraction, fraction) < 0) {
                lowerLimit = currentFraction;
                lowerIndex = index;
            }
            if (Double.compare(currentFraction, fraction) == 0) {
                return colors.get(index);
            }
            if (Double.compare(currentFraction, fraction) > 0) {
                upperLimit = currentFraction;
                upperIndex = index;
                break;
            }
            index++;
        }
        double interpolationFraction = (fraction - lowerLimit) / (upperLimit - lowerLimit);
        return (Color) Interpolator.LINEAR.interpolate(colors.get(lowerIndex), colors.get(upperIndex), interpolationFraction);
    }
}
