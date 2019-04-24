package editor;

import undoredo.UndoRedoAction;
import util.Coords;

import java.util.List;

public class AddToSelectionAction implements UndoRedoAction {

    private EditorPane pane;
    private List<Coords> coordsList;
    private EditSelectionAction editSelectionAction;

    public AddToSelectionAction(EditorPane pane, List<Coords> coordsList, Selection current, Selection prev, Selection next) {
        this.pane = pane;
        this.coordsList = coordsList;
        this.editSelectionAction = new EditSelectionAction(current, prev, next);
    }

    @Override
    public void redo() {
        for(Coords coords : coordsList)
            pane.removeCircle(coords.line, coords.column);
        this.editSelectionAction.redo();
    }

    @Override
    public void undo() {
        for(Coords coords : coordsList)
            pane.addCircle(coords.line, coords.column);
        this.editSelectionAction.undo();
    }
}
