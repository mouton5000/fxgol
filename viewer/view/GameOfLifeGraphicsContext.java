package viewer.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameOfLifeGraphicsContext {

    private GraphicsContext gc;
    private double cellsWidth;
    private double offsetX;
    private double offsetY;
    private double windowsWidth;
    private double windowsHeight;


    public GameOfLifeGraphicsContext(GraphicsContext gc, int cellsWidth,
                                     int windowsWidth, int windowsHeight) {
        this.gc = gc;
        this.cellsWidth = cellsWidth;
        this.offsetY = 0;
        this.offsetX = 0;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    public void addCell(int line, int column){
        gc.setFill(Color.BLACK);
        gc.fillRect(
                (column - 1) * cellsWidth - offsetX,
                (line - 1) * cellsWidth - offsetY,
                cellsWidth,
                cellsWidth);
    }

    public void clearCell(int line, int column){
        gc.setFill(Color.WHITE);
        gc.fillRect(
                (column - 1) * cellsWidth - offsetX,
                (line - 1) * cellsWidth - offsetY,
                cellsWidth,
                cellsWidth);
    }

    public void clear(){
        this.gc.clearRect(0, 0, this.windowsWidth, this.windowsHeight);
    }

    public void moveCamera(int dline, int dcolumn){
        this.offsetX += this.cellsWidth * dcolumn;
        this.offsetY += this.cellsWidth * dline;
    }

    public void moveCamera(int dx, int dy, double zoom) {
        this.offsetX += 10 * dx;
        this.offsetY += 10 * dy;

        if(zoom != 0) {
            double centerx = (offsetX + this.windowsWidth / 2) / this.cellsWidth;
            double centery = (offsetY + this.windowsHeight / 2)  / this.cellsWidth;

            if (zoom > 0 && this.cellsWidth < 100)
                this.cellsWidth += ((this.cellsWidth >= 10) ? 10 : 1) * zoom;
            else if (zoom < 0 && this.cellsWidth > 1)
                this.cellsWidth += ((this.cellsWidth > 10) ? 10 : 1) * zoom;
            offsetX = centerx * this.cellsWidth - this.windowsWidth / 2;
            offsetY = centery * this.cellsWidth - this.windowsHeight / 2;
        }

    }

    public void setWindowsWidth(double windowsWidth) {
        this.windowsWidth = windowsWidth;
    }

    public void setWindowsHeight(double windowsHeight) {
        this.windowsHeight = windowsHeight;
    }
}
