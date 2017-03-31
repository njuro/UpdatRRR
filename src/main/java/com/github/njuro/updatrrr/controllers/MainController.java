package com.github.njuro.updatrrr.controllers;

import com.github.njuro.updatrrr.AlertBuilder;
import com.github.njuro.updatrrr.Style;
import com.github.njuro.updatrrr.UpdatRRR_GUI;
import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

/**
 * JavaFX event controller for GUI of UpdatRRR
 *
 * @author njuro
 */
public class MainController extends BaseController {

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

    @FXML
    public void initialize() {
        Callback<ListView<Style>, ListCell<Style>> factory = lv -> new ListCell<Style>() {
            @Override
            protected void updateItem(Style item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Unnamed style" : item.getName());
                if (!empty) {
                    lbStatusRight.setText(item.getName());
                    tfName.setText(item.getName());
                    tfAuthor.setText(item.getAuthor());
                    tfUrl.setText(item.getUrl());
                    tfDate.setText(item.getDateString());
                    chbEnabled.setSelected(item.isEnabled());
                    taCode.setText(item.getCode());
                }
            }
        };
        cbStyleSelect.setCellFactory(factory);
        cbStyleSelect.setButtonCell(factory.call(null));
        initializeStyles();
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
        } catch (DatabaseFileException dbe) {
            new AlertBuilder(Alert.AlertType.ERROR).title("Saving failed").header("Error saving database")
                    .content("Error saving styles to the database at " + dbe.getDatabaseFile().getAbsolutePath() + ": "
                            + dbe.getMessage()).createAlert().showAndWait();
            return;
        }
        new AlertBuilder(Alert.AlertType.INFORMATION).title("Saved successfully").content("Database successfully saved")
                .createAlert().showAndWait();
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

    private void initializeStyles() {
        try {
            manager.loadStyles();
            manager.getSettings().setProperty("dbpath", manager.getDatabaseFile().getAbsolutePath());
            lbStatusLeft.setText("Successfully loaded " + manager.getStyles().size() + " styles from " +
                    manager.getSettings().getProperty("dbpath"));
            cbStyleSelect.getItems().setAll(manager.getStyles());
        } catch (DatabaseFileException dbe) {
            new AlertBuilder(Alert.AlertType.ERROR).title("Error").header("Error loading database")
                    .content("Error loading file: " + dbe.getMessage())
                    .createAlert().showAndWait();
            btOpenSettings();
        } finally {
            cbStyleSelect.setDisable(false);
        }
    }

    @FXML
    private void btOpenSettings() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(UpdatRRR_GUI.class.getClassLoader().getResource("views/settings.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Settings");
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
            initializeStyles();
        } catch (IOException ioe) {
            new AlertBuilder(Alert.AlertType.ERROR).title("Error").header("Error opening settings")
                    .content("Settings could not be opened: " + ioe.getMessage())
                    .createAlert().showAndWait();
        }
    }
}
