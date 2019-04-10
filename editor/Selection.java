package editor;

import editor.global.Params;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

abstract class Selection {
    protected abstract Set<SelectedCoordinates> getSelectedCoordinates();

    Selection extend(Selection s){
        return new ComplexSelection(this, s);
    }

    private Boolean[][] cells;

    private void buildCells(){
        Set<SelectedCoordinates> selectedCoordinatess = this.getSelectedCoordinates();

        int minLine = Integer.MAX_VALUE;
        int minColumn = Integer.MAX_VALUE;
        int maxLine = Integer.MIN_VALUE;
        int maxColumn = Integer.MIN_VALUE;

        for(SelectedCoordinates selectedCoordinates : selectedCoordinatess){
            minLine = Math.min(minLine, selectedCoordinates.line);
            minColumn = Math.min(minColumn, selectedCoordinates.column);
            maxLine = Math.max(maxLine, selectedCoordinates.line + selectedCoordinates.cells.length - 1);
            maxColumn = Math.max(maxColumn, selectedCoordinates.column + selectedCoordinates.cells[0].length - 1);
        }

        cells = new Boolean[maxLine - minLine + 1][maxColumn - minColumn + 1];

        for(SelectedCoordinates selectedCoordinates : selectedCoordinatess){
            for(int line = 0; line < selectedCoordinates.cells.length ; line++){
                for(int column = 0; column < selectedCoordinates.cells[0].length ; column++){
                    boolean alive = selectedCoordinates.cells[line][column];
                    if(cells[selectedCoordinates.line + line - minLine][selectedCoordinates.column + column - minColumn] == null)
                        cells[selectedCoordinates.line + line - minLine][selectedCoordinates.column + column - minColumn] = alive;
                    else
                        cells[selectedCoordinates.line + line - minLine][selectedCoordinates.column + column - minColumn] |= alive;
                }
            }
        }
    }

    Boolean[][] getCells(){
        if(cells == null)
            buildCells();
        return cells;
    }

    Group getNode(){
        Boolean[][] cells = getCells();

        int nbLines = cells.length;
        int nbColumns = cells[0].length;

        Group node = new Group();

        for(int line = 0; line <= nbLines; line++){
            node.getChildren().add(
                    new Line(
                            0,
                            line * Params.DEFAULT_CELLS_WIDTH,
                            nbColumns * Params.DEFAULT_CELLS_WIDTH,
                            line * Params.DEFAULT_CELLS_WIDTH));
        }

        for(int column = 0; column <= nbColumns; column++){
            node.getChildren().add(
                    new Line(
                            column * Params.DEFAULT_CELLS_WIDTH,
                            0,
                            column * Params.DEFAULT_CELLS_WIDTH,
                            nbLines * Params.DEFAULT_CELLS_WIDTH));
        }

        for(int line = 0; line < nbLines; line++){
            for(int column = 0; column < nbColumns; column++){
                if(cells[line][column] == null)
                    continue;
                Rectangle rectangle = new Rectangle(
                        column * Params.DEFAULT_CELLS_WIDTH,
                        line * Params.DEFAULT_CELLS_WIDTH,
                        Params.DEFAULT_CELLS_WIDTH,
                        Params.DEFAULT_CELLS_WIDTH
                );
                rectangle.setFill(Params.SELECTION_COLOR);
                node.getChildren().add(rectangle);
                if(cells[line][column]){
                    node.getChildren().add(
                            new Circle(
                            (column + 0.5) * Params.DEFAULT_CELLS_WIDTH,
                            (line + 0.5) * Params.DEFAULT_CELLS_WIDTH,
                            Params.DEFAULT_CIRCLE_RADIUS));
                }
            }
        }


        return node;
    }
}

class SimpleSelection extends Selection {

    private SelectedCoordinates selectedCoordinates;

    SimpleSelection(int line, int column, boolean[][] cells) {
        this.selectedCoordinates = new SelectedCoordinates(line, column, cells);
    }

    @Override
    protected Set<SelectedCoordinates> getSelectedCoordinates() {
        HashSet<SelectedCoordinates> set = new HashSet<>();
        set.add(selectedCoordinates);
        return set;
    }

    @Override
    public String toString() {
        return this.selectedCoordinates.toString();
    }
}

class ComplexSelection extends Selection{

    private Selection select1;
    private Selection select2;

    ComplexSelection(Selection select1, Selection select2) {
        this.select1 = select1;
        this.select2 = select2;
    }

    @Override
    protected Set<SelectedCoordinates> getSelectedCoordinates() {
        HashSet<SelectedCoordinates> set = new HashSet<>();
        set.addAll(select1.getSelectedCoordinates());
        set.addAll(select2.getSelectedCoordinates());
        return set;
    }

    @Override
    public String toString() {
        return this.select1.toString()+"\n "+ this.select2.toString();
    }
}

class SelectedCoordinates {
    int line;
    int column;
    boolean[][] cells;

    SelectedCoordinates(int line, int column, boolean[][] cells) {
        this.line = line;
        this.column = column;
        this.cells = cells;
    }

    @Override
    public String toString() {
        return
                "L " + this.line + " C " + this.column + "\n" +
                        Arrays.deepToString(cells);
    }
}