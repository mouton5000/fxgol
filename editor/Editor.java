package editor;

import editor.global.Params;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import viewer.view.Viewer;

import java.util.Optional;

public class Editor extends BorderPane {

    private int offsetLine = 0;
    private int offsetColumn = 0;
    private int viewerNbCellsPerLine = Params.DEFAULT_NB_CELLS_PER_LINE;
    private int viewerCellsWidth = Params.DEFAULT_CELLS_WIDTH;
    private double fps = Params.DEFAULT_FPS;

    private EditorPane pane;

    private StatusBar statusBar;
    private Label statusBarOffsetLabel;


    private Double dragX;
    private Double dragY;

    public Editor() {
        pane = new EditorPane();
        MenuBar bar = new MenuBar();

        Menu runMenu = new Menu("Run");
        MenuItem runItem = new MenuItem("Run");
        runItem.setOnAction(actionEvent -> {

            Stage stage = new Stage();

            boolean[][] cells = pane.getCells(offsetLine, offsetColumn, viewerNbCellsPerLine);

            Viewer viewer = new Viewer(viewerCellsWidth, cells, fps);
            Scene scene = new Scene(viewer);
            // Add the Scene to the Stage
            stage.setScene(scene);
            // Set the Title of the Stage
            stage.setTitle("FXGol Viewer");
            // Display the Stage
            stage.show();
            viewer.requestFocus();
        });
        MenuItem settingsItem = new MenuItem("Run settings");
        settingsItem.setOnAction(actionEvent -> {
            Dialog<Settings> dialog = Settings.getDialog(
                    this.offsetLine,
                    this.offsetColumn,
                    this.viewerNbCellsPerLine,
                    this.viewerCellsWidth,
                    this.fps
                    );
            Optional<Settings> result = dialog.showAndWait();

            if(result.isPresent()){
                Settings settings = result.get();

                if(settings.offsetLine != -1)
                    this.offsetLine = settings.offsetLine;

                if(settings.offsetColumn != -1)
                    this.offsetColumn = settings.offsetColumn;

                if(settings.viewerNbCellsPerLine != -1)
                    this.viewerNbCellsPerLine = settings.viewerNbCellsPerLine;

                if(settings.viewerCellsWidth != -1)
                    this.viewerCellsWidth = settings.viewerCellsWidth;

                if(settings.fps != -1)
                    this.fps = settings.fps;
            }
        });
        runMenu.getItems().addAll(runItem, settingsItem);
        bar.getMenus().add(runMenu);

        this.setOnMouseDragged(event -> {
            if(event.isStillSincePress())
                return;

            double x = event.getX();
            double y = event.getY();
            System.out.println("Drag : "+ event.getX() + " " + event.getY());
            if(dragX == null){
                dragX = x;
                dragY = y;
            }
            else {
                this.translate(x - dragX, y - dragY);
                dragX = x;
                dragY = y;
            }
            event.consume();
        });

        this.setOnMousePressed(event -> {
            System.out.println("Press : " + event.getX() + " " + event.getY());
            this.dragX = event.getX();
            this.dragY = event.getY();
        });

        this.setMinWidth(Params.DEFAULT_NB_CELLS_PER_LINE * Params.DEFAULT_CELLS_WIDTH);
        this.setMinHeight(Params.DEFAULT_NB_CELLS_PER_LINE * Params.DEFAULT_CELLS_WIDTH);
        pane.setTranslateX(Params.DEFAULT_NB_CELLS_PER_LINE * Params.DEFAULT_CELLS_WIDTH / 2D);
        pane.setTranslateY(Params.DEFAULT_NB_CELLS_PER_LINE * Params.DEFAULT_CELLS_WIDTH / 2D);

        statusBar = new StatusBar();
        statusBar.setPrefHeight(Params.STATUSBAR_PREF_HEIGHT);
        clearStatusBarText();

        statusBarOffsetLabel = new Label();
        this.setStatusBarOriginCoordinates(0, 0);

        statusBarOffsetLabel.setMinHeight(statusBar.getPrefHeight());
        statusBarOffsetLabel.setMaxHeight(statusBar.getPrefHeight());
        statusBarOffsetLabel.setAlignment(Pos.CENTER);

        statusBar.getLeftItems().add(statusBarOffsetLabel);
        statusBar.getLeftItems().add(new Separator(Orientation.VERTICAL));

        this.setCenter(pane);
        this.setTop(bar);
        this.setBottom(statusBar);
    }

    public void clearStatusBarText(){
        statusBar.setText("");
    }

    public void setStatusBarCoordinates(int line, int column){
        statusBar.setText("Current : (" + line + ", " + column + ")");
    }

    public void setStatusBarOriginCoordinates(int line, int column){
        statusBarOffsetLabel.setText("Origin: (" + line + ", " + column + ")");
    }

    private void translate(double dx, double dy) {
        pane.translate(dx, dy);
    }

}
