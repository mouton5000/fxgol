package editor;

import editor.global.Params;
import javafx.scene.Group;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class Editor extends Group {

    public Editor() {
        EditorPane pane = new EditorPane();
        MenuBar bar = new MenuBar();

        Menu runMenu = new Menu("Run");
        MenuItem runItem = new MenuItem("Run");
        runItem.setOnAction(actionEvent -> {

        });
        MenuItem settingsItem = new MenuItem("Run settings");
        settingsItem.setOnAction(actionEvent -> {

        });
        runMenu.getItems().addAll(runItem, settingsItem);
        bar.getMenus().add(runMenu);


        pane.setPrefSize(Params.WIDTH * Params.SIZE, Params.WIDTH * Params.SIZE);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(bar, pane);
        this.getChildren().add(vbox);
    }
}
