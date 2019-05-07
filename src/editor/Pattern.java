package editor;

import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import util.RunLenghtEncodingTranslator;

class Pattern{

    EditorPane pane;
    String name;
    boolean[][] cells;

    public Pattern(EditorPane pane, String name, String rleDescription) {

        this.pane = pane;
        this.name = name;
        cells = RunLenghtEncodingTranslator.fromRLE(rleDescription);

    }
}

class PatternCell extends ListCell<Pattern>{

    @Override
    protected void updateItem(Pattern item, boolean empty) {
        super.updateItem(item, empty);

        if(item != null && item.name != null)
            this.setText(item.name);
        if(!empty) {
            Button pasteButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.PENCIL));
            this.setGraphic(pasteButton);
            pasteButton.setTooltip(new Tooltip("Copy this pattern to selection."));
            if(item.cells != null) {
                pasteButton.setOnAction(event -> {
                    item.pane.copyPattern(item.cells);
                });
            }
        }

    }
}