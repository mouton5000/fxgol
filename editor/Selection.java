package editor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

abstract class Selection {
    protected abstract Set<SelectedCoordinates> getSelectedCoordinates();

    Selection extend(Selection s){
        return new ComplexSelection(this, s);
    }

    private Boolean[][] cells;

    Boolean[][] getCells(){
        if(cells != null)
            return cells;

        int minLine = Integer.MAX_VALUE;
        int minColumn = Integer.MAX_VALUE;
        int maxLine = Integer.MIN_VALUE;
        int maxColumn = Integer.MIN_VALUE;

        for(SelectedCoordinates selectedCoordinates : getSelectedCoordinates()){
            minLine = Math.min(minLine, selectedCoordinates.line);
            minColumn = Math.min(minColumn, selectedCoordinates.column);
            maxLine = Math.max(maxLine, selectedCoordinates.line + selectedCoordinates.cells.length - 1);
            maxColumn = Math.max(maxColumn, selectedCoordinates.column + selectedCoordinates.cells[0].length - 1);
        }

        Boolean[][] cells = new Boolean[maxLine - minLine + 1][maxColumn - minColumn + 1];

        for(SelectedCoordinates selectedCoordinates : getSelectedCoordinates()){
            for(int line = 0; line < selectedCoordinates.cells.length ; line++){
                for(int column = 0; column < selectedCoordinates.cells[0].length ; column++){
                    boolean alive = selectedCoordinates.cells[line][column];
                    if(cells[selectedCoordinates.line - minLine][selectedCoordinates.column - minColumn] == null)
                        cells[selectedCoordinates.line - minLine][selectedCoordinates.column - minColumn] = alive;
                    else
                        cells[selectedCoordinates.line - minLine][selectedCoordinates.column - minColumn] |= alive;
                }
            }
        }

        this.cells = cells;
        return cells;
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