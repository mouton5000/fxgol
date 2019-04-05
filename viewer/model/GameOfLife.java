package viewer.model;

import javafx.scene.canvas.GraphicsContext;
import viewer.view.GameOfLifeGraphicsContext;

public class GameOfLife {

    private GameOfLifeGraphicsContext gc;

    private byte[][] cells;
    private final int size;

    public int generation = 0;
    public GameOfLife(GameOfLifeGraphicsContext gc, final int size, boolean[][] cells) {
        this.gc = gc;
        this.size = size;
        this.cells = new byte[size + 2][size + 2];

        for(int line = 1; line < size + 1; line++)
            for(int column = 1; column < size + 1; column++) {
                if(cells[line - 1][column - 1])
                    addCell(line, column);
            }
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

//    public int count_neighbor(byte[][] newCells, int i, int j){
//        return newCells[i][j] >> 1;
//    }

    public void nextGeneration() {
        generation++;
        byte[][] newCells = new byte[size + 2][size + 2];

        for(int i = 0; i < size + 2; i++)
            System.arraycopy(cells[i], 0, newCells[i], 0, size + 2);

        for (int line = 1; line < size + 1; line++){
            for (int column = 1; column < size + 1; column++) {
                if(newCells[line][column] == 0)
                    continue;
                int count = newCells[line][column] >> 1;
                //this.count_neighbor(newCells, i, j);
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
        for (int line = 1; line < size + 1; line++)
            for (int column = 1; column < size + 1; column++)
                if((cells[line][column] & 1) == 1)
                    gc.addCell(line, column);
    }


}
