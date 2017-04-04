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
 * Controller for settings dialog
 */
public class SettingsController extends BaseController {
    @FXML
    private VBox vbMain;
    @FXML
    private Button btBrowse;
    @FXML
    private TextField tfFileChooser;
    @FXML
    private ComboBox<Theme> cbThemeChooser;
    private File chosenFile;
    private Theme chosenTheme;

    @FXML
    public void initialize() {
        tfFileChooser.setText(manager.getDatabaseFile().getAbsolutePath());
        chosenFile = manager.getDatabaseFile();
        cbThemeChooser.getItems().addAll(Theme.values());
        cbThemeChooser.getSelectionModel().selectedItemProperty().addListener((observableValue, theme, t1) -> {
            chosenTheme = t1;
            loadTheme(chosenTheme, cbThemeChooser.getScene());
        });
        cbThemeChooser.getSelectionModel().select(theme);
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
            manager.setDatabaseFile(chosenFile);
        }
        if (chosenTheme != null) {
            setTheme(chosenTheme);
            manager.getSettings().setProperty("theme", theme.toString());
        }
        ((Node) (event.getSource())).getScene().getWindow().hide();
    }
}
