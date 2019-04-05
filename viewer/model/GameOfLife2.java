package viewer.model;

import viewer.global.Params;
import javafx.scene.canvas.GraphicsContext;
import viewer.view.GameOfLifeGraphicsContext;

public class GameOfLife2 {

    private GameOfLifeGraphicsContext gc;

    private byte[][] cells;
    private final int size;

    public int generation = 0;
    public GameOfLife2(GameOfLifeGraphicsContext gc, final int size, boolean[][] cells) {
        this.gc = gc;
        this.size = size;
        this.cells = new byte[size + 2][size + 2];

        for(int i = 1; i < size + 1; i++)
            for(int j = 1; j < size + 1; j++) {
                if(cells[i - 1][j - 1])
                    addCell(i, j);
            }
    }

    private void addCell(int i, int j){
        cells[i][j] |= 1;
        for(int k = i - 1; k <= i + 1; k++) {
            cells[k][j - 1] += 2;
            cells[k][j + 1] += 2;
        }
        cells[i - 1][j] += 2;
        cells[i + 1][j] += 2;

        gc.addCell(i, j);
    }

    private void clearCell(int i, int j){
        cells[i][j] &= 254;
        for(int k = i - 1; k <= i + 1; k++) {
            cells[k][j - 1] -= 2;
            cells[k][j + 1] -= 2;
        }
        cells[i - 1][j] -= 2;
        cells[i + 1][j] -= 2;

        gc.clearCell(i, j);
    }

//    public int count_neighbor(byte[][] newCells, int i, int j){
//        return newCells[i][j] >> 1;
//    }

    public void nextGeneration() {
        generation++;
        byte[][] newCells = new byte[size + 2][size + 2];

        for(int i = 0; i < size + 2; i++)
            System.arraycopy(cells[i], 0, newCells[i], 0, size + 2);

        for (int i = 1; i < size + 1; i++){
            for (int j = 1; j < size + 1; j++) {
                if(newCells[i][j] == 0)
                    continue;
                int count = newCells[i][j] >> 1;
                //this.count_neighbor(newCells, i, j);
                if ((newCells[i][j] & 1) == 0){
                    if (count == 3) {
                        addCell(i, j);
                    }
                }
                else {
                    if (!(count == 2 || count == 3)) {
                        clearCell(i, j);
                    }
                }
            }
        }
    }

    public void redraw(){
        gc.clear();
        for (int i = 1; i < size + 1; i++)
            for (int j = 1; j < size + 1; j++)
                if((cells[i][j] & 1) == 1)
                    gc.addCell(i, j);
    }


}
