package editor;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import editor.global.Params;

import java.util.HashMap;
import java.util.Map;

public class EditorPane extends Pane {

    Map<Integer, Map<Integer, AliveCircle>> alives;

    public EditorPane() {
        for(int i = 0; i < Params.WIDTH; i++) {
            Line col = new Line(i * Params.SIZE, 0, i * Params.SIZE, Params.SIZE * Params.WIDTH);
            Line lin = new Line( 0, i * Params.SIZE, Params.SIZE * Params.WIDTH,i * Params.SIZE);
            this.getChildren().addAll(lin, col);
        }

        this.setOnMouseClicked(mouseEvent -> {
            System.out.println(this);
            int x = (int)(mouseEvent.getX() / Params.SIZE);
            int y = (int)(mouseEvent.getY() / Params.SIZE);

            double cx = (x + 0.5) * Params.SIZE;
            double cy = (y + 0.5) * Params.SIZE;

            AliveCircle circle = new AliveCircle(cx, cy, (double)(Params.SIZE / 2 - 2));
            this.getChildren().add(circle);
        });


    }

//    public addCell(int i, int j){
//
//    }
}
