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
import javafx.geometry.Point2D;
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

import java.util.List;


/**
 * Created by
 * User: hansolo
 * Date: 31.12.12
 * Time: 07:49
 */
public class SimpleHeatMap {
    private static final SnapshotParameters SNAPSHOT_PARAMETERS = new SnapshotParameters();
    private ColorMapping        colorMapping;
    private LinearGradient      mappingGradient;
    private boolean             fadeColors;
    private double              radius;
    private OpacityDistribution opacityDistribution;
    private Image               eventImage;
    private Canvas              monochromeCanvas;
    private GraphicsContext     ctx;
    private WritableImage       monochromeImage;
    private WritableImage       heatMap;
    private ImageView           heatMapView;


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
        SNAPSHOT_PARAMETERS.setFill(Color.TRANSPARENT);
        colorMapping        = COLOR_MAPPING;
        mappingGradient     = colorMapping.mapping;
        fadeColors          = FADE_COLORS;
        radius              = EVENT_RADIUS;
        opacityDistribution = OpacityDistribution.CUSTOM;
        eventImage          = createEventImage(radius, opacityDistribution);
        monochromeCanvas    = new Canvas(WIDTH, HEIGHT);
        ctx                 = monochromeCanvas.getGraphicsContext2D();
        monochromeImage     = new WritableImage((int) WIDTH, (int) HEIGHT);
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

    public void addEvents(final Point2D... EVENTS) {
        for (Point2D event : EVENTS) {
            ctx.drawImage(eventImage, event.getX() - radius, event.getY() - radius);
        }
        updateHeatMap();
    }
    public void addEvents(final List<Point2D> EVENTS) {
        for (Point2D event : EVENTS) {
            ctx.drawImage(eventImage, event.getX() - radius, event.getY() - radius);
        }
        updateHeatMap();
    }

    public void clearHeatMap() {
        ctx.clearRect(0, 0, monochromeCanvas.getWidth(), monochromeCanvas.getHeight());
        monochromeImage = new WritableImage(monochromeCanvas.widthProperty().intValue(), monochromeCanvas.heightProperty().intValue());
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
        eventImage = createEventImage(radius, opacityDistribution);
    }

    public OpacityDistribution getOpacityDistribution() {
        return opacityDistribution;
    }
    public void setOpacityDistribution(final OpacityDistribution OPACITY_DISTRIBUTION) {
        opacityDistribution = OPACITY_DISTRIBUTION;
        eventImage          = createEventImage(radius, opacityDistribution);
    }

    public void setSize(final double WIDTH, final double HEIGHT) {
        monochromeCanvas.setWidth(WIDTH);
        monochromeCanvas.setHeight(HEIGHT);
        if (WIDTH > 0 && HEIGHT > 0) {
            monochromeImage = new WritableImage(monochromeCanvas.widthProperty().intValue(), monochromeCanvas.heightProperty().intValue());
            updateHeatMap();
        }
    }

    public Image createEventImage(final double RADIUS, final OpacityDistribution OPACITY_DISTRIBUTION) {
        radius = RADIUS < 1 ? 1 : RADIUS;
        Stop[] stops = new Stop[11];
        for (int i = 0 ; i < 11 ; i++) {
            stops[i] = new Stop(i * 0.1, Color.rgb(255, 255, 255, OPACITY_DISTRIBUTION.distribution[i]));
        }
        int           size          = (int) (radius * 2);
        WritableImage raster        = new WritableImage(size, size);
        PixelWriter   pixelWriter   = raster.getPixelWriter();
        double        maxDistFactor = 1 / radius;
        Color         pixelColor;
        for (int y = 0 ; y < size ; y++) {
            for (int x = 0 ; x < size ; x++) {
                double distanceX = radius - x;
                double distanceY = radius - y;
                double distance  = Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));
                double fraction  = maxDistFactor * distance;
                for (int i = 0 ; i < 10 ; i++) {
                    if (Double.compare(fraction, stops[i].getOffset()) >= 0 && Double.compare(fraction, stops[i + 1].getOffset()) <= 0) {
                        pixelColor = (Color) Interpolator.LINEAR.interpolate(stops[i].getColor(), stops[i + 1].getColor(), (fraction - stops[i].getOffset()) / 0.1);
                        pixelWriter.setColor(x, y, pixelColor);
                        break;
                    }
                }
            }
        }
        return raster;
    }

    private void updateHeatMap() {
        monochromeCanvas.snapshot(SNAPSHOT_PARAMETERS, monochromeImage);
        heatMap = new WritableImage(monochromeImage.widthProperty().intValue(), monochromeImage.heightProperty().intValue());
        PixelWriter pixelWriter = heatMap.getPixelWriter();
        PixelReader pixelReader = monochromeImage.getPixelReader();
        Color colorFromMonoChromeImage;
        double brightness;
        Color mappedColor;
        for (int y = 0 ; y < monochromeImage.getHeight() ; y++) {
            for (int x = 0 ; x < monochromeImage.getWidth(); x++) {
                colorFromMonoChromeImage = pixelReader.getColor(x, y);
                //brightness = computeLuminance(colorFromMonoChromeImage.getRed(), colorFromMonoChromeImage.getGreen(), colorFromMonoChromeImage.getBlue());
                //brightness = computeBrightness(colorFromMonoChromeImage.getRed(), colorFromMonoChromeImage.getGreen(), colorFromMonoChromeImage.getBlue());
                brightness = computeBrightnessFast(colorFromMonoChromeImage.getRed(), colorFromMonoChromeImage.getGreen(), colorFromMonoChromeImage.getBlue());
                mappedColor = getColorAt(mappingGradient, brightness);
                if (fadeColors) {
                    //pixelWriter.setColor(x, y, Color.color(mappedColor.getRed(), mappedColor.getGreen(), mappedColor.getBlue(), brightness));
                    pixelWriter.setColor(x, y, Color.color(mappedColor.getRed(), mappedColor.getGreen(), mappedColor.getBlue(), colorFromMonoChromeImage.getOpacity()));
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
        List<Stop> stops = GRADIENT.getStops();
        Stop lowerLimit  = new Stop(0.0, stops.get(0).getColor());
        Stop upperLimit  = new Stop(1.0, stops.get(stops.size() - 1).getColor());

        for (Stop stop : stops) {
            if (Double.compare(stop.getOffset(), fraction) < 0) {
                lowerLimit = new Stop(stop.getOffset(), stop.getColor());
            } else if (Double.compare(stop.getOffset(), fraction) == 0) {
                return stop.getColor();
            } else {
                upperLimit = new Stop(stop.getOffset(), stop.getColor());
            }
        }

        double interpolationFraction = (fraction - lowerLimit.getOffset()) / (upperLimit.getOffset() - lowerLimit.getOffset());
        return (Color) Interpolator.LINEAR.interpolate(lowerLimit.getColor(), upperLimit.getColor(), interpolationFraction);
    }
}
