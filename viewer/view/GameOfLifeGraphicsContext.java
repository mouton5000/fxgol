package viewer.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import viewer.global.Params;

public class GameOfLifeGraphicsContext {

    private GraphicsContext gc;
    private double cellsWidth;
    private double offsetX;
    private double offsetY;


    public GameOfLifeGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
        this.cellsWidth = Params.WIDTH;
        this.offsetY = 0;
        this.offsetX = 0;
    }

    public void addCell(int i, int j){
        gc.setFill(Color.BLACK);
        gc.fillRect((i - 1) * cellsWidth - offsetX, (j - 1) * cellsWidth - offsetY, cellsWidth, cellsWidth);
    }

    public void clearCell(int i, int j){
        gc.setFill(Color.WHITE);
        gc.fillRect((i - 1) * cellsWidth - offsetX, (j - 1) * cellsWidth - offsetY, cellsWidth, cellsWidth);
    }

    public void clear(){
        this.gc.clearRect(0, 0, Params.WIDTH * Params.SIZE, Params.WIDTH * Params.SIZE);
    }

    public void moveCamera(int dx, int dy, double zoom) {
        this.offsetX += 10 * dx;
        this.offsetY += 10 * dy;

        if(zoom != 0) {
            double centerx = (offsetX + (Params.WIDTH * Params.SIZE) / 2) / this.cellsWidth;
            double centery = (offsetY + (Params.WIDTH * Params.SIZE) / 2)  / this.cellsWidth;
            if (zoom > 0 && this.cellsWidth < 100)
                this.cellsWidth += ((this.cellsWidth >= 10) ? 10 : 1) * zoom;
            else if (zoom < 0 && this.cellsWidth > 1)
                this.cellsWidth += ((this.cellsWidth > 10) ? 10 : 1) * zoom;
            offsetX = centerx * this.cellsWidth - (Params.WIDTH * Params.SIZE) / 2;
            offsetY = centery * this.cellsWidth - (Params.WIDTH * Params.SIZE) / 2;
        }

    }
}
