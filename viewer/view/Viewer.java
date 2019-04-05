package viewer.view;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import viewer.global.Params;
import viewer.model.GameOfLife2;

public class Viewer extends Group{

    private boolean started;

    private AnimationTimer timer;
    private GameOfLifeGraphicsContext gc;

    public Viewer() {

        // Create the Pane
        Canvas pane = new Canvas(Params.SIZE * Params.WIDTH, Params.SIZE * Params.WIDTH);
        this.getChildren().add(pane);

        started = false;

        boolean[][] cells = new boolean[Params.SIZE][Params.SIZE];
        for(int i = 0; i < Params.SIZE; i++)
            for(int j = 0; j < Params.SIZE; j++)
                cells[i][j] = Math.random() < 0.2;

        gc = new GameOfLifeGraphicsContext(pane.getGraphicsContext2D());

        GameOfLife2 gol = new GameOfLife2(gc, Params.SIZE, cells);

        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long currentTime) {
                if(Params.FPS != -1 && (currentTime - lastUpdate) / 1000000 <= Params.FPS)
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
