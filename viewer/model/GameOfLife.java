package viewer.model;

import viewer.global.Params;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameOfLife {

    private GraphicsContext gc;

    private boolean[][] cells = new boolean[Params.SIZE + 2][Params.SIZE + 2];

    public int generation = 0;
    public GameOfLife(GraphicsContext gc) {
        this.gc = gc;

        for(int i = 0; i < Params.SIZE + 2; i++){
            cells[i][0] = cells[i][Params.SIZE + 1] = false;
            cells[0][i] = cells[Params.SIZE + 1][i] = false;
        }

        addCell(10, 10);
        addCell(10, 11);
        addCell(10, 12);
        addCell(9, 12);
        addCell(8, 11);

        for(int i = 1; i < Params.SIZE + 1; i++)
            for(int j = 1; j < Params.SIZE + 1; j++) {
                cells[i][j] = Math.random() < 0.2;
                if(cells[i][j])
                    addCell(i, j);
            }
    }

    public void addCell(int i, int j){
        cells[i][j] = true;
        gc.setFill(Color.BLACK);
        gc.fillRect((i - 1) * Params.WIDTH, (j - 1) * Params.WIDTH, Params.WIDTH, Params.WIDTH);

    }

    public void clearCell(int i, int j){
        cells[i][j] = false;
        gc.setFill(Color.WHITE);
        gc.fillRect((i - 1) * Params.WIDTH, (j - 1) * Params.WIDTH, Params.WIDTH, Params.WIDTH);
    }

    public int count_neighbor(boolean[][] newCells, int i, int j){
        int count = 0;
        for(int k = i - 1; k <= i + 1; k++) {
            if(newCells[k][j - 1])
                count++;
            if(newCells[k][j + 1])
                count++;
        }
        if(newCells[i - 1][j])
            count++;
        if(newCells[i + 1][j])
            count++;
        return count;
    }

    public void nextGeneration() {
        generation++;
        boolean[][] newCells = new boolean[Params.SIZE + 2][Params.SIZE + 2];

        for(int i = 0; i < Params.SIZE + 2; i++)
            System.arraycopy(cells[i], 0, newCells[i], 0, Params.SIZE + 2);

        for (int i = 1; i < Params.SIZE + 1; i++){
            for (int j = 1; j < Params.SIZE + 1; j++) {
                int count = this.count_neighbor(newCells, i, j);
                if (!newCells[i][j]){
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


}
