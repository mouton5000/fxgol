package editor;

import editor.global.Params;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import undoredo.UndoRedo;
import util.Coords;
import util.RunLenghtEncodingTranslator;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

class EditorPane extends Pane {

    private LinkedList<Line> lines;
    private LinkedList<Line> columns;

    private double offsetX;
    private double offsetY;
    private double maxWidth;
    private double maxHeight;
    private Scale scale;

    private double pressedX;
    private double pressedY;

    private Rectangle selectionRectangle;
    private Selection selection;
    private Selection clipboardSelection;

    private File currentSaveFile;

    private boolean selectionEraseMode;

    private UndoRedo undoRedo;

    EditorPane(UndoRedo undoRedo) {
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
                    this.addCircleWithUndoRedoAction(line, column);
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

        scale = new Scale();
        this.getTransforms().add(scale);

        this.setOnScroll(event -> {
            boolean zoomIn = (event.getDeltaY() > 0);
            this.zoom(Params.STEPZOOM * (zoomIn?1:-1));
        });

        this.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.getWidth();
            double height = newVal.getHeight();
            updateWidthAndHeight(width, height);
            scale.setPivotX(scale.getPivotX() + (width - oldVal.getWidth()) / 2);
            scale.setPivotY(scale.getPivotY() + (height - oldVal.getHeight()) / 2);
        });

        selectionRectangle = new Rectangle(0, 0, 0, 0);
        selectionRectangle.setFill(Params.SELECTION_RECTANGLE_COLOR);
        selectionRectangle.setVisible(false);

        selection = new Selection(undoRedo);
        clipboardSelection = new Selection(undoRedo);
        clipboardSelection.setVisible(false);
        this.getChildren().addAll(selectionRectangle, selection, clipboardSelection);
        selection.toBack();
        selectionRectangle.toFront();
        selectionEraseMode = false;

        this.undoRedo = undoRedo;
    }

    void cutSelection(){
        if(selection.isEmpty())
            return;
        Selection prev = new Selection(selection, this.undoRedo );
        copySelection();
        selection.clear();
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(new EditSelectionAction(selection, prev, next));
    }

    void copySelection(){
        if(selection.isEmpty())
            return;
        this.clipboardSelection.copy(selection);
    }

    void copyPattern(boolean[][] cells){
        if(!selection.isEmpty())
            pasteSelection();
        Selection prev = new Selection(selection, this.undoRedo );
        selection.addRectangle(0, 0, cells.length, cells[0].length);
        for(int line = 0; line < cells.length; line++){
            for(int column = 0; column < cells[0].length; column++){
                if(cells[line][column]){
                    selection.addCircle(line, column);
                }
            }
        }
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(new EditSelectionAction(selection, prev, next));
    }

    void displayClipboardSelection(){
        if(clipboardSelection.isEmpty())
            return;
        Selection prev = new Selection(selection, this.undoRedo );
        selection.copy(clipboardSelection);
        selection.setOffset(this.getFirstVisibleLine(), this.getFirstVisibleColumn());
        if(selectionEraseMode)
            selection.toFront();
        else
            selection.toBack();
        selectionRectangle.toFront();
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(new EditSelectionAction(selection, prev, next));
    }

    void pasteSelection(){
        if(selection.isEmpty())
            return;
        LinkedList<Coords> addedCircles = new LinkedList<>();
        LinkedList<Coords> removedCircles = new LinkedList<>();
        Selection prev = new Selection(selection, this.undoRedo );
        for(int line = 0; line < selection.cells.length; line++){
            for(int column = 0; column < selection.cells[0].length; column++){
                Boolean alive = selection.cells[line][column];
                if(alive == null)
                    continue;
                if(alive){
                    if(this.getCircle(line + selection.offsetLine, column + selection.offsetColumn) == null)
                        addedCircles.add(new Coords(
                                line + selection.offsetLine, column + selection.offsetColumn
                        ));
                    }
                else if(selectionEraseMode && this.getCircle(line + selection.offsetLine, column + selection.offsetColumn) != null) {
                    removedCircles.add(
                            new Coords(line + selection.offsetLine, column + selection.offsetColumn)
                    );
                }
            }
        }
        selection.clear();
        Selection next = new Selection(selection, this.undoRedo);
        undoRedo.add(
                new CompositeAction(
                        new AddRemoveCirclesAction(this, addedCircles, removedCircles),
                        new EditSelectionAction(selection, prev, next)
                ));
    }

    void removeSelection(){
        if(selection.isEmpty())
            return;
        Selection prev = new Selection(selection, this.undoRedo );
        selection.clear();
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(new EditSelectionAction(selection, prev, next));
    }

    void xMirrorSelection(){
        if(selection.isEmpty())
            return;
        Selection prev = new Selection(selection, this.undoRedo );
        selection.xMirror();
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(new EditSelectionAction(selection, prev, next));
    }

    void yMirrorSelection(){
        if(selection.isEmpty())
            return;
        Selection prev = new Selection(selection, this.undoRedo );
        selection.yMirror();
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(new EditSelectionAction(selection, prev, next));
    }

    void rotateSelection(){
        if(selection.isEmpty())
            return;
        Selection prev = new Selection(selection, this.undoRedo );
        selection.rotate();
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(new EditSelectionAction(selection, prev, next));
    }

    void stepSelection(){
        if(selection.isEmpty())
            return;
        Selection prev = new Selection(selection, this.undoRedo );
        selection.step();
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(new EditSelectionAction(selection, prev, next));
    }

    private void select(int line1, int column1, int line2, int column2){
        Selection prev = new Selection(selection, this.undoRedo );
        selection.addRectangle(line1, column1, line2 - line1, column2 - column1);
        LinkedList<Coords> coordsList = new LinkedList<>();
        for(Node child : this.getChildren()) {
            if (child instanceof AliveCircle) {
                AliveCircle circle = (AliveCircle) child;
                if (circle.line >= line1 && circle.line < line2 && circle.column >= column1 && circle.column < column2) {
                    coordsList.add(new Coords(circle.line, circle.column));
                    selection.addCircle(circle.line,circle.column);
                }
            }
        }
        Selection next = new Selection(selection, this.undoRedo );
        undoRedo.add(
                new CompositeAction(
                        new AddRemoveCirclesAction(this, new LinkedList<>(), coordsList),
                        new EditSelectionAction(selection, prev, next)
                ));
    }

    void clear() {
        selection.clear();
        if(clipboardSelection != null)
            clipboardSelection.clear();
        this.getChildren().removeIf(child -> child instanceof AliveCircle);

        this.zoom(1 - scale.getX());
        this.translate(-this.getTranslateX(), -this.getTranslateY());
    }

    String open() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        if(currentSaveFile != null)
            fileChooser.setInitialDirectory(currentSaveFile.getParentFile());
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Run lenght encoding (.rle)", "*.rle"));
        File file = fileChooser.showOpenDialog(this.getScene().getWindow());
        if(file == null)
            return null;

        currentSaveFile = file;
        StringBuilder lines = new StringBuilder();
        try {
            BufferedReader bufferedReader =
                    new BufferedReader(new FileReader(file));
            String line;
            while((line = bufferedReader.readLine()) != null){
                lines.append(line);
                lines.append("\n");
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        this.clear();
        boolean[][] cells = RunLenghtEncodingTranslator.fromRLE(lines.toString());
        this.copyPattern(cells);
        this.pasteSelection();
        return file.getAbsolutePath();
    }

    String save(){
        if(currentSaveFile == null)
            return saveAs();
        else
            return saveAs(currentSaveFile);
    }

    String saveAs(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        if(currentSaveFile != null)
            fileChooser.setInitialDirectory(currentSaveFile.getParentFile());
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Run lenght encoding (.rle)", "*.rle"));
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if(file == null)
            return null;
        if(!file.getName().endsWith(".rle"))
            file = new File(file.getAbsolutePath() + ".rle");
        return saveAs(file);
    }

    String saveAs(File file){
        currentSaveFile = file;

        boolean[][] cells = this.getCells();
        String rleDescription = RunLenghtEncodingTranslator.toRLE(cells);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(rleDescription);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    private int getFirstVisibleColumn(){
        return Params.getColumn(scale.getPivotX() - this.getWidth() / (2 * scale.getX())) + 1;
    }

    private int getFirstVisibleLine(){
        return Params.getColumn(scale.getPivotY() - this.getHeight() / (2 * scale.getY())) + 1;
    }

    void addCircleWithUndoRedoAction(int line, int column){
        LinkedList<Coords> added = new LinkedList<>();
        added.add(new Coords(line, column));
        undoRedo.add(new AddRemoveCirclesAction(this, added, new LinkedList<>()));
    }

    void removeCircleWithUndoRedoAction(int line, int column){
        LinkedList<Coords> removed = new LinkedList<>();
        removed.add(new Coords(line, column));
        undoRedo.add(new AddRemoveCirclesAction(this, new LinkedList<>(), removed));
    }

    AliveCircle addCircle(int line, int column){
        double cx = (column + 0.5) * Params.DEFAULT_CELLS_WIDTH;
        double cy = (line + 0.5) * Params.DEFAULT_CELLS_WIDTH;

        AliveCircle circle = new AliveCircle(cx, cy, Params.DEFAULT_CIRCLE_RADIUS, line, column);
        this.getChildren().add(circle);
        if(selectionEraseMode)
            circle.toBack();
        else
            circle.toFront();
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

    boolean[][] getCells(){
        return getCells(true, false, -1, -1, -1, -1);
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
        dx = dx / scale.getX();
        dy = dy / scale.getY();

        this.setTranslateX(this.getTranslateX() + dx);
        this.setTranslateY(this.getTranslateY() + dy);

        scale.setPivotX(scale.getPivotX() - dx);
        scale.setPivotY(scale.getPivotY() - dy);

        offsetX -= dx;
        offsetY -= dy;

        double width = Math.max(Math.abs(-2 * offsetX - this.getWidth()), Math.abs(-2 * offsetX + this.getWidth()));
        double height = Math.max(Math.abs(-2 * offsetY - this.getHeight()), Math.abs(-2 * offsetY + this.getHeight()));

        updateWidthAndHeight(width, height);
    }

    void zoom(double dz){
        double sx = scale.getX() + dz;
        double sy = scale.getY() + dz;
        if(sx > Params.MINZOOM && sy > Params.MINZOOM && sx < Params.MAXZOOM && sy < Params.MAXZOOM) {
            scale.setX(sx);
            scale.setY(sy);
            updateWidthAndHeight(this.getWidth(), this.getHeight());
        }
    }

    private void updateWidthAndHeight(double width, double height){
        width = width / scale.getX();
        height = height / scale.getY();

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

    void setSelectionEraseMode(boolean selectionEraseMode) {
        this.selectionEraseMode = selectionEraseMode;
        if(selectionEraseMode)
            selection.toFront();
        else
            selection.toBack();
    }
}
