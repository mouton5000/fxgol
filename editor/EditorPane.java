package editor;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import editor.global.Params;

import java.util.Map;

class EditorPane extends Pane {

    Map<Integer, Map<Integer, AliveCircle>> alives;

    EditorPane() {
        for(int i = 0; i < Params.DEFAULT_NB_CELLS_PER_LINE; i++) {
            Line col = new Line(i * Params.DEFAULT_CELLS_WIDTH, 0, i * Params.DEFAULT_CELLS_WIDTH, Params.DEFAULT_CELLS_WIDTH * Params.DEFAULT_NB_CELLS_PER_LINE);
            Line lin = new Line( 0, i * Params.DEFAULT_CELLS_WIDTH, Params.DEFAULT_CELLS_WIDTH * Params.DEFAULT_NB_CELLS_PER_LINE,i * Params.DEFAULT_CELLS_WIDTH);
            this.getChildren().addAll(lin, col);
        }

        this.setOnMouseClicked(event -> {
            int column = (int)(event.getX() / Params.DEFAULT_CELLS_WIDTH);
            int line = (int)(event.getY() / Params.DEFAULT_CELLS_WIDTH);

            double cx = (column + 0.5) * Params.DEFAULT_CELLS_WIDTH;
            double cy = (line + 0.5) * Params.DEFAULT_CELLS_WIDTH;

            AliveCircle circle = new AliveCircle(cx, cy, (double)(Params.DEFAULT_CELLS_WIDTH / 2 - 2), line, column);
            this.getChildren().add(circle);
        });

        this.setOnMouseMoved(event -> {
            int column = (int)(event.getX() / Params.DEFAULT_CELLS_WIDTH);
            int line = (int)(event.getY() / Params.DEFAULT_CELLS_WIDTH);

            ((Editor)this.getParent()).setStatusBarCoordinates(line, column);
        });

        this.setOnMouseExited(event -> {
            ((Editor)this.getParent()).clearStatusBarText();
        });

    }

    boolean[][] getCells(int offsetLine, int offsetColumn, int viewerNbCellsPerLine){
        boolean[][] cells = new boolean[viewerNbCellsPerLine][viewerNbCellsPerLine];

        for(Node child : this.getChildren()){
            if(child instanceof AliveCircle){
                AliveCircle circle = (AliveCircle)child;
                if(circle.line >= offsetLine && circle.line < offsetLine + viewerNbCellsPerLine
                && circle.column >= offsetColumn && circle.column < offsetColumn + viewerNbCellsPerLine)
                    cells[circle.line - offsetLine][circle.column - offsetColumn] = true;
            }
        }

        return cells;
    }

}
