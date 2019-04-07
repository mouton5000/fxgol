package viewer.model;

import viewer.view.GameOfLifeGraphicsContext;

public class FiniteGameOfLife implements GameOfLife{

    private GameOfLifeGraphicsContext gc;

    private byte[][] cells;
    private final int nbCellsPerLine;
    private final int nbCellsPerColumn;
    public FiniteGameOfLife(GameOfLifeGraphicsContext gc, boolean[][] cells) {
        this.gc = gc;
        this.nbCellsPerLine = cells[0].length;
        this.nbCellsPerColumn = cells.length;
        this.cells = new byte[nbCellsPerColumn + 2][nbCellsPerLine + 2];

        for(int line = 1; line < nbCellsPerColumn + 1; line++)
            for(int column = 1; column < nbCellsPerLine + 1; column++) {
                if(cells[line - 1][column - 1])
                    addCell(line, column);
            }

        gc.drawBorder(0, 0, nbCellsPerLine + 2, nbCellsPerColumn + 2);
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
        byte[][] newCells = new byte[nbCellsPerColumn + 2][nbCellsPerLine + 2];

        for(int i = 0; i < nbCellsPerColumn + 2; i++)
            System.arraycopy(cells[i], 0, newCells[i], 0, nbCellsPerLine + 2);

        for (int line = 1; line < nbCellsPerColumn + 1; line++){
            for (int column = 1; column < nbCellsPerLine + 1; column++) {
                if(newCells[line][column] == 0)
                    continue;
                int count = newCells[line][column] >> 1;
                if ((newCells[line][column] & 1) == 0){
                    if (count == 3) {
                        addCell(line, column);
                    }
                }
                else {
                    if (!(count == 2 || count == 3)) {
                        clearCell(line, column);
                    }
                }
            }
        }
    }

    public void redraw(){
        gc.clear();
        for (int line = 1; line < nbCellsPerColumn + 1; line++)
            for (int column = 1; column < nbCellsPerLine + 1; column++)
                if((cells[line][column] & 1) == 1)
                    gc.addCell(line, column);

        gc.drawBorder(0, 0, nbCellsPerLine + 2, nbCellsPerColumn + 2);
    }


}
