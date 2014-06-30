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

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;


/**
 * Created by
 * User: hansolo
 * Date: 01.01.13
 * Time: 15:50
 */
public class Helper {
    public static Point2D latLongToPixel(final Dimension2D MAP_DIMENSION,
                                         final Point2D UPPER_LEFT,
                                         final Point2D LOWER_RIGHT,
                                         final Point2D LOCATION) {
        final double LATITUDE   = LOCATION.getX();
        final double LONGITUDE  = LOCATION.getY();
        final double MAP_WIDTH  = MAP_DIMENSION.getWidth();
        final double MAP_HEIGHT = MAP_DIMENSION.getHeight();

        final double WORLD_MAP_WIDTH = ((MAP_WIDTH / (LOWER_RIGHT.getY() - UPPER_LEFT.getY())) * 360) / (2 * Math.PI);
        final double MAP_OFFSET_Y    = (WORLD_MAP_WIDTH / 2 * Math.log10((1 + Math.sin(Math.toRadians(LOWER_RIGHT.getX()))) / (1 - Math.sin(Math.toRadians(LOWER_RIGHT.getX())))));

        final double X = (LONGITUDE - UPPER_LEFT.getY()) * (MAP_WIDTH / (LOWER_RIGHT.getY() - UPPER_LEFT.getY()));
        final double Y = MAP_HEIGHT - ((WORLD_MAP_WIDTH / 2 * Math.log10((1 + Math.sin(Math.toRadians(LATITUDE))) / (1 - Math.sin(Math.toRadians(LATITUDE))))) - MAP_OFFSET_Y);

        return new Point2D(X, Y);
    }

    public static double getSignedDegrees(final int DEGREES, final int MINUTES, final int SECONDS) {
        return DEGREES + (MINUTES / 60.0) + (SECONDS / 3600.0);
    }

    public static double getSignedDegrees(final int DEGREES, final double MINUTES) {
        return DEGREES + (MINUTES / 60.0);
    }

    public static String getDMSCompass(final double LATITUDE, final double LONGITUDE) {
        final StringBuilder DIRECTION = new StringBuilder();
        DIRECTION.append(Math.abs(LATITUDE)).append(" ").append(getLatitudeDirection(LATITUDE));
        DIRECTION.append(", ");
        DIRECTION.append(Math.abs(LONGITUDE)).append(" ").append(getLongitudeDirection(LONGITUDE));
        return DIRECTION.toString();
    }

    public static String getLatitudeDirection(final double LATITUDE) {
        final String DIRECTION;
        if (LATITUDE > 0) {
            DIRECTION = "N";
        } else {
            DIRECTION = "S";
        }
        return DIRECTION;
    }

    public static String getLongitudeDirection(final double LONGITUDE) {
        final String DIRECTION;
        if (LONGITUDE > 0) {
            DIRECTION = "E";
        } else {
            DIRECTION = "W";
        }
        return DIRECTION;
    }
}
