/*
 * Copyright (c) 2018 Dimitri Watel
 */

package editor;

import editor.global.Params;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Arrays;
import java.util.List;

class Settings {

    int offsetLine;
    int offsetColumn;
    int viewerNbCellsPerLine;
    int viewerCellsWidth;
    double fps;

    public Settings(int offsetLine, int offsetColumn, int viewerNbCellsPerLine, int viewerCellsWidth, double fps) {
        this.offsetLine = offsetLine;
        this.offsetColumn = offsetColumn;
        this.viewerNbCellsPerLine = viewerNbCellsPerLine;
        this.viewerCellsWidth = viewerCellsWidth;
        this.fps = fps;
    }

    static Dialog<Settings> getDialog(
            int offsetLine, int offsetColumn, int viewerNbCellsPerLine, int viewerCellsWidth, double fps) {
        Dialog<Settings> dialog = new Dialog<Settings>();

        dialog.setTitle("Viewer Options");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();

        VBox vbox = new VBox();

        HBox offsetLineHBox = new HBox();
        Label offsetLineLabel = new Label("Upper line of Viewer : ");
        offsetLineLabel.setAlignment(Pos.CENTER_LEFT);
        TextField offsetLineTextField = new TextField(String.valueOf(offsetLine));

        offsetLineTextField.textProperty().addListener((observableValue, old, value) -> {
            if(!value.matches("\\d*"))
                offsetLineTextField.setText(old);
            }
        );
        offsetLineHBox.getChildren().addAll(offsetLineLabel, offsetLineTextField);

        HBox offsetColumnHBox = new HBox();
        Label offsetColumnLabel = new Label("Leftmost column of Viewer : ");
        offsetColumnLabel.setAlignment(Pos.CENTER_LEFT);
        TextField offsetColumnTextField = new TextField(String.valueOf(offsetColumn));

        offsetColumnTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("\\d*"))
                        offsetColumnTextField.setText(old);
                }
        );
        offsetColumnHBox.getChildren().addAll(offsetColumnLabel, offsetColumnTextField);

        HBox viewerNbCellsPerLineHBox = new HBox();
        Label viewerNbCellsPerLineLabel = new Label("Nb cells per line of Viewer : ");
        viewerNbCellsPerLineLabel.setAlignment(Pos.CENTER_LEFT);
        TextField viewerNbCellsPerLineTextField = new TextField(String.valueOf(viewerNbCellsPerLine));

        viewerNbCellsPerLineTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("\\d*"))
                        viewerNbCellsPerLineTextField.setText(old);
                }
        );
        viewerNbCellsPerLineHBox.getChildren().addAll(viewerNbCellsPerLineLabel, viewerNbCellsPerLineTextField);


        HBox viewerCellsWidthHBox = new HBox();
        Label viewerCellsWidthLabel = new Label("Width of cells of Viewer : ");
        viewerCellsWidthLabel.setAlignment(Pos.CENTER_LEFT);
        TextField viewerCellsWidthTextField = new TextField(String.valueOf(viewerCellsWidth));

        viewerCellsWidthTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("\\d*"))
                        viewerCellsWidthTextField.setText(old);
                }
        );
        viewerCellsWidthHBox.getChildren().addAll(viewerCellsWidthLabel, viewerCellsWidthTextField);


        HBox fpsHBox = new HBox();
        Label fpsLabel = new Label("Frame per second of Viewer : ");
        fpsLabel.setAlignment(Pos.CENTER_LEFT);
        TextField fpsTextField = new TextField(String.valueOf(fps));

        fpsTextField.textProperty().addListener((observableValue, old, value) -> {
                    if(!value.matches("\\d*\\.?\\d*"))
                        fpsTextField.setText(old);
                }
        );
        fpsHBox.getChildren().addAll(fpsLabel, fpsTextField);

        vbox.getChildren().addAll(
                offsetLineHBox,
                offsetColumnHBox,
                viewerNbCellsPerLineHBox,
                viewerCellsWidthHBox,
                fpsHBox);

        dialogPane.setContent(vbox);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if(buttonType == ButtonType.OK){
                int nOffsetLine = -1;
                int nOffsetColumn = -1;
                int nViewerNbCellsPerLine = -1;
                int nViewerCellsWidth = -1;
                double nFps = -1;

                if(!offsetLineTextField.getText().equals(""))
                    nOffsetLine = Integer.valueOf(offsetLineTextField.getText());

                if(!offsetColumnTextField.getText().equals(""))
                    nOffsetColumn = Integer.valueOf(offsetColumnTextField.getText());

                if(!viewerNbCellsPerLineTextField.getText().equals(""))
                    nViewerNbCellsPerLine = Integer.valueOf(viewerNbCellsPerLineTextField.getText());

                if(!viewerCellsWidthTextField.getText().equals(""))
                    nViewerCellsWidth = Integer.valueOf(viewerCellsWidthTextField.getText());

                if(!fpsTextField.getText().equals(""))
                    nFps = Double.valueOf(fpsTextField.getText());

                return new Settings(nOffsetLine, nOffsetColumn, nViewerNbCellsPerLine, nViewerCellsWidth, nFps);
            }
            return null;
        });

        return dialog;
    }
}
