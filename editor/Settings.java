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

        Label leftLimitLabel = new Label("Leftmost column of Viewer : ");
        leftLimitLabel.setAlignment(Pos.CENTER_LEFT);
        TextField leftLimitTextField = new TextField(String.valueOf(offsetColumn));

        Label rightLimitLabel = new Label("Rightmost column of Viewer : ");
        rightLimitLabel.setAlignment(Pos.CENTER_LEFT);
        TextField rightLimitTextField = new TextField(String.valueOf(offsetColumn + nbCellsPerLine - 1));

        Label topLimitLabel = new Label("Upper line of Viewer : ");
        topLimitLabel.setAlignment(Pos.CENTER_LEFT);
        TextField topLimitTextField = new TextField(String.valueOf(offsetLine));

        Label bottomLimitLabel = new Label("Lowermost line of Viewer : ");
        bottomLimitLabel.setAlignment(Pos.CENTER_LEFT);
        TextField bottomLimitTextField = new TextField(String.valueOf(offsetLine + nbCellsPerColumn - 1));

        Label cellsWidthLabel = new Label("Width of cells of Viewer : ");
        cellsWidthLabel.setAlignment(Pos.CENTER_LEFT);
        TextField cellsWidthTextField = new TextField(String.valueOf(cellsWidth));

        Label fpsLabel = new Label("Frame per second of Viewer : ");
        fpsLabel.setAlignment(Pos.CENTER_LEFT);
        TextField fpsTextField = new TextField(String.valueOf(fps));

        Node[] toAdd = {
                infiniteGridLabel, infiniteGridCheckBox,
                allCellsLabel, allCellsCheckBox,
                leftLimitLabel, leftLimitTextField,
                rightLimitLabel, rightLimitTextField,
                topLimitLabel, topLimitTextField,
                bottomLimitLabel, bottomLimitTextField,
                cellsWidthLabel, cellsWidthTextField,
                fpsLabel, fpsTextField};

        for(int i = 0; i < toAdd.length; i++){
            Node child = toAdd[i];
            grid.add(child, i % 2, i / 2);
        }

        infiniteGridCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            topLimitTextField.setDisable(newValue || allCellsCheckBox.isSelected());
            leftLimitTextField.setDisable(newValue || allCellsCheckBox.isSelected());
            rightLimitTextField.setDisable(newValue || allCellsCheckBox.isSelected());
            bottomLimitTextField.setDisable(newValue || allCellsCheckBox.isSelected());
            allCellsCheckBox.setDisable(newValue);
        });

        allCellsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            topLimitTextField.setDisable(newValue);
            leftLimitTextField.setDisable(newValue);
            rightLimitTextField.setDisable(newValue);
            bottomLimitTextField.setDisable(newValue);
        });

        allCellsCheckBox.setSelected(allCells);
        infiniteGridCheckBox.setSelected(infiniteGrid);

        topLimitTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("-?\\d*"))
                        topLimitTextField.setText(old);
                }
        );

        leftLimitTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("-?\\d*"))
                        leftLimitTextField.setText(old);
                }
        );

        rightLimitTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("-?\\d*"))
                        rightLimitTextField.setText(old);
                }
        );

        bottomLimitTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("-?\\d*"))
                        bottomLimitTextField.setText(old);
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

                if(!leftLimitTextField.getText().equals(""))
                    nOffsetColumn = Integer.valueOf(leftLimitTextField.getText());

                if(!rightLimitTextField.getText().equals(""))
                    nNbCellsPerLine = Integer.valueOf(rightLimitTextField.getText()) - (nOffsetColumn == null ? offsetColumn : nOffsetColumn) + 1;

                if(!topLimitTextField.getText().equals(""))
                    nOffsetLine = Integer.valueOf(topLimitTextField.getText());

                if(!bottomLimitTextField.getText().equals(""))
                    nNbCellsPerColumn = Integer.valueOf(bottomLimitTextField.getText()) - (nOffsetLine == null ? offsetLine : nOffsetLine) + 1;

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
