package com.github.njuro.updatrrr.controllers;

import com.github.njuro.updatrrr.AlertBuilder;
import com.github.njuro.updatrrr.Style;
import com.github.njuro.updatrrr.UpdatRRR_GUI;
import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;

/**
 * JavaFX event controller for GUI of UpdatRRR
 *
 * @author njuro
 */
public class MainController extends BaseController {

    @FXML
    private VBox vbMain;

    @FXML
    private ComboBox<Style> cbStyleSelect;

    @FXML
    private Button btUpdate;

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfAuthor;

    @FXML
    private TextField tfUrl;

    @FXML
    private TextField tfDate;

    @FXML
    private CheckBox chbEnabled;

    @FXML
    private TextArea taCode;

    @FXML
    private Label lbStatusLeft;

    @FXML
    private Label lbStatusRight;

    private File previousDatabaseFile;

    @FXML
    public void initialize() {
        Callback<ListView<Style>, ListCell<Style>> factory = lv -> new ListCell<Style>() {
            @Override
            protected void updateItem(Style item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Unnamed style" : item.getName());
            }
        };
        cbStyleSelect.setCellFactory(factory);
        cbStyleSelect.setButtonCell(factory.call(null));
        initializeStyles();
    }

    private void initializeStyles() {
        try {
            manager.loadStyles();
            lbStatusLeft.setText("Successfully loaded " + manager.getStyles().size() + " styles from " +
                    manager.getDatabaseFile().getAbsolutePath());
            manager.getStyles().sort(Comparator.comparing(Style::getDate).reversed());
            //workaround for proper refresh of styles in combo box
            int index = cbStyleSelect.getSelectionModel().getSelectedIndex();
            cbStyleSelect.getItems().clear();
            cbStyleSelect.getItems().setAll(manager.getStyles());
            cbStyleSelect.getSelectionModel().select(index);
        } catch (DatabaseFileException dbe) {
            if (manager.getDatabaseFile() == null || manager.getSettings().getProperty("dbpath").equals("")) {
                Alert welcome = new AlertBuilder(Alert.AlertType.INFORMATION).title("Welcome to UpdatRRR")
                        .header("Welcome!")
                        .content("Please specify the path to your StylRRR database file").createAlert();
                welcome.getDialogPane().setMinWidth(600);
                welcome.getButtonTypes().setAll(ButtonType.NEXT, ButtonType.CLOSE);
                Optional<ButtonType> response = welcome.showAndWait();
                response.ifPresent(buttonType -> {
                    if (buttonType.getButtonData().isCancelButton()) {
                        System.exit(0);
                    }
                });
            } else {
                new AlertBuilder(Alert.AlertType.ERROR).title("Error").header("Error loading database")
                        .content("Error loading file: " + dbe.getMessage())
                        .createAlert().showAndWait();
            }
            if (previousDatabaseFile != null) {
                manager.setDatabaseFile(previousDatabaseFile);
            }
            btOpenSettings();
        } finally {
            cbStyleSelect.setDisable(false);
        }
    }

    @FXML
    private void refreshStyle() {
        Style selectedStyle = cbStyleSelect.getSelectionModel().getSelectedItem();
        if (selectedStyle != null) {
            lbStatusRight.setText(selectedStyle.getName());
            tfName.setText(selectedStyle.getName());
            tfAuthor.setText(selectedStyle.getAuthor());
            tfUrl.setText(selectedStyle.getUrl());
            tfDate.setText(selectedStyle.getDateString());
            chbEnabled.setSelected(selectedStyle.isEnabled());
            taCode.setText(selectedStyle.getCode());
        }
    }

    @FXML
    private void btSaveStyles() {
        Style selectedStyle = cbStyleSelect.getSelectionModel().getSelectedItem();
        if (selectedStyle != null) {
            selectedStyle.setName(tfName.getText().trim());
            selectedStyle.setAuthor(tfAuthor.getText().trim());
            selectedStyle.setUrl(tfUrl.getText().trim());
            selectedStyle.setEnabled(chbEnabled.isSelected());
            selectedStyle.setCode(taCode.getText());
        }
        try {
            manager.saveStyles();
            refreshStyle();
        } catch (DatabaseFileException dbe) {
            new AlertBuilder(Alert.AlertType.ERROR).title("Saving failed").header("Error saving database")
                    .content("Error saving styles to the database at " + dbe.getDatabaseFile().getAbsolutePath() + ": "
                            + dbe.getMessage()).createAlert().showAndWait();
            return;
        }
        new AlertBuilder(Alert.AlertType.INFORMATION).title("Saved successfully").content("Database successfully saved")
                .createAlert().showAndWait();
        manager.getStyles().sort(Comparator.comparing(Style::getDate).reversed());
        cbStyleSelect.getItems().setAll(manager.getStyles());
    }

    @FXML
    private void btUpdateStyle() {
        Style selectedStyle = cbStyleSelect.getSelectionModel().getSelectedItem();
        if (selectedStyle == null) {
            return;
        }
        String result;
        try {
            btUpdate.setDisable(true);
            result = manager.updateStyle(selectedStyle);
            refreshStyle();
        } catch (StyleException se) {
            new AlertBuilder(Alert.AlertType.ERROR).title("Update failed").header("Update failed")
                    .content("Getting update for " + se.getStyle().getName() + " failed: " + se.getMessage())
                    .createAlert().showAndWait();
            return;
        } finally {
            btUpdate.setDisable(false);
        }
        if (result != null) {
            new AlertBuilder(Alert.AlertType.INFORMATION).title("Updated successfully")
                    .header(selectedStyle.getName() + " updated!")
                    .content("Style " + selectedStyle.getName() + " was updated from version " + result +
                            " to version  " + selectedStyle.getDateString())
                    .createAlert().showAndWait();
            btSaveStyles();
        } else {
            new AlertBuilder(Alert.AlertType.INFORMATION).title("Update not found").header("Update not found")
                    .content(selectedStyle.getName() + " is already the most recent version ("
                            + selectedStyle.getDateString() + ")")
                    .createAlert().showAndWait();
        }
    }

    @FXML
    private void btUpdateAllStyles() {
        int updated = 0;
        int notUpdated = 0;
        int failed = 0;
        StringBuilder messages = new StringBuilder();
        for (Style style : manager.getStyles()) {
            try {
                messages.append("- ").append(style.getName()).append(": ");
                String result = manager.updateStyle(style);
                if (result == null) {
                    messages.append("Already the most recent version");
                    notUpdated++;
                } else {
                    messages.append("Updated from version ").append(result).append(" to ").append(style.getDateString());
                    updated++;
                }
            } catch (StyleException se) {
                messages.append("Getting update failed: ").append(se.getMessage());
                failed++;
            } finally {
                messages.append("\n");
            }
        }
        Alert alert = new AlertBuilder(Alert.AlertType.INFORMATION).title("Update completed")
                .header("All updated completes").createAlert();
        Label label = new Label(String.format("Styles: %d | Updated: %d | Not updated: %d | Failed: %d",
                manager.getStyles().size(), updated, notUpdated, failed));
        TextArea results = new TextArea(messages.toString());
        results.setEditable(false);
        GridPane.setVgrow(results, Priority.ALWAYS);
        GridPane.setHgrow(results, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.setVgap(10);
        expContent.add(label, 0, 0);
        expContent.add(results, 0, 1);
        alert.getDialogPane().setContent(expContent);
        alert.showAndWait();
        btSaveStyles();
    }

    @FXML
    private void btOpenSettings() {
        try {
            previousDatabaseFile = manager.getDatabaseFile();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(UpdatRRR_GUI.class.getClassLoader().getResource("views/settings.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Settings");
            Scene scene = new Scene(loader.load());
            loadTheme(scene);
            stage.setScene(scene);
            stage.getIcons().add(UpdatRRR_GUI.ICON);
            stage.showAndWait();
            loadTheme(vbMain.getScene());
            initializeStyles();
        } catch (IOException ioe) {
            new AlertBuilder(Alert.AlertType.ERROR).title("Error").header("Error opening settings")
                    .content("Settings could not be opened: " + ioe.getMessage())
                    .createAlert().showAndWait();
        }
    }
}
