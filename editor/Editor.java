package editor;

import editor.global.Params;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import undoredo.UndoRedo;
import viewer.view.Viewer;

import java.util.NoSuchElementException;
import java.util.Optional;

public class Editor extends BorderPane {

    private boolean viewerInfiniteSize = true;
    private boolean viewerAllCells = false;
    private int viewerOffsetLine = 0;
    private int viewerOffsetColumn = 0;
    private int viewerNbCellsPerLine = Params.DEFAULT_NB_CELLS_PER_LINE;
    private int viewerNbCellsPerColumn = Params.DEFAULT_NB_CELLS_PER_COLUMN;
    private int viewerCellsWidth = Params.DEFAULT_CELLS_WIDTH;
    private double vierwerFps = Params.DEFAULT_FPS;

    private EditorPane pane;

    private StatusBar statusBar;

    private EditorMenu editorMenu;

    private Double dragX;
    private Double dragY;

    private UndoRedo undoRedo;

    public Editor() {

        undoRedo = new UndoRedo();
        pane = new EditorPane(undoRedo);
        MenuBar bar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newItem.setOnAction(actionEvent -> {
            pane.clear();
            this.setTitle(null);
        });
        MenuItem openItem = new MenuItem("Open");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        openItem.setOnAction(actionEvent -> {
            String filename = pane.open();
            this.setTitle(filename);
        });
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(actionEvent -> {
            String filename = pane.save();
            this.setTitle(filename);
        });
        MenuItem saveAsItem = new MenuItem("SaveAs");
        saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        saveAsItem.setOnAction(actionEvent -> {
            String filename = pane.saveAs();
            this.setTitle(filename);
        });
        MenuItem importItem = new MenuItem("Import");
        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, importItem);

        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        undoItem.setOnAction(actionEvent -> {
            try {
                undoRedo.undo();
            }
            catch (NoSuchElementException ignored){
            }
        });

        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        redoItem.setOnAction(actionEvent -> {
            try {
                undoRedo.redo();
            }
            catch (NoSuchElementException ignored){
            }
        });
        editMenu.getItems().addAll(undoItem, redoItem);

        Menu runMenu = new Menu("Run");
        MenuItem runItem = new MenuItem("Run");
        runItem.setAccelerator(new KeyCodeCombination(KeyCode.F9));
        runItem.setOnAction(actionEvent -> {

            Stage stage = new Stage();

            boolean[][] cells = pane.getCells(
                    viewerInfiniteSize, viewerAllCells,
                    viewerOffsetLine, viewerOffsetColumn,
                    viewerNbCellsPerLine, viewerNbCellsPerColumn);

            Viewer viewer = new Viewer(viewerCellsWidth, cells, viewerInfiniteSize, vierwerFps);
            Scene scene = new Scene(viewer);
            // Add the Scene to the Stage
            stage.setScene(scene);
            // Set the Title of the Stage
            stage.setTitle("FXGol Viewer");
            // Display the Stage

            stage.show();

            scene.widthProperty().addListener((observable, oldValue, newValue) -> viewer.setWidth(newValue.doubleValue()));
            scene.heightProperty().addListener((observable, oldValue, newValue) -> viewer.setHeight(newValue.doubleValue()));


            viewer.requestFocus();
        });

        MenuItem settingsItem = new MenuItem("Run settings");
        settingsItem.setOnAction(actionEvent -> {
            Dialog<Settings> dialog = Settings.getDialog(
                    this.viewerInfiniteSize,
                    this.viewerAllCells,
                    this.viewerOffsetLine,
                    this.viewerOffsetColumn,
                    this.viewerNbCellsPerLine,
                    this.viewerNbCellsPerColumn,
                    this.viewerCellsWidth,
                    this.vierwerFps
                    );
            Optional<Settings> result = dialog.showAndWait();

            if(result.isPresent()){
                Settings settings = result.get();

                if(settings.infiniteSize != null)
                    this.viewerInfiniteSize = settings.infiniteSize;

                if(settings.allCells != null)
                    this.viewerAllCells = settings.allCells;

                if(settings.offsetLine != null)
                    this.viewerOffsetLine = settings.offsetLine;

                if(settings.offsetColumn != null)
                    this.viewerOffsetColumn = settings.offsetColumn;

                if(settings.nbCellsPerLine != null)
                    this.viewerNbCellsPerLine = settings.nbCellsPerLine;

                if(settings.nbCellsPerColumn != null)
                    this.viewerNbCellsPerColumn = settings.nbCellsPerColumn;

                if(settings.cellsWidth != null)
                    this.viewerCellsWidth = settings.cellsWidth;

                if(settings.fps != null)
                    this.vierwerFps = settings.fps;
            }
        });
        runMenu.getItems().addAll(runItem, settingsItem);

        Menu selectionMenu = new Menu("Selection");

        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        cutItem.setOnAction(event -> this.pane.cutSelection());

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        copyItem.setOnAction(event -> this.pane.copySelection());

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        pasteItem.setOnAction(event -> this.pane.displayClipboardSelection());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        deleteItem.setOnAction(event -> this.pane.removeSelection());

        MenuItem xMirrorItem = new MenuItem("Mirror (horizontally)");
        xMirrorItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        xMirrorItem.setOnAction(event -> this.pane.xMirrorSelection());
        MenuItem yMirrorItem = new MenuItem("Mirror (vertically)");
        yMirrorItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        yMirrorItem.setOnAction(event -> this.pane.yMirrorSelection());
        MenuItem rotateItem = new MenuItem("Rotate (90 Clockwize)");
        rotateItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
        rotateItem.setOnAction(event -> this.pane.rotateSelection());
        MenuItem oneStepItem = new MenuItem("Step one generation");
        oneStepItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
        oneStepItem.setOnAction(event -> this.pane.stepSelection());

        selectionMenu.getItems().addAll(cutItem, copyItem, pasteItem, deleteItem, new SeparatorMenuItem(), xMirrorItem, yMirrorItem, rotateItem, oneStepItem);
        bar.getMenus().addAll(fileMenu, editMenu, runMenu, selectionMenu);

        this.setOnMouseDragged(event -> {
            if(event.isStillSincePress())
                return;

            if(event.getButton() != MouseButton.SECONDARY)
                return;

            double x = event.getX();
            double y = event.getY();
            if(dragX == null){
                dragX = x;
                dragY = y;
            }
            else {
                this.translate(x - dragX, y - dragY);
                dragX = x;
                dragY = y;
            }
            event.consume();
        });

        this.setOnMousePressed(event -> {
            if(event.getButton() != MouseButton.SECONDARY)
                return;

            this.dragX = event.getX();
            this.dragY = event.getY();

            event.consume();
        });

        statusBar = new StatusBar();
        statusBar.setPrefHeight(Params.STATUSBAR_PREF_HEIGHT);
        clearStatusBarText();

        editorMenu = new EditorMenu(pane);


        this.setMinWidth(Params.DEFAULT_NB_CELLS_PER_LINE * Params.DEFAULT_CELLS_WIDTH);
        this.setMinHeight(Params.DEFAULT_NB_CELLS_PER_LINE * Params.DEFAULT_CELLS_WIDTH);

        this.setCenter(pane);
        this.setTop(bar);
        this.setBottom(statusBar);
        this.setLeft(editorMenu);

        setSelectionEraseMode(false);
    }

    public void clearStatusBarText(){
        statusBar.setText("");
    }

    public void setStatusBarCoordinates(int line, int column){
        statusBar.setText("Current : (" + line + ", " + column + ")");
    }

    private void translate(double dx, double dy) {
        pane.translate(dx, dy);
    }

    private void setTitle(String title){
        if(title == null)
            title = "";
        ((Stage)this.getScene().getWindow()).setTitle(title);
    }

    void setSelectionEraseMode(boolean selectionEraseMode) {
        pane.setSelectionEraseMode(selectionEraseMode);
        editorMenu.setSelectionEraseMode(selectionEraseMode);
    }
}
