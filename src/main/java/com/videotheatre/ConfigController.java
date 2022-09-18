package com.videotheatre;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.util.List;

public class ConfigController {
    @FXML
    TextArea txtDirectories;
    @FXML
    RadioButton optVideoModeNewVideo;
    @FXML
    RadioButton optVideoModeLoop;
    @FXML
    Spinner<Integer> spinnerRows;
    @FXML
    Spinner<Integer> spinnerColumns;
    @FXML
    CheckBox chkBoxStretchVideo;
    @FXML
    CheckBox chkRemoveWatchedItems;

    @FXML
    public void buttonSelectDirectory_clicked(ActionEvent event) {
        var stage = ((Stage)(((Button)event.getSource()).getScene().getWindow()));

        var directoryChooser = new DirectoryChooser();
        var selectedDirectory = directoryChooser.showDialog(stage);
        txtDirectories.setText(txtDirectories.getText() + "\n" + selectedDirectory.getAbsolutePath());
    }

    @FXML
    public void buttonDirectoryClear_clicked(ActionEvent event) {
        txtDirectories.setText("");
    }
    @FXML
    public void buttonStart_clicked(ActionEvent event) {
        Settings.columnCount = spinnerColumns.getValue();
        Settings.rowCount = spinnerRows.getValue();
        Settings.loopVideo = optVideoModeLoop.isSelected();
        Settings.videoDirectories = List.of(txtDirectories.getText().split("\n"));
        Settings.stretchVideoToGrid = chkBoxStretchVideo.isSelected();
        Settings.removeWatchedVideosFromList = chkRemoveWatchedItems.isSelected();

        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();

        var stage = new Stage();
        stage.setTitle("Theatre");

        try {
            new TheatreController().render(stage);
        } catch (Exception ex) {
            // TBA
            System.err.println(ex.toString());
        }
    }
}
