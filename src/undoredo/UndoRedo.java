package undoredo;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class UndoRedo {

    private LinkedList<UndoRedoAction> actions;
    private ListIterator<UndoRedoAction> iterator;

    public UndoRedo() {
        actions = new LinkedList<>();
        this.iterator = actions.listIterator();
    }

    public void add(UndoRedoAction action){
        while(this.iterator.hasNext()){
            this.iterator.next();
            this.iterator.remove();
        }
        this.iterator.add(action);
        action.redo();
    }

    public void undo() throws NoSuchElementException {
        UndoRedoAction action = this.iterator.previous();
        action.undo();
    }

    public void redo() throws NoSuchElementException {
        UndoRedoAction action = this.iterator.next();
        action.redo();
    }

}
