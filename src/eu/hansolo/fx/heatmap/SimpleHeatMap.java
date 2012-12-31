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

import java.util.LinkedList;
import java.util.List;


/**
 * Created by
 * User: hansolo
 * Date: 31.12.12
 * Time: 07:49
 */
public class SimpleHeatMap {
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
    private ColorMapping    colorMapping;
    private LinearGradient  mappingGradient;
    private boolean         fadeColors;
    private double          radius;
    private Image           eventImage;
    private Canvas          monochrome;
    private GraphicsContext ctx;
    private WritableImage   monochromeImage;
    private WritableImage   heatMap;
    private ImageView       heatMapView;


    // ******************** Constructors **************************************
    public SimpleHeatMap(final double WIDTH, final double HEIGHT) {
        this(WIDTH, HEIGHT, ColorMapping.LIME_YELLOW_RED);
    }

    public SimpleHeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING) {
        this(WIDTH, HEIGHT, COLOR_MAPPING, 15.5);
    }

    public SimpleHeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING, final double EVENT_RADIUS) {
        this(WIDTH, HEIGHT, COLOR_MAPPING, EVENT_RADIUS, true);
    }

    public SimpleHeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING, final double EVENT_RADIUS, final boolean FADE_COLORS) {
        colorMapping        = COLOR_MAPPING;
        mappingGradient     = colorMapping.mapping;
        fadeColors          = FADE_COLORS;
        radius              = EVENT_RADIUS;
        eventImage          = createEventImage(radius);
        monochrome          = new Canvas(WIDTH, HEIGHT);
        ctx                 = monochrome.getGraphicsContext2D();
        heatMapView         = new ImageView(heatMap);
        heatMapView.setMouseTransparent(true);
        heatMapView.setOpacity(0.5);
    }


    // ******************** Methods *******************************************
    public ImageView getHeatMapImage() {
        return heatMapView;
    }

    public void addEvent(final double X, final double Y, final Image EVENT_IMAGE, final double OFFSET_X, final double OFFSET_Y) {
        ctx.drawImage(EVENT_IMAGE, X - OFFSET_X, Y - OFFSET_Y);
        updateHeatMap();
    }

    public void addEvent(final double X, final double Y) {
        addEvent(X, Y, eventImage, radius, radius);
    }

    public void clearHeatMap() {
        ctx.clearRect(0, 0, monochrome.getWidth(), monochrome.getHeight());
        monochromeImage = new WritableImage(monochrome.widthProperty().intValue(), monochrome.heightProperty().intValue());
        updateHeatMap();
    }

    public double getHeatMapOpacity() {
        return heatMapView.getOpacity();
    }

    public void setHeatMapOpacity(final double HEAT_MAP_OPACITY) {
        double opacity = HEAT_MAP_OPACITY < 0 ? 0 : (HEAT_MAP_OPACITY > 1 ? 1 : HEAT_MAP_OPACITY);
        heatMapView.setOpacity(opacity);
    }

    public ColorMapping getColorMapping() {
        return colorMapping;
    }

    public void setColorMapping(final ColorMapping COLOR_MAPPING) {
        colorMapping    = COLOR_MAPPING;
        mappingGradient = COLOR_MAPPING.mapping;
        updateHeatMap();
    }

    public boolean isFadeColors() {
        return fadeColors;
    }

    public void setFadeColors(final boolean FADE_COLORS) {
        fadeColors = FADE_COLORS;
        updateHeatMap();
    }

    public double getEventRadius() {
        return radius;
    }

    public void setEventRadius(final double RADIUS) {
        radius     = RADIUS < 1 ? 1 : RADIUS;
        eventImage = createEventImage(radius);
    }

    public void setSize(final double WIDTH, final double HEIGHT) {
        monochrome.setWidth(WIDTH);
        monochrome.setHeight(HEIGHT);
        if (WIDTH > 0 && HEIGHT > 0) {
            monochromeImage = new WritableImage(monochrome.widthProperty().intValue(), monochrome.heightProperty().intValue());
            updateHeatMap();
        }
    }

    public Image createEventImage(final double RADIUS) {
        radius = RADIUS < 1 ? 1 : RADIUS;
        Stop[] stops = {
            new Stop(0.0, Color.rgb(255, 255, 255, 0.90)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.56)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.40)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.28)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.20)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.14)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.10)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.07)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.05)),
            new Stop(0.0, Color.rgb(255, 255, 255, 0.03)),
            new Stop(0.0, Color.TRANSPARENT)
        };

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
        return raster;
    }

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

    private double computeBrightness(final double RED, final double GREEN, final double BLUE) {
        return  (0.2126 * RED + 0.7152 * GREEN + 0.0722 * BLUE);
    }

    private double computeBrightnessFast(final double RED, final double GREEN, final double BLUE) {
        return ((RED + RED + BLUE + GREEN + GREEN + GREEN) / 6.0);
    }

    private double computePerceivedBrightness(final double RED, final double GREEN, final double BLUE) {
        return ((0.299 * RED) + (0.587 * GREEN) + (0.114 * BLUE));
    }

    private double computePerceivedBrightnessFast(final double RED, final double GREEN, final double BLUE) {
        return ((RED + RED + RED  + BLUE + GREEN + GREEN + GREEN + GREEN) * 0.5);
    }

    private double computeLuminance(final double RED, final double GREEN, final double BLUE) {
        return Math.sqrt(0.241 * (RED * RED) + 0.691 * (GREEN * GREEN) + 0.068 * (BLUE * BLUE));
    }

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
