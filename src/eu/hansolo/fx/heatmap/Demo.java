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

import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


/**
 * Created by
 * User: hansolo
 * Date: 01.01.13
 * Time: 08:02
 */
public class Demo extends Application {
    private SimpleHeatMap heatMap;
    private StackPane     pane;
    private Point2D[]     events;


    // ******************** Initialization ************************************
    @Override public void init() {
        pane    = new StackPane();
        heatMap = new SimpleHeatMap(400, 400, ColorMapping.LIME_YELLOW_RED, 40);
        heatMap.setOpacityDistribution(OpacityDistribution.LINEAR);
        heatMap.setHeatMapOpacity(1);
        events  = new Point2D[] {
            new Point2D(110, 238),
            new Point2D(120, 144),
            new Point2D(207, 119),
            new Point2D(315, 348),
            new Point2D(264, 226),
            new Point2D(280, 159),
            new Point2D(240, 186),
            new Point2D(228, 170),
            new Point2D(234, 160),
            new Point2D(214, 170),
            new Point2D(200, 200),
        };
    }


    // ******************** Start *********************************************
    @Override public void start(Stage stage) {
        pane.getChildren().setAll(heatMap.getHeatMapImage());
        Scene scene = new Scene(pane, 400, 400, Color.rgb(0, 0, 0, 0.5));

        stage.setTitle("JavaFX HeatMap Demo");
        stage.setScene(scene);
        stage.show();
        heatMap.addEvents(events);
    }

    public static void main(String[] args) {
        Dimension2D mapDimension = new Dimension2D(1500, 1577);
        Point2D     upperLeft    = new Point2D(0, 9.8);
        Point2D     lowerRight   = new Point2D(53.45, 10.2);
        Point2D     location     = new Point2D(53.7, 9.95);

        System.out.println(Helper.latLongToPixel(mapDimension, upperLeft, lowerRight, location));

        launch(args);
    }


    // ******************** Methods *******************************************
    private void registerListeners() {
        pane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            double x = event.getX();
            double y = event.getY();
            if (x < heatMap.getEventRadius()) x = heatMap.getEventRadius();
            if (x > pane.getWidth() - heatMap.getEventRadius()) x = pane.getWidth() - heatMap.getEventRadius();
            if (y < heatMap.getEventRadius()) y = heatMap.getEventRadius();
            if (y > pane.getHeight() - heatMap.getEventRadius()) y = pane.getHeight() - heatMap.getEventRadius();

            heatMap.addEvent(x, y);
        });
        pane.widthProperty().addListener((ov, oldWidth, newWidth) -> heatMap.setSize(newWidth.doubleValue(), pane.getHeight()));
        pane.heightProperty().addListener((ov, oldHeight, newHeight) -> heatMap.setSize(pane.getWidth(), newHeight.doubleValue()));
    }
}
