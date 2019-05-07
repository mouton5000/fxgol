package editor;

import undoredo.UndoRedoAction;

class CompositeAction implements UndoRedoAction {

    UndoRedoAction[] actions;

    public CompositeAction(UndoRedoAction... actions) {
        this.actions = actions;
    }

    @Override
    public void redo() {
        for(UndoRedoAction action: actions)
            action.redo();
    }

    @Override
    public void undo() {
        for(UndoRedoAction action: actions)
            action.undo();
    }
}
