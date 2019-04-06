package editor;

import editor.global.Params;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import viewer.view.Viewer;

import java.util.Optional;

public class Editor extends Group {

    private int offsetLine = 0;
    private int offsetColumn = 0;
    private int viewerNbCellsPerLine = Params.DEFAULT_NB_CELLS_PER_LINE;
    private int viewerCellsWidth = Params.DEFAULT_CELLS_WIDTH;
    private double fps = Params.DEFAULT_FPS;

    public Editor() {
        EditorPane pane = new EditorPane();
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


        pane.setPrefSize(Params.DEFAULT_NB_CELLS_PER_LINE * Params.DEFAULT_CELLS_WIDTH, Params.DEFAULT_NB_CELLS_PER_LINE * Params.DEFAULT_CELLS_WIDTH);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(bar, pane);
        this.getChildren().add(vbox);
    }
}
