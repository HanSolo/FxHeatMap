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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


/**
 * User: hansolo
 * Date: 29.06.14
 * Time: 13:51
 */
public class SimpleHeatMapDemo extends Application {    
    private SimpleHeatMap                  heatMap;
    private StackPane                      pane;
    private Slider                         sliderOpacity;
    private Button                         button1;
    private Button                         button2;
    private ChoiceBox<ColorMapping>        choiceBoxMapping;
    private TextField                      field1;
    private CheckBox                       checkBoxFadeColors;
    private Slider                         sliderRadius;
    private ChoiceBox<OpacityDistribution> choiceBoxOpacityDistribution;
    private Button                         clearHeatMap;
    private EventHandler<ActionEvent>      handler;


    // ******************** Initialization ************************************
    @Override public void init() {
        pane                         = new StackPane();
        heatMap                      = new SimpleHeatMap(400, 400, ColorMapping.BLACK_WHITE);
        sliderOpacity                = new Slider();
        button1                      = new Button("Button 1");
        button2                      = new Button("Button 2");
        choiceBoxMapping             = new ChoiceBox<>();
        field1                       = new TextField();
        checkBoxFadeColors           = new CheckBox("Fade colors");
        sliderRadius                 = new Slider();
        choiceBoxOpacityDistribution = new ChoiceBox<>();
        clearHeatMap                 = new Button("Clear");
        handler                      = EVENT -> {
            final Object SRC = EVENT.getSource();
            if (SRC.equals(choiceBoxMapping)) {
                heatMap.setColorMapping(ColorMapping.valueOf(choiceBoxMapping.getSelectionModel().getSelectedItem().toString()));
            } else if (SRC.equals(choiceBoxOpacityDistribution)) {
                heatMap.setOpacityDistribution(OpacityDistribution.valueOf(choiceBoxOpacityDistribution.getSelectionModel().getSelectedItem().toString()));              
            } else if (SRC.equals(checkBoxFadeColors)) {
                heatMap.setFadeColors(checkBoxFadeColors.isSelected());
            } else if (SRC.equals(clearHeatMap)) {
                heatMap.clearHeatMap();
            }
        };
        registerListeners();
    }


    // ******************** Start *********************************************
    @Override public void start(Stage stage) {
        sliderOpacity.setMin(0);
        sliderOpacity.setMax(1);
        sliderOpacity.setValue(heatMap.getHeatMapOpacity());
        sliderOpacity.valueChangingProperty().addListener((observableValue, aBoolean, aBoolean2) -> heatMap.setHeatMapOpacity(sliderOpacity.getValue()));

        choiceBoxMapping.getItems().setAll(ColorMapping.BLACK_WHITE, ColorMapping.WHITE_BLACK);
        choiceBoxMapping.getSelectionModel().select(heatMap.getColorMapping());
        choiceBoxMapping.addEventHandler(ActionEvent.ACTION, handler);

        choiceBoxOpacityDistribution.getItems().setAll(OpacityDistribution.values());
        choiceBoxOpacityDistribution.getSelectionModel().select(heatMap.getOpacityDistribution());
        choiceBoxOpacityDistribution.addEventHandler(ActionEvent.ACTION, handler);

        checkBoxFadeColors.setSelected(heatMap.isFadeColors());
        checkBoxFadeColors.setOnAction(handler);

        sliderRadius.setMin(10);
        sliderRadius.setMax(50);
        sliderRadius.setValue(heatMap.getEventRadius());
        sliderRadius.valueChangingProperty().addListener((observableValue, aBoolean, aBoolean2) -> heatMap.setEventRadius(sliderRadius.getValue()));

        clearHeatMap.setOnAction(handler);

        VBox layout = new VBox();
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);
        layout.getChildren().setAll(button1,
                                    field1,
                                    button2,
                                    sliderOpacity,
                                    choiceBoxMapping,
                                    checkBoxFadeColors,
                                    sliderRadius,
                                    choiceBoxOpacityDistribution,
                                    clearHeatMap);

        pane.getChildren().setAll(layout, heatMap.getHeatMapImage());
        Scene scene = new Scene(pane, 400, 400, Color.GRAY);

        stage.setTitle("JavaFX SimpleHeatMap Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
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
