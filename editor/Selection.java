package editor;

import editor.global.Params;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import undoredo.UndoRedo;
import util.Coords;

class Selection extends Group {

    private UndoRedo undoRedo;
    int offsetLine;
    int offsetColumn;

    Boolean[][] cells;

    private double dragX, dragY;
    private int movingLine, movingColumn;

    Selection(UndoRedo undoRedo){
        this.undoRedo = undoRedo;
        offsetLine = Integer.MAX_VALUE;
        offsetColumn = Integer.MAX_VALUE;

        this.setOnMouseClicked(Event::consume);
        this.setOnMousePressed(event -> {
            if(event.getButton() != MouseButton.PRIMARY)
                return;
            this.dragX = (int)(event.getX() / Params.DEFAULT_CELLS_WIDTH) * Params.DEFAULT_CELLS_WIDTH;
            this.dragY = (int)(event.getY() / Params.DEFAULT_CELLS_WIDTH) * Params.DEFAULT_CELLS_WIDTH;
            this.movingLine = 0;
            this.movingColumn = 0;
            event.consume();
        });
        this.setOnMouseDragged(event -> {
            if(event.getButton() != MouseButton.PRIMARY)
                return;
            int dline = (int)((event.getY() - dragY) / Params.DEFAULT_CELLS_WIDTH);
            int dcolumn = (int)((event.getX() - dragX) / Params.DEFAULT_CELLS_WIDTH);
            if(dline != 0 || dcolumn != 0) {
                this.translate(dline, dcolumn);
                this.movingLine += dline;
                this.movingColumn += dcolumn;
            }
            event.consume();
        });
        this.setOnMouseReleased(event -> {
            Coords srcCoords = new Coords((int)(this.dragY / Params.DEFAULT_CELLS_WIDTH),
                    (int)(this.dragX / Params.DEFAULT_CELLS_WIDTH));
            Coords destCoords = new Coords(srcCoords.line + movingLine, srcCoords.column + movingColumn);
            this.translate(srcCoords.line - destCoords.line, srcCoords.column - destCoords.column);
            MoveSelectionAction action = new MoveSelectionAction(this, srcCoords, destCoords);
            undoRedo.add(action);
            event.consume();
        });
    }

    private Selection(Selection selection, UndoRedo undoRedo){
        this(undoRedo);
        this.copy(selection);
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
        for(int l = line; l < line + nbLines; l++){
            for(int c = column; c < column + nbColumns; c++){
                Rectangle rectangle = new Rectangle(
                        Params.getX(c) + 1 - this.getLayoutX(),
                        Params.getY(l) + 1- this.getLayoutY(),
                        Params.DEFAULT_CELLS_WIDTH - 2,
                        Params.DEFAULT_CELLS_WIDTH - 2
                );
                rectangle.setFill(Params.SELECTION_COLOR);
                this.getChildren().add(rectangle);
                rectangle.toBack();
            }
        }

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

    void addCircle(int line, int column){
        this.getChildren().add(
                new Circle(Params.getX(column) - this.getLayoutX() + 0.5 * Params.DEFAULT_CELLS_WIDTH,
                        Params.getY(line) - this.getLayoutY() + 0.5 * Params.DEFAULT_CELLS_WIDTH,
                        Params.DEFAULT_CIRCLE_RADIUS));
            cells[line - offsetLine][column - offsetColumn] = true;
    }

    void xMirror(){
        Selection prev = new Selection(this, this.undoRedo );


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


        Selection next = new Selection(this, this.undoRedo );
        undoRedo.add(new EditSelectionAction(this, prev, next));

    }

    void yMirror(){
        Selection prev = new Selection(this, this.undoRedo );
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


        Selection next = new Selection(this, this.undoRedo );
        undoRedo.add(new EditSelectionAction(this, prev, next));
    }

    void rotate(){
        Selection prev = new Selection(this, this.undoRedo );
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for(Node child : this.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rect = (Rectangle)child;
                minX = Math.min(minX, rect.getX());
                maxX = Math.max(maxX, rect.getX() + rect.getWidth());
                minY = Math.min(minY, rect.getY());
                maxY = Math.max(maxY, rect.getY() + rect.getHeight());
            }
        }

        for(Node child : this.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rect = (Rectangle)child;

                double x = rect.getX();
                double y = rect.getY();
                double w = rect.getWidth();
                double h = rect.getHeight();

                rect.setX(minX + maxY - y - h);
                rect.setY(minY + x - minX);
                rect.setWidth(h);
                rect.setHeight(w);
            }
            else if(child instanceof Circle){
                Circle circle = (Circle) child;
                double x = circle.getCenterX();
                double y = circle.getCenterY();
                circle.setCenterX(minX + maxY - y);
                circle.setCenterY(minY + x - minX);
            }
        }

        int newNbLines = cells[0].length;
        int newNbColumns = cells.length;
        Boolean[][] newCells = new Boolean[newNbLines][newNbColumns];
        for(int line = 0; line < newNbLines; line++)
            for(int column = 0; column < newNbColumns; column++)
                newCells[line][column] = cells[cells.length - 1 - column][line];
        cells = newCells;

        Selection next = new Selection(this, this.undoRedo );
        undoRedo.add(new EditSelectionAction(this, prev, next));
    }

    void step(){
        Selection prev = new Selection(this, this.undoRedo );
        int nbLines = cells.length;
        int nbColumns = cells[0].length;
        Boolean[][] newCells = new Boolean[nbLines + 2][nbColumns + 2];

        for(int line = -1; line <= nbLines; line++){
            for(int column = -1; column <= nbColumns; column++){
                int nbNeighbors = 0;
                for(int deltaL = -1; deltaL <= 1; deltaL++){
                    int dLine = deltaL + line;
                    if(dLine < 0 || dLine >= nbLines)
                        continue;
                    for(int deltaC = -1; deltaC <= 1; deltaC++){
                        if(deltaC == 0 && deltaL == 0)
                            continue;
                        int dColumn = deltaC + column;
                        if(dColumn < 0 || dColumn >= nbColumns)
                            continue;
                        Boolean alive = cells[dLine][dColumn];
                        nbNeighbors += (alive != null && alive)?1:0;
                    }
                }

                Boolean alive = null;
                if(line >= 0 && line < nbLines && column >= 0 && column < nbColumns && cells[line][column] != null)
                    alive = cells[line][column];

                if(alive != null && alive){
                    newCells[line + 1][column + 1] = nbNeighbors == 2 || nbNeighbors == 3;
                }
                else{
                    if(nbNeighbors == 3)
                        newCells[line + 1][column + 1] = true;
                    else
                        newCells[line + 1][column + 1] = alive;
                }
            }
        }

        this.getChildren().removeIf(child -> child instanceof Circle);

        int currentOffsetLine = offsetLine;
        int currentOffsetColumn = offsetColumn;
        for(int line = -1; line <= nbLines; line++) {
            for (int column = -1; column <= nbColumns; column++) {
                Boolean alive = newCells[line + 1][column + 1];
                int dLine = line + currentOffsetLine - offsetLine;
                int dColumn = column + currentOffsetColumn - offsetColumn;
                if(alive != null && alive) {
                    if(dLine < 0 || dLine >= cells.length || dColumn < 0 || dColumn >= cells[0].length
                            || cells[dLine][dColumn] == null) {
                        this.addRectangle(currentOffsetLine + line, currentOffsetColumn + column,
                                1, 1);
                    }
                    this.addCircle(currentOffsetLine + line, currentOffsetColumn + column);
                }
                else if(dLine >= 0 && dLine < cells.length && dColumn >= 0 && dColumn < cells[0].length) {
                    cells[dLine][dColumn] = alive;
                }
            }
        }
        Selection next = new Selection(this, this.undoRedo );
        undoRedo.add(new EditSelectionAction(this, prev, next));
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

    void copy(Selection selection){
        this.clear();

        this.setLayoutX(selection.getLayoutX());
        this.setLayoutY(selection.getLayoutY());
        this.setTranslateX(selection.getTranslateX());
        this.setTranslateY(selection.getTranslateY());
        this.offsetLine = selection.offsetLine;
        this.offsetColumn = selection.offsetColumn;
        this.cells = selection.cells.clone();
        for(Node child : selection.getChildren()){
            if(child instanceof Rectangle){
                Rectangle rectangle = (Rectangle)child;
                Rectangle rectangle2 =
                        new Rectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                rectangle2.setFill(Params.SELECTION_COLOR);
                this.getChildren().add(rectangle2);
            }
            else if(child instanceof Circle){
                Circle circle = (Circle) child;
                this.getChildren().add(
                    new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius())
                );
            }
        }
    }
}