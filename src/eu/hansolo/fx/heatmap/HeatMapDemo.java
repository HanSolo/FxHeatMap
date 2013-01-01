package eu.hansolo.fx.heatmap;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
 * Created by
 * User: hansolo
 * Date: 27.12.12
 * Time: 05:58
 */
public class HeatMapDemo extends Application {
    private HeatMap                   heatMap;
    private StackPane                 pane;
    private Slider                    sliderOpacity;
    private Button                    button1;
    private Button                    button2;
    private ChoiceBox                 choiceBoxMapping;
    private TextField                 field1;
    private CheckBox                  checkBoxFadeColors;
    private Slider                    sliderRadius;
    private ChoiceBox                 choiceBoxOpacityDistribution;
    private EventHandler<ActionEvent> handler;


    // ******************** Initialization ************************************
    @Override public void init() {
        pane                         = new StackPane();
        heatMap                      = new HeatMap(400, 400, ColorMapping.BLUE_CYAN_GREEN_YELLOW_RED);
        sliderOpacity                = new Slider();
        button1                      = new Button("Button 1");
        button2                      = new Button("Button 2");
        choiceBoxMapping             = new ChoiceBox();
        field1                       = new TextField();
        checkBoxFadeColors           = new CheckBox("Fade colors");
        sliderRadius                 = new Slider();
        choiceBoxOpacityDistribution = new ChoiceBox();
        handler                      = new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent EVENT) {
                final Object SRC = EVENT.getSource();
                if (SRC.equals(choiceBoxMapping)) {
                    heatMap.setColorMapping(ColorMapping.valueOf(choiceBoxMapping.getSelectionModel().getSelectedItem().toString()));
                } else if (SRC.equals(choiceBoxOpacityDistribution)) {
                    heatMap.setOpacityDistribution(OpacityDistribution.valueOf(choiceBoxOpacityDistribution.getSelectionModel().getSelectedItem().toString()));
                    heatMap.updateMonochromeMap(OpacityDistribution.valueOf(choiceBoxOpacityDistribution.getSelectionModel().getSelectedItem().toString()));
                } else if (SRC.equals(checkBoxFadeColors)) {
                    heatMap.setFadeColors(checkBoxFadeColors.isSelected());
                }
            }
        };
        registerListeners();
    }


    // ******************** Start *********************************************
    @Override public void start(Stage stage) {
        sliderOpacity.setMin(0);
        sliderOpacity.setMax(1);
        sliderOpacity.setValue(heatMap.getHeatMapOpacity());
        sliderOpacity.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                heatMap.setHeatMapOpacity(sliderOpacity.getValue());
            }
        });

        choiceBoxMapping.getItems().setAll(ColorMapping.values());
        choiceBoxMapping.getSelectionModel().select(heatMap.getColorMapping());
        choiceBoxMapping.addEventHandler(ActionEvent.ACTION, handler);

        choiceBoxOpacityDistribution.getItems().setAll(OpacityDistribution.values());
        choiceBoxOpacityDistribution.getSelectionModel().select(heatMap.getOpacityDistribution());
        choiceBoxOpacityDistribution.addEventHandler(ActionEvent.ACTION, handler);

        checkBoxFadeColors.setSelected(heatMap.isFadeColors());
        checkBoxFadeColors.addEventHandler(ActionEvent.ACTION, handler);

        sliderRadius.setMin(10);
        sliderRadius.setMax(50);
        sliderRadius.setValue(heatMap.getEventRadius());
        sliderRadius.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                heatMap.setEventRadius(sliderRadius.getValue());
            }
        });

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
                                    choiceBoxOpacityDistribution);

        pane.getChildren().setAll(layout, heatMap.getHeatMapImage());
        Scene scene = new Scene(pane, 400, 400, Color.GRAY);

        stage.setTitle("JavaFX HeatMap Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


    // ******************** Methods *******************************************
    private void registerListeners() {
        pane.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent EVENT) {
                double x = EVENT.getX();
                double y = EVENT.getY();
                if (x < heatMap.getEventRadius()) x = heatMap.getEventRadius();
                if (x > pane.getWidth() - heatMap.getEventRadius()) x = pane.getWidth() - heatMap.getEventRadius();
                if (y < heatMap.getEventRadius()) y = heatMap.getEventRadius();
                if (y > pane.getHeight() - heatMap.getEventRadius()) y = pane.getHeight() - heatMap.getEventRadius();

                heatMap.addEvent(x, y);
            }
        });
        pane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> ov, Number oldWidth, Number newWidth) {
                heatMap.setSize(newWidth.doubleValue(), pane.getHeight());
            }
        });
        pane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> ov, Number oldHeight, Number newHeight) {
                heatMap.setSize(pane.getWidth(), newHeight.doubleValue());
            }
        });
    }
}