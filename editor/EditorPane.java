package editor;

import editor.global.Params;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;
import java.util.LinkedList;

class EditorPane extends Pane {

    private LinkedList<Line> lines;
    private LinkedList<Line> columns;

    private double offsetX;
    private double offsetY;
    private double maxWidth;
    private double maxHeight;

    private double pressedX;
    private double pressedY;

    private LinkedList<Rectangle> selectionRectangles;
    private HashSet<AliveCircle> selectedCircles;
    private Selection selection;
    private Selection clipboardSelection;
    private Group displayedClipboardSelection;

    EditorPane() {
        lines = new LinkedList<>();
        columns = new LinkedList<>();

        this.setOnMouseClicked(event -> {
            if(!event.isStillSincePress())
                return;
            if(event.getButton() != MouseButton.PRIMARY)
                return;

            if(!event.isControlDown()) {

                this.clearSelectionRectangles();
                int column = getColumn(event.getX());
                int line = getLine(event.getY());
                this.addCircle(line, column);

            }
            else{
                addNewSelectionRectangle();
                int c1 = getColumn(event.getX());
                int l1 = getLine(event.getY());
                resizeCurrentSelectionRectangle(l1, c1, l1 + 1, c1 + 1);
            }
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
            if(event.isStillSincePress())
                return;
            if(event.getButton() != MouseButton.PRIMARY)
                return;
            Rectangle selectionRectangle = this.getCurrentSelectionRectangle();

            selectionRectangle.setX(Math.min(event.getX(), pressedX));
            selectionRectangle.setY(Math.min(event.getY(), pressedY));
            selectionRectangle.setWidth(Math.abs(event.getX() - pressedX));
            selectionRectangle.setHeight(Math.abs(event.getY() - pressedY));

            event.consume();
        });

        this.setOnMousePressed(event -> {

            if(event.getButton() != MouseButton.PRIMARY)
                return;

            pressedX = event.getX();
            pressedY = event.getY();

            if(!event.isControlDown())
                clearSelectionRectangles();

            this.addNewSelectionRectangle();
            event.consume();
        });

        this.setOnMouseReleased(event -> {

            if(event.isStillSincePress())
                return;
            if(event.getButton() != MouseButton.PRIMARY)
                return;

            Rectangle selectionRectangle = this.getCurrentSelectionRectangle();

            int c1 = getColumn(selectionRectangle.getX());
            int l1 = getLine(selectionRectangle.getY());
            int c2 = getColumn(selectionRectangle.getX() + selectionRectangle.getWidth()) + 1;
            int l2 = getLine(selectionRectangle.getY() + selectionRectangle.getHeight()) + 1;
            this.resizeCurrentSelectionRectangle(l1, c1, l2, c2);

            event.consume();
        });

        this.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.getWidth();
            double height = newVal.getHeight();
            updateWidthAndHeight(width, height);
        });

        selectionRectangles = new LinkedList<>();
        selectedCircles = new HashSet<>();

    }

    private Rectangle getCurrentSelectionRectangle(){
        return selectionRectangles.getLast();
    }

    private void clearSelectionRectangles(){
        for(Rectangle selectByDraggingRectangle : selectionRectangles)
            this.getChildren().remove(selectByDraggingRectangle);
        selectionRectangles.clear();
        selectedCircles.clear();
        selection = null;
    }

    private Rectangle addNewSelectionRectangle(){
        Rectangle selectionRectangle = new Rectangle();
        selectionRectangle.setWidth(0);
        selectionRectangle.setHeight(0);
        selectionRectangle.setFill(Params.SELECTION_COLOR);
        selectionRectangles.add(selectionRectangle);
        this.getChildren().add(selectionRectangle);
        return selectionRectangle;
    }

    private void resizeCurrentSelectionRectangle(int l1, int c1, int l2, int c2){
        Rectangle selectionRectangle = this.getCurrentSelectionRectangle();

        double x = getX(c1);
        double y = getY(l1);
        double w = getX(c2) - x;
        double h = getY(l2) - y;

        selectionRectangle.setX(x);
        selectionRectangle.setY(y);
        selectionRectangle.setWidth(w);
        selectionRectangle.setHeight(h);

        boolean[][] cells = new boolean[l2 - l1][c2 - c1];
        for(Node child : this.getChildren()) {
            if (child instanceof AliveCircle) {
                AliveCircle circle = (AliveCircle) child;
                if(circle.line >= l1 && circle .line < l2 && circle.column >= c1 && circle.column < c2) {
                    cells[circle.line - l1][circle.column - c1] = true;
                    selectedCircles.add(circle);
                }
            }
        }
        Selection newselection = new SimpleSelection(l1, c1, cells);

        if(selection == null)
            selection = newselection;
        else
            selection = selection.extend(newselection);
    }

    void cutSelection(){
        this.copySelection();
        this.getChildren().removeAll(selectedCircles);
        this.clearSelectionRectangles();
    }

    void copySelection(){
        this.clipboardSelection = selection;
    }

    void displayClipboardSelection(){
        if(this.clipboardSelection == null)
            return;

        this.clearSelectionRectangles();

        Group newDisplayedCliboardSelection = clipboardSelection.getNode();
        if(this.displayedClipboardSelection != null)
            this.getChildren().remove(displayedClipboardSelection);
        displayedClipboardSelection = newDisplayedCliboardSelection;

        displayedClipboardSelection.setLayoutX(getX(getFirstVisibleColumn()));
        displayedClipboardSelection.setLayoutY(getY(getFirstVisibleLine()));

        displayedClipboardSelection.setOnMouseClicked(Event::consume);
        displayedClipboardSelection.setOnMousePressed(Event::consume);
        displayedClipboardSelection.setOnMouseReleased(Event::consume);
        displayedClipboardSelection.setOnMouseDragged(event -> {
            displayedClipboardSelection.setLayoutX(
                    this.getX(this.getColumn(displayedClipboardSelection.getLayoutX() + event.getX())));
            displayedClipboardSelection.setLayoutY(
                    this.getY(this.getLine(displayedClipboardSelection.getLayoutY() + event.getY())));
            event.consume();
        });

        this.getChildren().add(displayedClipboardSelection);
    }

    void pasteDisplayedSelection(int line, int column, boolean eraseAliveWithDead){
        if(this.clipboardSelection == null)
            return;
        Boolean[][] cells = clipboardSelection.getCells();
        for(int l = 0; l < cells.length; l++){
            for(int c = 0; c < cells[0].length; c++){
                if(cells[l][c] == null)
                    continue;
                AliveCircle circle = getCircle(line + l, column + c);
                if(circle == null && cells[l][c])
                    this.addCircle(line + l, column + c);
                else if(circle != null && !cells[l][c])
                    this.removeCircle(line + l, column + c);
            }
        }
    }

    private double getX(int line){
        return line * Params.DEFAULT_CELLS_WIDTH;
    }

    private int getColumn(double x){
        return (int)Math.floor(x / Params.DEFAULT_CELLS_WIDTH);
    }

    private int getFirstVisibleColumn(){
        return getColumn(-this.getTranslateX()) + 1;
    }

    private double getY(int column){
        return column * Params.DEFAULT_CELLS_WIDTH;
    }

    private int getLine(double y){
        return (int)Math.floor(y / Params.DEFAULT_CELLS_WIDTH);
    }

    private int getFirstVisibleLine(){
        return getColumn(-this.getTranslateY()) + 1;
    }

    AliveCircle addCircle(int line, int column){
        double cx = (column + 0.5) * Params.DEFAULT_CELLS_WIDTH;
        double cy = (line + 0.5) * Params.DEFAULT_CELLS_WIDTH;

        AliveCircle circle = new AliveCircle(cx, cy, Params.DEFAULT_CIRCLE_RADIUS, line, column);
        this.getChildren().add(circle);
        return circle;
    }

    AliveCircle getCircle(int line, int column){
        for(Node child : this.getChildren()) {
            if (child instanceof AliveCircle) {
                AliveCircle circle = (AliveCircle) child;
                if(circle.line == line && circle.column == column)
                    return circle;
            }
        }
        return null;
    }

    AliveCircle removeCircle(int line, int column){
        AliveCircle circle = getCircle(line, column);
        if(circle == null)
            return null;
        this.getChildren().remove(circle);
        return circle;
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
