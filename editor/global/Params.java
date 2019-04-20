package editor.global;

import javafx.scene.paint.Color;

public class Params {
    public static final int DEFAULT_CELLS_WIDTH = 50;
    public static final int DEFAULT_CIRCLE_RADIUS = Params.DEFAULT_CELLS_WIDTH / 2 - 2;
    public static final int DEFAULT_NB_CELLS_PER_LINE = 10;
    public static final int DEFAULT_NB_CELLS_PER_COLUMN = 10;
    public static final double DEFAULT_FPS = -1;

    public static final int STATUSBAR_PREF_HEIGHT = 30;
    public static final int EDITOR_LEFT_MENU_PREF_WIDTH = 200;
    public static final Color SELECTION_RECTANGLE_COLOR = new Color(0.3, 0.3, 0.3, 0.6);
    public static final Color SELECTION_COLOR = new Color(0.5, 0.5, 0.5, 1);

    public static final double STEPZOOM = 0.1;
    public static final double MINZOOM = 0.1;
    public static final double MAXZOOM = 1.7;

    public static double getX(int column){
        return column * Params.DEFAULT_CELLS_WIDTH;
    }

    public static int getColumn(double x){
        return (int)Math.floor(x / Params.DEFAULT_CELLS_WIDTH);
    }

    public static double getY(int line){
        return line * Params.DEFAULT_CELLS_WIDTH;
    }

    public static int getLine(double y){
        return (int)Math.floor(y / Params.DEFAULT_CELLS_WIDTH);
    }
}
