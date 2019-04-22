package editor;

import undoredo.UndoRedoAction;
import java.util.LinkedList;

class AddRemoveCirclesAction implements UndoRedoAction {

    private EditorPane pane;
    private LinkedList<Coords> addedCircles;
    private LinkedList<Coords> removedCircles;

    public AddRemoveCirclesAction(EditorPane pane, LinkedList<Coords> addedCircles, LinkedList<Coords> removedCircles) {
        this.pane = pane;
        this.addedCircles = addedCircles;
        this.removedCircles = removedCircles;
    }

    @Override
    public void redo() {
        for(Coords coords : addedCircles)
            pane.addCircle(coords.line, coords.column);
        for(Coords coords : removedCircles)
            pane.removeCircle(coords.line, coords.column);
    }

    @Override
    public void undo() {
        for(Coords coords : addedCircles)
            pane.removeCircle(coords.line, coords.column);
        for(Coords coords : removedCircles)
            pane.addCircle(coords.line, coords.column);
    }
}

class Coords {
    int line;
    int column;

    public Coords(int line, int column) {
        this.line = line;
        this.column = column;
    }
}
