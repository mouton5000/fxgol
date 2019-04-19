package editor;

import com.sun.javafx.font.FontFactory;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.regex.Matcher;

class Pattern{

    EditorPane pane;
    String name;
    boolean[][] cells;

    public Pattern(EditorPane pane, String name, String rleDescription) {

        this.pane = pane;
        this.name = name;

        String[] lines = rleDescription.split("\\n");

        cells = null;

        int index;
        for(index = 0; index < lines.length; index++){
            String line = lines[index].trim();
            if(line.startsWith("#"))
                continue;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("x = (\\d+), y = (\\d+).*");
            Matcher m = p.matcher(line);
            if(m.matches()) {
                cells = new boolean[Integer.valueOf(m.group(2))][Integer.valueOf(m.group(1))];
                break;
            }

        }

        if(cells == null)
            return;

        int value = 0;
        int l = 0;
        int c = 0;
        for(int i = index + 1; i < lines.length; i++){
            String line = lines[i].trim();
            for(int j = 0; j < line.length(); j++){
                char ch = line.charAt(j);
                if(ch == 'b' || ch == 'o'){
                    if(value == 0)
                        value = 1;
                    if(ch == 'o') {
                        for (int k = c; k < c + value; k++)
                            cells[l][k] = true;
                    }
                    c += value;
                    value = 0;
                }
                else if(ch == '$'){
                    if(value == 0)
                        value = 1;
                    l += value;
                    c = 0;
                    value = 0;
                }
                else
                    value = 10 * value + Character.getNumericValue(ch);
            }
        }
    }
}

class PatternCell extends ListCell<Pattern>{

    @Override
    protected void updateItem(Pattern item, boolean empty) {
        super.updateItem(item, empty);

        if(item != null && item.name != null)
            this.setText(item.name);
        if(!empty) {
            Button pasteButton = new Button("P");
            this.setGraphic(pasteButton);
            if(item.cells != null)
                pasteButton.setOnAction(event -> {
                    item.pane.copyPattern(item.cells);
                });
        }

    }
}