package viewer.model;

import viewer.view.GameOfLifeGraphicsContext;

public class InfiniteGameOfLife implements GameOfLife {

    private GameOfLifeGraphicsContext gc;
    private static final int BASIC_SIZE = 100;

    private byte[][] cells;
    private int currentNbLines;
    private int currentNbColumns;

    public InfiniteGameOfLife(GameOfLifeGraphicsContext gc, boolean[][] cells) {
        this.gc = gc;

        currentNbLines = (cells[0].length / BASIC_SIZE + 1) * BASIC_SIZE;
        currentNbColumns = (cells.length / BASIC_SIZE + 1) * BASIC_SIZE;

        this.cells = new byte[currentNbLines + 2][currentNbColumns + 2];

        for(int line = 0; line < cells.length; line++)
            for(int column = 0; column < cells[0].length; column++)
                if(cells[line][column])
                    addCell(line + 1, column + 1);
    }

    private void addCell(int line, int column){
        cells[line][column] |= 1;
        for(int k = line - 1; k <= line + 1; k++) {
            cells[k][column - 1] += 2;
            cells[k][column + 1] += 2;
        }
        cells[line - 1][column] += 2;
        cells[line + 1][column] += 2;

        gc.addCell(line, column);
    }

    private void clearCell(int line, int column){
        cells[line][column] &= 254;
        for(int k = line - 1; k <= line + 1; k++) {
            cells[k][column - 1] -= 2;
            cells[k][column + 1] -= 2;
        }
        cells[line - 1][column] -= 2;
        cells[line + 1][column] -= 2;

        gc.clearCell(line, column);
    }

    public void nextGeneration() {
        byte[][] newCells = new byte[currentNbLines + 2][currentNbColumns + 2];

        for (int i = 0; i < currentNbLines + 2; i++)
            System.arraycopy(cells[i], 0, newCells[i], 0, currentNbColumns + 2);

        for (int line = 1; line < currentNbLines + 1; line++) {
            for (int column = 1; column < currentNbColumns + 1; column++) {
                if (newCells[line][column] == 0)
                    continue;
                int count = newCells[line][column] >> 1;
                if ((newCells[line][column] & 1) == 0) {
                    if (count == 3) {
                        addCell(line, column);
                    }
                } else {
                    if (!(count == 2 || count == 3)) {
                        clearCell(line, column);
                    }
                }
            }
        }

        // If any of the border cells are alive, extend the cells in the given direction.

        boolean doExtendLeft = false;
        boolean doExtendRight = false;
        boolean doExtendUp = false;
        boolean doExtendDown = false;

        for (int line = 1; line < currentNbLines + 1; line++)
            if ((newCells[line][0] >> 1) == 3) {
                doExtendLeft = true;
                break;
            }

        for (int line = 1; line < currentNbLines + 1; line++)
            if ((newCells[line][currentNbColumns + 1] >> 1) == 3) {
                doExtendRight = true;
                break;
            }

        for (int column = 1; column < currentNbColumns + 1; column++)
            if((newCells[0][column] >> 1) == 3){
                doExtendUp = true;
                break;
            }

        for (int column = 1; column < currentNbColumns + 1; column++)
            if((newCells[currentNbLines + 1][column] >> 1) == 3){
                doExtendDown = true;
                break;
            }


        if(doExtendLeft || doExtendRight || doExtendUp || doExtendDown) {
            int offsetLine = doExtendUp?BASIC_SIZE:0;
            int offsetColumn = doExtendLeft?BASIC_SIZE:0;
            int deltaNbLines = offsetLine + (doExtendDown?BASIC_SIZE:0);
            int deltaNbColumn = offsetColumn + (doExtendRight?BASIC_SIZE:0);

            extend(offsetLine, offsetColumn, deltaNbLines, deltaNbColumn);

            if(doExtendLeft){
                for (int line = 1; line < currentNbLines - deltaNbLines + 1; line++)
                    if ((newCells[line][0] >> 1) == 3)
                        addCell(offsetLine + line, offsetColumn);
            }

            if(doExtendRight){
                for (int line = 1; line < currentNbLines - deltaNbLines + 1; line++)
                    if ((newCells[line][currentNbColumns - deltaNbColumn + 1] >> 1) == 3)
                        addCell(offsetLine + line, offsetColumn + currentNbColumns - deltaNbColumn + 1);
            }

            if(doExtendUp){
                for (int column = 1; column < currentNbColumns - deltaNbColumn + 1; column++)
                    if((newCells[0][column] >> 1) == 3)
                        addCell(offsetLine, offsetColumn + column);
            }

            if(doExtendDown){
                for (int column = 1; column < currentNbColumns - deltaNbColumn + 1; column++)
                    if((newCells[currentNbLines - deltaNbLines + 1][column] >> 1) == 3)
                        addCell(offsetLine + currentNbLines - deltaNbLines + 1, offsetColumn + column);
            }
        }

    }

    /**
     * Copy the array cell into a new bigger array by adding blank lines and columns.
     *
     * @param offsetLine : number of blank lines added before index 0 of the current array
     * @param offsetColumn : number of blank columns added before index 0 of current array
     * @param deltaNbLines : number of lines added to the array (must be at least offsetLine)
     * @param deltaNbColumns : number of columns added to the array (must be at least offsetColumn)
     */
    private void extend(int offsetLine, int offsetColumn, int deltaNbLines, int deltaNbColumns){
        byte[][] newCells = new byte[currentNbLines + 2 + deltaNbLines][currentNbColumns + 2 + deltaNbColumns];

        for(int i = 0; i < currentNbLines + 2; i++)
            System.arraycopy(cells[i], 0, newCells[i + offsetLine], offsetColumn, currentNbColumns + 2);

        currentNbLines += deltaNbLines;
        currentNbColumns += deltaNbColumns;

        cells = newCells;

        gc.moveCamera(offsetLine, offsetColumn);
        redraw();
    }

    public void redraw(){
        gc.clear();
        for (int line = 1; line < currentNbLines + 1; line++)
            for (int column = 1; column < currentNbColumns + 1; column++)
                if((cells[line][column] & 1) == 1)
                    gc.addCell(line, column);
    }

    private void printCells(){
        System.out.println();
        System.out.println("New Generation " + currentNbLines +" " + currentNbColumns);
        for (int i = 0; i < currentNbLines + 2; i++){
            for(int j = 0; j < currentNbColumns + 2; j++){
                System.out.print(cells[i][j] / 2);
            }

            System.out.print(" ");

            for(int j = 0; j < currentNbColumns + 2; j++){
                System.out.print(cells[i][j] & 1);
            }
            System.out.println();
        }
    }
}
