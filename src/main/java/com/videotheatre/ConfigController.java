package com.videotheatre;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ConfigController implements Initializable {
    File previousDir;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            var loaded = Settings.loadSettingsFromFile();

            if (loaded) {
                var videoDirStr = "";
                for (var str : Settings.videoDirectories) {
                    videoDirStr += str + "\n";
                }

                txtDirectories.setText(videoDirStr);
                optVideoModeNewVideo.setSelected(!Settings.loopVideo);
                spinnerRows.getValueFactory().setValue(Settings.rowCount);
                spinnerColumns.getValueFactory().setValue(Settings.columnCount);
                chkBoxStretchVideo.setSelected(Settings.stretchVideoToGrid);
                chkRemoveWatchedItems.setSelected(Settings.removeWatchedVideosFromList);
            }
        } catch (Exception ex) {
            // TODO
            System.err.println(ex);
        }
    }

    @FXML
    public void buttonSelectDirectory_clicked(ActionEvent event) {
        var stage = ((Stage)(((Button)event.getSource()).getScene().getWindow()));

        var directoryChooser = new DirectoryChooser();
        if (previousDir != null) {
            directoryChooser.setInitialDirectory(previousDir);
        }
        var selectedDirectory = directoryChooser.showDialog(stage);
        txtDirectories.setText(txtDirectories.getText() + "\n" + selectedDirectory.getAbsolutePath());

        previousDir = selectedDirectory.getParentFile();
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

        try {
            Settings.saveSettingsToFile();
        } catch (Exception ex) {
            System.err.println(ex);
        }

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
