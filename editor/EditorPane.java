package editor;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import editor.global.Params;
import javafx.scene.shape.Rectangle;

import java.util.LinkedList;
import java.util.Map;

class EditorPane extends Pane {

    LinkedList<Line> lines;
    LinkedList<Line> columns;

    private double offsetX;
    private double offsetY;
    private double maxWidth;
    private double maxHeight;

    private double pressedX;
    private double pressedY;
    private Rectangle selectByDraggingRectangle;

    EditorPane() {
        lines = new LinkedList<>();
        columns = new LinkedList<>();

        this.setOnMouseClicked(event -> {
            if(!event.isStillSincePress())
                return;

            this.selectByDraggingRectangle.setVisible(false);

            int column = getColumn(event.getX());
            int line = getLine(event.getY());

            double cx = (column + 0.5) * Params.DEFAULT_CELLS_WIDTH;
            double cy = (line + 0.5) * Params.DEFAULT_CELLS_WIDTH;

            AliveCircle circle = new AliveCircle(cx, cy, (double)(Params.DEFAULT_CELLS_WIDTH / 2 - 2), line, column);
            this.getChildren().add(circle);
        });

        this.setOnMouseMoved(event -> {
            int column = (int)Math.floor(event.getX() / Params.DEFAULT_CELLS_WIDTH);
            int line = (int)Math.floor(event.getY() / Params.DEFAULT_CELLS_WIDTH);

            ((Editor)this.getParent()).setStatusBarCoordinates(line, column);
        });

        this.setOnMouseExited(event -> {
            ((Editor)this.getParent()).clearStatusBarText();
        });

        this.setOnMouseDragged(event -> {
            if(event.getButton() != MouseButton.PRIMARY)
                return;

            selectByDraggingRectangle.setX(Math.min(event.getX(), pressedX));
            selectByDraggingRectangle.setY(Math.min(event.getY(), pressedY));
            selectByDraggingRectangle.setWidth(Math.abs(event.getX() - pressedX));
            selectByDraggingRectangle.setHeight(Math.abs(event.getY() - pressedY));

            event.consume();
        });

        this.setOnMousePressed(event -> {
            if(event.getButton() != MouseButton.PRIMARY)
                return;

            pressedX = event.getX();
            pressedY = event.getY();

            selectByDraggingRectangle.setWidth(0);
            selectByDraggingRectangle.setHeight(0);
            selectByDraggingRectangle.setVisible(true);

            event.consume();
        });

        this.setOnMouseReleased(event -> {

            if(event.getButton() != MouseButton.PRIMARY)
                return;

            int c1 = getColumn(selectByDraggingRectangle.getX());
            int l1 = getLine(selectByDraggingRectangle.getY());
            int c2 = getColumn(selectByDraggingRectangle.getX() + selectByDraggingRectangle.getWidth()) + 1;
            int l2 = getLine(selectByDraggingRectangle.getY() + selectByDraggingRectangle.getHeight()) + 1;

            double x = getX(c1);
            double y = getY(l1);
            double w = getX(c2) - x;
            double h = getY(l2) - y;

            selectByDraggingRectangle.setX(x);
            selectByDraggingRectangle.setY(y);
            selectByDraggingRectangle.setWidth(w);
            selectByDraggingRectangle.setHeight(h);

            event.consume();
        });

        this.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.getWidth();
            double height = newVal.getHeight();
            updateWidthAndHeight(width, height);
        });

        selectByDraggingRectangle = new Rectangle(0, 0, 0, 0);
        selectByDraggingRectangle.setFill(new Color(0.7, 0.7, 0.7, 0.6));
        selectByDraggingRectangle.setVisible(false);
        this.getChildren().add(selectByDraggingRectangle);

    }

    double getX(int line){
        return line * Params.DEFAULT_CELLS_WIDTH;
    }

    int getColumn(double x){
        return (int)Math.floor(x / Params.DEFAULT_CELLS_WIDTH);
    }

    double getY(int column){
        return column * Params.DEFAULT_CELLS_WIDTH;
    }

    int getLine(double y){
        return (int)Math.floor(y / Params.DEFAULT_CELLS_WIDTH);
    }

    boolean[][] getCells(boolean infiniteSize, boolean allCells,
                         int offsetLine, int offsetColumn,
                         int viewerNbCellsPerLine, int viewerNbCellsPerColumn){

        if(infiniteSize || allCells){
            int minLine = Integer.MAX_VALUE;
            int minColumn = Integer.MAX_VALUE;
            int maxLine = Integer.MIN_VALUE;
            int maxColumn = Integer.MIN_VALUE;

            for(Node child : this.getChildren()) {
                if (child instanceof AliveCircle) {
                    AliveCircle circle = (AliveCircle) child;
                    minLine = Math.min(minLine, circle.line);
                    maxLine = Math.max(maxLine, circle.line);
                    minColumn = Math.min(minColumn, circle.column);
                    maxColumn = Math.max(maxColumn, circle.column);
                }
            }

            offsetLine = minLine;
            offsetColumn = minColumn;
            viewerNbCellsPerColumn = maxLine - minLine + 1;
            viewerNbCellsPerLine = maxColumn - minColumn + 1;
        }


        boolean[][] cells = new boolean[viewerNbCellsPerColumn][viewerNbCellsPerLine];


        for(Node child : this.getChildren()){
            if(child instanceof AliveCircle){
                AliveCircle circle = (AliveCircle)child;
                if(circle.line >= offsetLine && circle.line < offsetLine + viewerNbCellsPerColumn
                && circle.column >= offsetColumn && circle.column < offsetColumn + viewerNbCellsPerLine)
                    cells[circle.line - offsetLine][circle.column - offsetColumn] = true;
            }
        }

        return cells;
    }

    void translate(double dx, double dy){
        this.setTranslateX(this.getTranslateX() + dx);
        this.setTranslateY(this.getTranslateY() + dy);

        offsetX -= dx;
        offsetY -= dy;

        double width = Math.max(Math.abs(-2 * offsetX - this.getWidth()), Math.abs(-2 * offsetX + this.getWidth()));
        double height = Math.max(Math.abs(-2 * offsetY - this.getHeight()), Math.abs(-2 * offsetY + this.getHeight()));

        updateWidthAndHeight(width, height);
    }

    private void updateWidthAndHeight(double width, double height){
        if(width > maxWidth || height > maxHeight) {
            maxWidth = Math.max(maxWidth, width);
            maxHeight = Math.max(maxHeight, height);
            this.checkLinesAndColumns();
        }
    }

    private void checkLinesAndColumns() {
        ObservableList<Node> children = this.getChildren();

        int nbLines = (int) (maxHeight / (Params.DEFAULT_CELLS_WIDTH)) + 4;
        int nbColumns = (int) (maxWidth / (Params.DEFAULT_CELLS_WIDTH)) + 4;

        double h, w;

        for (Line line : lines) {
            line.setStartX(maxWidth);
            line.setEndX(-maxWidth);
        }

        for (int i = lines.size() / 2; i < nbLines; i++) {
            h = Params.DEFAULT_CELLS_WIDTH * i;
            Line linePos = new Line(maxWidth, -h, -maxWidth, -h);
            lines.add(linePos);
            children.add(linePos);

            if (h != 0) {
                Line lineNeg = new Line(maxWidth, h, -maxWidth, h);
                lines.add(lineNeg);
                children.add(lineNeg);
            }

        }


        for (Line column : columns) {
            column.setStartY(maxHeight);
            column.setEndY(-maxHeight);
        }

        for (int i = columns.size() / 2; i < nbColumns; i++) {
            w = Params.DEFAULT_CELLS_WIDTH * i;
            Line columnPos = new Line(w, maxHeight, w, -maxHeight);
            columns.add(columnPos);
            children.add(columnPos);

            if(w != 0) {
                Line columnNeg = new Line(-w, maxHeight, -w, -maxHeight);
                columns.add(columnNeg);
                children.add(columnNeg);
            }
        }

    }
}
