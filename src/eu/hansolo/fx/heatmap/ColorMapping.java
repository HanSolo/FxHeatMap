package eu.hansolo.fx.heatmap;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.Stop;


/**
 * Created by
 * User: hansolo
 * Date: 01.01.13
 * Time: 08:13
 */
public enum ColorMapping {
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
