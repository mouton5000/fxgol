package viewer.view;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import viewer.model.FiniteGameOfLife;
import viewer.model.GameOfLife;
import viewer.model.InfiniteGameOfLife;

public class Viewer extends Group{

    private boolean started;

    private AnimationTimer timer;

    private Canvas pane;
    private GameOfLifeGraphicsContext gc;

    private int generation;

    private static final int MIN_WIDTH = 500;
    private static final int MIN_HEIGHT = 500;

    public Viewer(final int cellSize, boolean[][] cells, final boolean infiniteGrid, final double fps) {

        final int nbCellsPerLine = cells[0].length;
        final int nbCellsPerColumn = cells.length;
        final int windowsWidth = Math.max(MIN_WIDTH, cellSize * (nbCellsPerLine + 2));
        final int windowsHeight = Math.max(MIN_HEIGHT, cellSize * (nbCellsPerColumn + 2));

        // Create the Pane
        pane = new Canvas(windowsWidth, windowsHeight);
//        this.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
//            pane.setHeight(newValue.getHeight());
//            pane.setWidth(newValue.getWidth());
//        });

        this.getChildren().add(pane);

        started = false;

        gc = new GameOfLifeGraphicsContext(
                pane.getGraphicsContext2D(), cellSize,
                windowsWidth, windowsHeight);

        GameOfLife gol;
        if(infiniteGrid){
            gol = new InfiniteGameOfLife(gc, cells);
        }
        else
            gol = new FiniteGameOfLife(gc, cells);


        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long currentTime) {
                if(fps != -1 && (currentTime - lastUpdate) / 1000000 <= 1000 / fps)
                    return;
                lastUpdate = currentTime;
                gol.nextGeneration();
                generation++;
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

    public void setWidth(double width){
        this.pane.setWidth(width);
        this.gc.setWindowsWidth(width);
    }

    public void setHeight(double height){
        this.pane.setHeight(height);
        this.gc.setWindowsHeight(height);
    }

}
