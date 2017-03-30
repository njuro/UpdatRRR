package com.github.njuro.updatrrr.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller for settings dialog
 */
public class SettingsController extends BaseController {
    @FXML
    private Button btBrowse;
    @FXML
    private TextField tfFileChooser;

    private File chosenFile;


    @FXML
    private void initialize() {
        tfFileChooser.setText(manager.getDbFile().getAbsolutePath());
        chosenFile = manager.getDbFile();
    }

    @FXML
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a StylRRR database");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("StylRRR database file", "stylRRR_DB.json"),
                new FileChooser.ExtensionFilter("JSON file", "*.json"),
                new FileChooser.ExtensionFilter("Any file", "*.*")
        );
        chosenFile = fileChooser.showOpenDialog(btBrowse.getScene().getWindow());
        if (chosenFile != null) {
            tfFileChooser.setText(chosenFile.getAbsolutePath());
        }
    }

    @FXML
    private void btCloseSettings(ActionEvent event) {
        ((Node) (event.getSource())).getScene().getWindow().hide();
    }

    @FXML
    private void btSaveSettings(ActionEvent event) {
        if (chosenFile != null) {
            manager.setDbFile(chosenFile);
        }
        ((Node) (event.getSource())).getScene().getWindow().hide();
    }
}
