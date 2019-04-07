/*
 * Copyright (c) 2018 Dimitri Watel
 */

package editor;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

class Settings {

    Boolean infiniteSize;
    Boolean allCells;
    Integer offsetLine;
    Integer offsetColumn;
    Integer nbCellsPerLine;
    Integer nbCellsPerColumn;
    Integer cellsWidth;
    Double fps;

    public Settings(Boolean infiniteSize, Boolean allCells,
                    Integer offsetLine, Integer offsetColumn,
                    Integer nbCellsPerLine, Integer nbCellsPerColumn,
                    Integer cellsWidth, Double fps) {
        this.infiniteSize = infiniteSize;
        this.allCells = allCells;
        this.offsetLine = offsetLine;
        this.offsetColumn = offsetColumn;
        this.nbCellsPerLine = nbCellsPerLine;
        this.nbCellsPerColumn = nbCellsPerColumn;
        this.cellsWidth = cellsWidth;
        this.fps = fps;
    }

    static Dialog<Settings> getDialog(
            boolean infiniteGrid, boolean allCells,
            int offsetLine, int offsetColumn,
            int nbCellsPerLine, int nbCellsPerColumn,
            int cellsWidth, double fps) {
        Dialog<Settings> dialog = new Dialog<Settings>();

        dialog.setTitle("Viewer Options");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();

        GridPane grid = new GridPane();

        Label allCellsLabel = new Label("Viewer contains all cells");
        allCellsLabel.setAlignment(Pos.CENTER_LEFT);
        CheckBox allCellsCheckBox = new CheckBox();

        Label infiniteGridLabel = new Label("Viewer has infinite size");
        infiniteGridLabel.setAlignment(Pos.CENTER_LEFT);
        CheckBox infiniteGridCheckBox = new CheckBox();

        Label offsetLineLabel = new Label("Upper line of Viewer : ");
        offsetLineLabel.setAlignment(Pos.CENTER_LEFT);
        TextField offsetLineTextField = new TextField(String.valueOf(offsetLine));

        Label offsetColumnLabel = new Label("Leftmost column of Viewer : ");
        offsetColumnLabel.setAlignment(Pos.CENTER_LEFT);
        TextField offsetColumnTextField = new TextField(String.valueOf(offsetColumn));

        Label nbCellsPerLineLabel = new Label("Nb cells per line of Viewer : ");
        nbCellsPerLineLabel.setAlignment(Pos.CENTER_LEFT);
        TextField nbCellsPerLineTextField = new TextField(String.valueOf(nbCellsPerLine));

        Label nbCellsPerColumnLabel = new Label("Nb cells per column of Viewer : ");
        nbCellsPerColumnLabel.setAlignment(Pos.CENTER_LEFT);
        TextField nbCellsPerColumnTextField = new TextField(String.valueOf(nbCellsPerColumn));

        Label cellsWidthLabel = new Label("Width of cells of Viewer : ");
        cellsWidthLabel.setAlignment(Pos.CENTER_LEFT);
        TextField cellsWidthTextField = new TextField(String.valueOf(cellsWidth));

        Label fpsLabel = new Label("Frame per second of Viewer : ");
        fpsLabel.setAlignment(Pos.CENTER_LEFT);
        TextField fpsTextField = new TextField(String.valueOf(fps));

        Node[] toAdd = {
                infiniteGridLabel, infiniteGridCheckBox,
                allCellsLabel, allCellsCheckBox,
                offsetLineLabel, offsetLineTextField,
                offsetColumnLabel, offsetColumnTextField,
                nbCellsPerLineLabel, nbCellsPerLineTextField,
                nbCellsPerColumnLabel, nbCellsPerColumnTextField,
                cellsWidthLabel, cellsWidthTextField,
                fpsLabel, fpsTextField};

        for(int i = 0; i < toAdd.length; i++){
            Node child = toAdd[i];
            grid.add(child, i % 2, i / 2);
        }

        infiniteGridCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            offsetLineTextField.setDisable(newValue || allCellsCheckBox.isSelected());
            offsetColumnTextField.setDisable(newValue || allCellsCheckBox.isSelected());
            nbCellsPerLineTextField.setDisable(newValue || allCellsCheckBox.isSelected());
            nbCellsPerColumnTextField.setDisable(newValue || allCellsCheckBox.isSelected());
            allCellsCheckBox.setDisable(newValue);
        });

        allCellsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            offsetLineTextField.setDisable(newValue);
            offsetColumnTextField.setDisable(newValue);
            nbCellsPerLineTextField.setDisable(newValue);
            nbCellsPerColumnTextField.setDisable(newValue);
        });

        allCellsCheckBox.setSelected(allCells);
        infiniteGridCheckBox.setSelected(infiniteGrid);

        offsetLineTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("-?\\d*"))
                        offsetLineTextField.setText(old);
                }
        );

        offsetColumnTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("-?\\d*"))
                        offsetColumnTextField.setText(old);
                }
        );

        nbCellsPerLineTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("\\d*"))
                        nbCellsPerLineTextField.setText(old);
                }
        );

        nbCellsPerColumnTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("\\d*"))
                        nbCellsPerColumnTextField.setText(old);
                }
        );

        cellsWidthTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("\\d*"))
                        cellsWidthTextField.setText(old);
                }
        );

        fpsTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("\\d*\\.?\\d*"))
                        fpsTextField.setText(old);
                }
        );


        dialogPane.setContent(grid);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if(buttonType == ButtonType.OK){
                Boolean nInfiniteSize = infiniteGridCheckBox.isSelected();
                Boolean nAllCells = allCellsCheckBox.isSelected();

                Integer nOffsetLine = null;
                Integer nOffsetColumn = null;
                Integer nNbCellsPerLine = null;
                Integer nNbCellsPerColumn = null;
                Integer nCellsWidth = null;
                Double nFps = null;

                if(!offsetLineTextField.getText().equals(""))
                    nOffsetLine = Integer.valueOf(offsetLineTextField.getText());

                if(!offsetColumnTextField.getText().equals(""))
                    nOffsetColumn = Integer.valueOf(offsetColumnTextField.getText());

                if(!nbCellsPerLineTextField.getText().equals(""))
                    nNbCellsPerLine = Integer.valueOf(nbCellsPerLineTextField.getText());

                if(!nbCellsPerColumnTextField.getText().equals(""))
                    nNbCellsPerColumn = Integer.valueOf(nbCellsPerColumnTextField.getText());

                if(!cellsWidthTextField.getText().equals(""))
                    nCellsWidth = Integer.valueOf(cellsWidthTextField.getText());

                if(!fpsTextField.getText().equals(""))
                    nFps = Double.valueOf(fpsTextField.getText());

                return new Settings(
                        nInfiniteSize, nAllCells,
                        nOffsetLine, nOffsetColumn,
                        nNbCellsPerLine, nNbCellsPerColumn,
                        nCellsWidth, nFps);
            }
            return null;
        });

        return dialog;
    }
}
