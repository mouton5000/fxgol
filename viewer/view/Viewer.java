package viewer.view;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import viewer.model.GameOfLife;

public class Viewer extends Group{

    private boolean started;

    private AnimationTimer timer;
    private GameOfLifeGraphicsContext gc;

    public Viewer(final int cellSize, boolean[][] cells, final double fps) {

        final int nbCellsPerLine = cells.length;
        final int windowsWidth = cellSize * nbCellsPerLine;

        // Create the Pane
        Canvas pane = new Canvas(windowsWidth, windowsWidth);
        this.getChildren().add(pane);

        started = false;

        gc = new GameOfLifeGraphicsContext(pane.getGraphicsContext2D(), cellSize, nbCellsPerLine);

        GameOfLife gol = new GameOfLife(gc, nbCellsPerLine, cells);

        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long currentTime) {
                if(fps != -1 && (currentTime - lastUpdate) / 1000000 <= 1000 / fps)
                    return;
                lastUpdate = currentTime;
                if(gol.generation <= 1000)
                    gol.nextGeneration();
                else
                    timer.stop();
            }
        };


        pane.setOnMouseClicked(
                event -> {
                    if(started)
                        timer.stop();
                    else
                        timer.start();
                    started = !started;
                }
        );

        pane.setOnScroll(
                scrollEvent -> {
                    if(scrollEvent.getDeltaY() < 0){
                        gc.moveCamera(0, 0, -1);
                        gol.redraw();
                    }
                    else{
                        gc.moveCamera(0, 0, 1);
                        gol.redraw();
                    }
                }
        );

        this.setOnKeyPressed(
                keyEvent -> {
                    if(keyEvent.getCode() == KeyCode.RIGHT){
                        gc.moveCamera(1, 0, 0);
                        gol.redraw();
                    }
                    else if(keyEvent.getCode() == KeyCode.LEFT){
                        gc.moveCamera(-1, 0, 0);
                        gol.redraw();
                    }
                    else if(keyEvent.getCode() == KeyCode.UP){
                        gc.moveCamera(0, -1, 0);
                        gol.redraw();
                    }
                    else if(keyEvent.getCode() == KeyCode.DOWN){
                        gc.moveCamera(0, 1, 0);
                        gol.redraw();
                    }
                }
        );

    }
}
