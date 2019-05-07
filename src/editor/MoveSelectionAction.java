package editor;

import undoredo.UndoRedoAction;
import util.Coords;

public class MoveSelectionAction implements UndoRedoAction {

    private Selection selection;
    private Coords srcCoords;
    private Coords destCoords;

    public MoveSelectionAction(Selection selection, Coords srcCoords, Coords destCoords) {
        this.selection = selection;
        this.srcCoords = srcCoords;
        this.destCoords = destCoords;
    }

    @Override
    public void redo() {
        selection.translate(destCoords.line - srcCoords.line, destCoords.column - srcCoords.column);
    }

    @Override
    public void undo() {
        selection.translate(srcCoords.line - destCoords.line, srcCoords.column - destCoords.column);
    }



}
