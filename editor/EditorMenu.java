package editor;

import editor.global.Params;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import util.Ressources;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class EditorMenu extends Group {

    ToggleButton not_erase;
    ToggleButton erase;

    public EditorMenu(EditorPane pane) {

        VBox vbox = new VBox();

        HBox hBox = new HBox();
        not_erase = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.PLUS_CIRCLE));
        erase = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS_CIRCLE));
        not_erase.setMinWidth(Params.EDITOR_LEFT_MENU_PREF_WIDTH / 2);
        not_erase.setOnAction(actionEvent -> {
            erase.setSelected(!not_erase.isSelected());
            ((Editor)this.getParent()).setSelectionEraseMode(!not_erase.isSelected());
        });
        not_erase.setTooltip(new Tooltip("Move a selection over an existing alive cell do not erase it."));
        erase.setMinWidth(Params.EDITOR_LEFT_MENU_PREF_WIDTH / 2);
        erase.setOnAction(actionEvent -> {
            not_erase.setSelected(!erase.isSelected());
            ((Editor)this.getParent()).setSelectionEraseMode(erase.isSelected());
        });
        erase.setTooltip(new Tooltip("Move a selection over an existing alive cell do erase it."));
        hBox.getChildren().addAll(not_erase, erase);

        ChoiceBox<String> categories = new ChoiceBox<>();

        Callback<ListView<Pattern>, ListCell<Pattern>> patternCellFactory = param -> new PatternCell();
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

        vbox.getChildren().addAll(hBox, new Separator(Orientation.HORIZONTAL), categories, lists.get(0));

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

    void setSelectionEraseMode(boolean selectionEraseMode) {
        if(selectionEraseMode)
            erase.setSelected(true);
        else
            not_erase.setSelected(true);
    }

}