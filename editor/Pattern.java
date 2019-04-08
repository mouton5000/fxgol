package editor;

import javafx.scene.control.ListCell;

class Pattern{

    String name;
    String rleDescription;

    public Pattern(String name, String rleDescription) {
        this.name = name;
        this.rleDescription = rleDescription;
    }
}

class PatternCell extends ListCell<Pattern>{
    @Override
    protected void updateItem(Pattern item, boolean empty) {
        super.updateItem(item, empty);

        if(item != null && item.name != null)
            this.setText(item.name);
    }
}
