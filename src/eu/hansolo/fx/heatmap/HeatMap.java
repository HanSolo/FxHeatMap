/*
 * Copyright (c) 2014 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.heatmap;

import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by
 * User: hansolo
 * Date: 27.12.12
 * Time: 05:46
 */
public class HeatMap extends ImageView {
    private static final SnapshotParameters SNAPSHOT_PARAMETERS = new SnapshotParameters();
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


    // ******************** Constructors **************************************
    public HeatMap() {
        this(100, 100);
    }
    public HeatMap(final double WIDTH, final double HEIGHT) {
        this(WIDTH, HEIGHT, ColorMapping.LIME_YELLOW_RED);
    }
    public HeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING) {
        this(WIDTH, HEIGHT, COLOR_MAPPING, 15.5);
    }
    public HeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING, final double EVENT_RADIUS) {
        this(WIDTH, HEIGHT, COLOR_MAPPING, EVENT_RADIUS, true, 0.5, OpacityDistribution.CUSTOM);
    }
    public HeatMap(final double WIDTH, final double HEIGHT, ColorMapping COLOR_MAPPING, final double EVENT_RADIUS, final boolean FADE_COLORS, final double HEAT_MAP_OPACITY, final OpacityDistribution OPACITY_DISTRIBUTION) {
        super();
        SNAPSHOT_PARAMETERS.setFill(Color.TRANSPARENT);
        eventList           = new ArrayList<>();
        eventImages         = new HashMap<>();
        colorMapping        = COLOR_MAPPING;
        mappingGradient     = colorMapping.mapping;
        fadeColors          = FADE_COLORS;
        radius              = EVENT_RADIUS;
        opacityDistribution = OPACITY_DISTRIBUTION;
        eventImage          = createEventImage(radius, opacityDistribution);
        monochrome          = new Canvas(WIDTH, HEIGHT);
        ctx                 = monochrome.getGraphicsContext2D();
        monochromeImage     = new WritableImage((int) WIDTH, (int) HEIGHT);
        setImage(heatMap);
        setMouseTransparent(true);
        setOpacity(HEAT_MAP_OPACITY);
        registerListeners();
    }

    public void registerListeners() {
        fitWidthProperty().addListener(observable -> resize());
        fitHeightProperty().addListener(observable -> resize());
    }


    // ******************** Methods *******************************************              
    /**
     * Add a list of events and update the heatmap after all events
     * have been added
     * @param EVENTS
     */
    public void addEvents(final Point2D... EVENTS) {
        for (Point2D event : EVENTS) {
            eventList.add(new HeatMapEvent(event.getX(), event.getY(), radius, opacityDistribution));
            ctx.drawImage(eventImage, event.getX() - radius, event.getY() - radius);
        }
        updateHeatMap();
    }

    /**
     * Add a list of events and update the heatmap after all events
     * have been added
     * @param EVENTS
     */
    public void addEvents(final List<Point2D> EVENTS) {
        EVENTS.forEach(event -> {
            eventList.add(new HeatMapEvent(event.getX(), event.getY(), radius, opacityDistribution));
            ctx.drawImage(eventImage, event.getX() - radius, event.getY() - radius);
        });
        updateHeatMap();
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
        radius = RADIUS < 1 ? 1 : RADIUS;
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
        setFitWidth(WIDTH);
        setFitHeight(HEIGHT);
    }

    /**
     * Saves the current heat map image as png with the given name to the desktop folder of the current user
     * @param FILE_NAME
     */
    public void saveAsPng(final String FILE_NAME) {        
        saveAsPng(this, FILE_NAME + ".png");
    }

    /**
     * Saves the given node as png with the given name to the desktop folder of the current user
     * @param NODE
     * @param FILE_NAME
     */
    public void saveAsPng(final Node NODE, final String FILE_NAME) {
        new Thread(() ->
                       Platform.runLater(() -> {
                           final String TARGET = System.getProperty("user.home") + "/Desktop/" + FILE_NAME + ".png";
                           try {
                               ImageIO.write(SwingFXUtils.fromFXImage(NODE.snapshot(SNAPSHOT_PARAMETERS, null), null), "png", new File(TARGET));
                           } catch (IOException exception) {
                               // handle exception here
                           }
                       })
        ).start();    
    }
    
    /**
     * Create an image that contains a circle filled with a
     * radial gradient from white to transparent
     * @param RADIUS
     * @return an image that contains a filled circle
     */
    public Image createEventImage(final double RADIUS, final OpacityDistribution OPACITY_DISTRIBUTION) {
        Double radius = RADIUS < 1 ? 1 : RADIUS;
        if (eventImages.containsKey(OPACITY_DISTRIBUTION.name() + radius)) {
            return eventImages.get(OPACITY_DISTRIBUTION.name() + radius);
        }
        Stop[] stops = new Stop[11];
        for (int i = 0; i < 11; i++) {
            stops[i] = new Stop(i * 0.1, Color.rgb(255, 255, 255, OPACITY_DISTRIBUTION.distribution[i]));
        }

        int size = (int) (radius * 2);
        WritableImage raster        = new WritableImage(size, size);
        PixelWriter   pixelWriter   = raster.getPixelWriter();
        double        maxDistFactor = 1 / radius;
        Color pixelColor;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double deltaX   = radius - x;
                double deltaY   = radius - y;
                double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
                double fraction = maxDistFactor * distance;
                for (int i = 0; i < 10; i++) {
                    if (Double.compare(fraction, stops[i].getOffset()) >= 0 && Double.compare(fraction, stops[i + 1].getOffset()) <= 0) {
                        pixelColor = (Color) Interpolator.LINEAR.interpolate(stops[i].getColor(), stops[i + 1].getColor(), (fraction - stops[i].getOffset()) / 0.1);
                        pixelWriter.setColor(x, y, pixelColor);
                        break;
                    }
                }
            }
        }
        eventImages.put(OPACITY_DISTRIBUTION.name() + radius, raster);
        return raster;
    }

    /**
     * Updates each event in the monochrome map to the given opacity gradient
     * which could be useful to reduce oversmoothing
     * @param OPACITY_GRADIENT
     */
    public void updateMonochromeMap(final OpacityDistribution OPACITY_GRADIENT) {
        ctx.clearRect(0, 0, monochrome.getWidth(), monochrome.getHeight());
        eventList.forEach(event -> {
            event.setOpacityDistribution(OPACITY_GRADIENT);
            ctx.drawImage(createEventImage(event.getRadius(), event.getOpacityDistribution()), event.getX() - event.getRadius() * 0.5, event.getY() - event.getRadius() * 0.5);
        });
        updateHeatMap();
    }

    /**
     * Recreates the heatmap based on the current monochrome map.
     * Using this approach makes it easy to change the used color
     * mapping.
     */
    private void updateHeatMap() {
        monochrome.snapshot(SNAPSHOT_PARAMETERS, monochromeImage);
        heatMap = new WritableImage(monochromeImage.widthProperty().intValue(), monochromeImage.heightProperty().intValue());
        Color       colorFromMonoChromeImage;
        double      brightness;
        Color       mappedColor;
        PixelWriter pixelWriter = heatMap.getPixelWriter();
        PixelReader pixelReader = monochromeImage.getPixelReader();
        int width  = (int) monochromeImage.getWidth();
        int height = (int) monochromeImage.getHeight();
        for (int y = 0 ; y < height ; y++) {
            for (int x = 0 ; x < width ; x++) {
                colorFromMonoChromeImage = pixelReader.getColor(x, y);
                brightness = colorFromMonoChromeImage.getOpacity();
                mappedColor = getColorAt(mappingGradient, brightness);
                if (fadeColors) {
                    pixelWriter.setColor(x, y, Color.color(mappedColor.getRed(), mappedColor.getGreen(), mappedColor.getBlue(), brightness));
                } else {
                    pixelWriter.setColor(x, y, mappedColor);
                }
            }
        }
        setImage(heatMap);
    }

    /**
     * Calculates the color in a linear gradient at the given fraction
     * @param GRADIENT
     * @param FRACTION
     * @return the color in a linear gradient at the given fraction
     */
    private Color getColorAt(final LinearGradient GRADIENT, final double FRACTION) {
        List<Stop> stops     = GRADIENT.getStops();
        double     fraction  = FRACTION < 0f ? 0f : (FRACTION > 1 ? 1 : FRACTION);
        Stop       lowerStop = new Stop(0.0, stops.get(0).getColor());
        Stop       upperStop = new Stop(1.0, stops.get(stops.size() - 1).getColor());

        for (Stop stop : stops) {
            double currentFraction = stop.getOffset();
            if (Double.compare(currentFraction, fraction) == 0) {
                return stop.getColor();
            } else if (Double.compare(currentFraction, fraction) < 0) {
                lowerStop = new Stop(currentFraction, stop.getColor());
            } else {
                upperStop = new Stop(currentFraction, stop.getColor());
                break;
            }
        }

        double interpolationFraction = (fraction - lowerStop.getOffset()) / (upperStop.getOffset() - lowerStop.getOffset());
        return (Color) Interpolator.LINEAR.interpolate(lowerStop.getColor(), upperStop.getColor(), interpolationFraction);
    }

    private void resize() {
        double width  = getFitWidth();
        double height = getFitHeight();

        monochrome.setWidth(width);
        monochrome.setHeight(height);

        if (width > 0 && height > 0) {
            monochromeImage = new WritableImage(monochrome.widthProperty().intValue(), monochrome.heightProperty().intValue());
            updateHeatMap();
        }
    }
}
