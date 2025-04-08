package common;

import java.awt.*;

public class Constants {
    public static final Dimension SCREEN_SIZE = new Dimension(1000, 600);
    public static final float[] PROJECTION_MATRIX = new float[] {
            2f/SCREEN_SIZE.width, 0,                       0,  -1,
            0,                    2f/-SCREEN_SIZE.height,  0,   1,
            0,                    0,                      -1,   0,
            0,                    0,                       0,   1
    };
}
