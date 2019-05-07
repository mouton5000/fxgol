package editor;

import undoredo.UndoRedoAction;

class EditSelectionAction implements UndoRedoAction {

    private Selection current;
    private Selection prev;
    private Selection next;

    public EditSelectionAction(Selection current, Selection prev, Selection next) {
        this.current = current;
        this.prev = prev;
        this.next = next;
    }

    @Override
    public void redo() {
        this.current.copy(this.next);
    }

    @Override
    public void undo() {
        this.current.copy(this.prev);
    }
}
