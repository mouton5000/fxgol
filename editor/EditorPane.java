package editor;

import editor.global.Params;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

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

    private Rectangle selectionRectangle;
    Selection selection;
    private Selection clipboardSelection;

    EditorPane() {
        lines = new LinkedList<>();
        columns = new LinkedList<>();

        this.setOnMouseClicked(event -> {
            if(!event.isStillSincePress())
                return;
            if(event.getButton() != MouseButton.PRIMARY)
                return;

            if(!event.isControlDown()) {
                if(selection.isEmpty()) {
                    int column = Params.getColumn(event.getX());
                    int line = Params.getLine(event.getY());
                    this.addCircle(line, column);
                }
                else
                    pasteSelection();
            }
            else{
                int c1 = Params.getColumn(event.getX());
                int l1 = Params.getLine(event.getY());
                select(l1, c1, l1 + 1, c1 + 1);
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

            selectionRectangle.setVisible(true);
            selectionRectangle.setX(pressedX);
            selectionRectangle.setY(pressedY);
            selectionRectangle.setWidth(0);
            selectionRectangle.setHeight(0);
            event.consume();
        });

        this.setOnMouseReleased(event -> {

            if(event.isStillSincePress())
                return;
            if(event.getButton() != MouseButton.PRIMARY)
                return;

            if(!event.isControlDown())
                pasteSelection();


            int c1 = Params.getColumn(selectionRectangle.getX());
            int l1 = Params.getLine(selectionRectangle.getY());
            int c2 = Params.getColumn(selectionRectangle.getX() + selectionRectangle.getWidth()) + 1;
            int l2 = Params.getLine(selectionRectangle.getY() + selectionRectangle.getHeight()) + 1;
            select(l1, c1, l2, c2);
            selectionRectangle.setVisible(false);

            event.consume();
        });

        this.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.getWidth();
            double height = newVal.getHeight();
            updateWidthAndHeight(width, height);
        });

        selectionRectangle = new Rectangle(0, 0, 0, 0);
        selectionRectangle.setFill(Params.SELECTION_COLOR);
        selectionRectangle.setVisible(false);

        selection = new Selection();

        this.getChildren().addAll(selectionRectangle, selection);
    }

    void cutSelection(){
        if(selection.isEmpty())
            return;
        copySelection();
        selection.clear();
    }

    void copySelection(){
        if(selection.isEmpty())
            return;
        if(clipboardSelection != null)
            this.getChildren().remove(clipboardSelection);
        clipboardSelection = selection.copy();
        clipboardSelection.setVisible(false);
        this.getChildren().add(clipboardSelection);
    }

    void displayClipboardSelection(){
        selection.clear();
        clipboardSelection.setOffset(this.getFirstVisibleColumn(), this.getFirstVisibleLine());
        clipboardSelection.setVisible(true);
        selection = clipboardSelection;
        clipboardSelection = null;
        copySelection();
    }

    void pasteSelection(){
        if(selection.isEmpty())
            return;
        for(int line = 0; line < selection.cells.length; line++){
            for(int column = 0; column < selection.cells[0].length; column++){
                Boolean alive = selection.cells[line][column];
                if(alive == null || !alive)
                    continue;
                if(this.getCircle(line + selection.offsetLine, column + selection.offsetColumn) == null)
                    this.addCircle(line + selection.offsetLine, column + selection.offsetColumn);
            }
        }
        selection.clear();
    }

    void removeSelection(){
        if(selection.isEmpty())
            return;
        selection.clear();
    }

    void xMirrorSelection(){
        if(selection.isEmpty())
            return;
        selection.xMirror();
    }

    void yMirrorSelection(){
        if(selection.isEmpty())
            return;
        selection.yMirror();
    }

    void rotateSelection(){
        if(selection.isEmpty())
            return;
        selection.rotate();
    }

    private void select(int line1, int column1, int line2, int column2){
        selection.addRectangle(line1, column1, line2 - line1, column2 - column1);
        this.getChildren().removeIf(child -> {
            if (child instanceof AliveCircle) {
                AliveCircle circle = (AliveCircle) child;
                if(circle.line >= line1 && circle .line < line2 && circle.column >= column1 && circle.column < column2) {
                    selection.addCircle(circle.line, circle.column);
                    return true;
                }
            }
            return false;
        });
    }

    private int getFirstVisibleColumn(){
        return Params.getColumn(-this.getTranslateX()) + 1;
    }

    private int getFirstVisibleLine(){
        return Params.getColumn(-this.getTranslateY()) + 1;
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
