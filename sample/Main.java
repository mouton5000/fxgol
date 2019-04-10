package sample;

import editor.Editor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import viewer.view.Viewer;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{

        Editor editor = new Editor();

        Scene scene = new Scene(editor);
        // Add the Scene to the Stage
        stage.setScene(scene);
        // Set the Title of the Stage
        stage.setTitle("FXGol Editor");
        // Display the Stage
        stage.show();
        editor.requestFocus();

    }
    public static void main(String[] args) {
        launch(args);
    }
}
