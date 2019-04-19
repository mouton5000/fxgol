package editor;

import editor.global.Params;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import util.Ressources;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class EditorMenu extends Group {

    public EditorMenu(EditorPane pane) {

        VBox vbox = new VBox();


        ChoiceBox<String> categories = new ChoiceBox<>();

        Callback<ListView<Pattern>, ListCell<Pattern>> patternCellFactory = param -> new PatternCell();
//        ChangeListener<Pattern> patternSelectedListener =
//                (observable, oldValue, newValue) -> {
//            boolean[][] cells = newValue.cells;
//            if(cells == null)
//                return;
//            pane.copyPattern(cells);
//        };


        ArrayList<ListView<Pattern>> lists = new ArrayList<>();

        File patternsDir = new File(Ressources.getRessource("patterns"));
        for(File file : patternsDir.listFiles()) {
            categories.getItems().add(file.getName());
            ListView<Pattern> list = new ListView<>();
            list.setCellFactory(patternCellFactory);
//            list.getSelectionModel().selectedItemProperty().addListener(patternSelectedListener);
            for(File patternFile : file.listFiles()){
                String name = patternFile.getName();
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                String description = null;
                if(patternFile.getName().endsWith(".rle")) {
                    name = name.substring(0, name.length() - 4);
                }

                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(patternFile));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = br.readLine()) != null)
                        sb.append(line + "\n");
                    description = sb.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Pattern pattern = new Pattern(pane, name, description);
                list.getItems().add(pattern);
            }
            lists.add(list);
        }

        vbox.getChildren().addAll(new Separator(Orientation.HORIZONTAL), categories, lists.get(0));

        categories.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Node> children = vbox.getChildren();
            children.remove(children.size() - 1);
            children.add(lists.get(newValue.intValue()));
        });
        categories.getSelectionModel().select(0);

        vbox.setMaxWidth(Params.EDITOR_LEFT_MENU_PREF_WIDTH);
        categories.setPrefWidth(Params.EDITOR_LEFT_MENU_PREF_WIDTH);

        this.getChildren().add(vbox);
    }


}