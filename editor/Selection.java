package editor;

import editor.global.Params;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

class Selection extends Group {

    int offsetLine;
    int offsetColumn;

    Boolean[][] cells;

    private double dragX, dragY;

    Selection(){
        offsetLine = Integer.MAX_VALUE;
        offsetColumn = Integer.MAX_VALUE;

        this.setOnMouseClicked(Event::consume);
        this.setOnMousePressed(event -> {
            this.dragX = event.getX();
            this.dragY = event.getY();
            event.consume();
        });
        this.setOnMouseReleased(Event::consume);
        this.setOnMouseDragged(event -> {
            int dline = (int)((event.getY() - dragY) / Params.DEFAULT_CELLS_WIDTH);
            int dcolumn = (int)((event.getX() - dragX) / Params.DEFAULT_CELLS_WIDTH);
            if(dline != 0 || dcolumn != 0) {
                this.translate(dline, dcolumn);
            }
            event.consume();
        });
    }

    void translate(int dline, int dcolumn){
        offsetLine += dline;
        offsetColumn += dcolumn;
        this.setLayoutX(this.getLayoutX() + Params.DEFAULT_CELLS_WIDTH * dcolumn);
        this.setLayoutY(this.getLayoutY() + Params.DEFAULT_CELLS_WIDTH * dline);
    }

    void setOffset(int line, int column){
        offsetColumn = column;
        offsetLine = line;

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        for(Node child : this.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rect = (Rectangle)child;
                minX = Math.min(minX, rect.getX());
                minY = Math.min(minY, rect.getY());
            }
        }
        this.setLayoutX(Params.getX(column) - minX);
        this.setLayoutY(Params.getY(line) - minY);
    }

    void addRectangle(int line, int column, int nbLines, int nbColumns){
        Rectangle rectangle = new Rectangle(Params.getX(column) - this.getLayoutX(),
                Params.getY(line) - this.getLayoutY(),
                nbColumns * Params.DEFAULT_CELLS_WIDTH,
                nbLines * Params.DEFAULT_CELLS_WIDTH);
        rectangle.setFill(Params.SELECTION_COLOR);
        this.getChildren().add(rectangle);
        if(cells == null || line < offsetLine || column < offsetColumn
                || line + nbLines > offsetLine + cells.length
                || column + nbColumns > offsetColumn + cells[0].length){
            int newOffsetLine = Math.min(offsetLine, line);
            int newOffsetColumn = Math.min(offsetColumn, column);
            int newNbLines =
                    Math.max(((cells == null)?Integer.MIN_VALUE:offsetLine + cells.length), line + nbLines) - newOffsetLine;
            int newNbColumns =
                    Math.max(((cells == null)?Integer.MIN_VALUE:offsetColumn + cells[0].length), column + nbColumns) - newOffsetColumn;
            Boolean[][] newCells = new Boolean[newNbLines][newNbColumns];
            if(cells != null) {
                for (int l = 0; l < cells.length; l++) {
                    System.arraycopy(cells[l], 0,
                            newCells[offsetLine - newOffsetLine + l], offsetColumn - newOffsetColumn,
                            cells[0].length);
                }
            }

            for(int l = line; l < line + nbLines; l++){
                for(int c = column; c < column + nbColumns; c++){
                    if(newCells[l - newOffsetLine][c - newOffsetColumn] == null)
                        newCells[l - newOffsetLine][c - newOffsetColumn] = false;
                }
            }

            offsetLine = newOffsetLine;
            offsetColumn = newOffsetColumn;
            cells = newCells;
        }
    }

    void addCircle(int line, int column){
        this.getChildren().add(
                new Circle(Params.getX(column) - this.getLayoutX() + 0.5 * Params.DEFAULT_CELLS_WIDTH,
                        Params.getY(line) - this.getLayoutY() + 0.5 * Params.DEFAULT_CELLS_WIDTH,
                        Params.DEFAULT_CIRCLE_RADIUS));
        cells[line - offsetLine][column - offsetColumn] = true;
    }

    void xMirror(){
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;

        for(Node child : this.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rect = (Rectangle)child;
                minX = Math.min(minX, rect.getX());
                maxX = Math.max(maxX, rect.getX() + rect.getWidth());
            }
        }

        for(Node child : this.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rect = (Rectangle)child;
                rect.setX(maxX - (rect.getX() - minX) - rect.getWidth());
            }
            else if(child instanceof Circle){
                Circle circle = (Circle) child;
                circle.setCenterX(maxX - (circle.getCenterX() - minX));
            }
        }

        int nbcolumns = cells[0].length;
        for(int line = 0; line < cells.length; line++){
            for(int column = 0; column < nbcolumns / 2; column++){
                Boolean temp = cells[line][column];
                cells[line][column] = cells[line][nbcolumns - 1 - column];
                cells[line][nbcolumns - 1 - column] = temp;
            }
        }

    }

    void yMirror(){
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for(Node child : this.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rect = (Rectangle)child;
                minY = Math.min(minY, rect.getY());
                maxY = Math.max(maxY, rect.getY() + rect.getHeight());
            }
        }

        for(Node child : this.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rect = (Rectangle)child;
                rect.setY(maxY - (rect.getY() - minY) - rect.getHeight());
            }
            else if(child instanceof Circle){
                Circle circle = (Circle) child;
                circle.setCenterY(maxY - (circle.getCenterY() - minY));
            }
        }

        int nbLines = cells.length;
        for(int line = 0; line < nbLines / 2; line++){
            Boolean[] temp = cells[line];
            cells[line] = cells[nbLines - 1 - line];
            cells[nbLines - 1 - line] = temp;
        }

    }

    void clear(){
        this.getChildren().clear();
        this.cells = null;
        this.offsetLine = Integer.MAX_VALUE;
        this.offsetColumn = Integer.MAX_VALUE;
        this.setLayoutX(0);
        this.setLayoutY(0);
    }

    boolean isEmpty(){
        return this.cells == null;
    }

    Selection copy(){
        Selection selection = new Selection();
        selection.setLayoutX(this.getLayoutX());
        selection.setLayoutY(this.getLayoutY());
        selection.setTranslateX(this.getTranslateX());
        selection.setTranslateY(this.getTranslateY());
        selection.offsetLine = this.offsetLine;
        selection.offsetColumn = this.offsetColumn;
        selection.cells = cells.clone();
        for(Node child : this.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rectangle = (Rectangle)child;
                Rectangle rectangle2 =
                        new Rectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                rectangle2.setFill(Params.SELECTION_COLOR);
                selection.getChildren().add(rectangle2);
            }
            else if(child instanceof Circle){
                Circle circle = (Circle) child;
                selection.getChildren().add(
                    new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius())
                );
            }
        }
        return selection;
    }
}