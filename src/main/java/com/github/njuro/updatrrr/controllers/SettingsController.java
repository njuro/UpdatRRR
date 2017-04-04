package com.github.njuro.updatrrr.controllers;

import com.github.njuro.updatrrr.Theme;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Event controller for settings dialog of UpdatRRR GUI
 *
 * @author njuro
 */
public class SettingsController extends BaseController {

    @FXML
    private VBox vbMain;

    @FXML
    private Button btBrowse;

    @FXML
    private TextField tfDatabaseFile;

    @FXML
    private ComboBox<Theme> cbThemes;

    private File chosenFile;
    private Theme chosenTheme;

    @FXML
    public void initialize() {
        tfDatabaseFile.setText(manager.getDatabaseFile().getAbsolutePath());
        chosenFile = manager.getDatabaseFile();
        cbThemes.getItems().addAll(Theme.values());
        cbThemes.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTheme, newTheme) -> {
            chosenTheme = newTheme;
            loadTheme(chosenTheme, cbThemes.getScene());
        });
        cbThemes.getSelectionModel().select(theme);
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
            tfDatabaseFile.setText(chosenFile.getAbsolutePath());
        }
    }

    @FXML
    private void closeSettings(ActionEvent event) {
        ((Node) (event.getSource())).getScene().getWindow().hide();
    }

    @FXML
    private void saveSettings(ActionEvent event) {
        if (chosenFile != null) {
            manager.setDatabaseFile(chosenFile);
        }
        if (chosenTheme != null) {
            setTheme(chosenTheme);
        }
        ((Node) (event.getSource())).getScene().getWindow().hide();
    }
}
