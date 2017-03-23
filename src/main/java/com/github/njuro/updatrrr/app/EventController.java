package com.github.njuro.updatrrr.app;

import com.github.njuro.updatrrr.Style;
import com.github.njuro.updatrrr.UpdatRRR;
import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

/**
 * JavaFX event controller for GUI of UpdatRRR
 *
 * @author njuro
 */
public class EventController {
    private UpdatRRR manager;

    @FXML
    private GridPane gpInfo;

    @FXML
    private ComboBox cbStyleSelect;

    @FXML
    private Button btSave;

    @FXML
    private Button btUpdate;

    @FXML
    private Button btUpdateAll;

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

    private Style selectedStyle;

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
        manager = new UpdatRRR();
        if (!initializeStyles()) {
            return;
        }
        cbStyleSelect.setDisable(false);
        lbStatusLeft.setText("Successfully loaded " + manager.getStyles().size() + " styles from " + UpdatRRR.DB_PATH);
    }

    @FXML
    private void refreshStyle() {
        cbStyleSelect.setItems(FXCollections.observableList(manager.getStyles()));
        selectedStyle = (Style) cbStyleSelect.getSelectionModel().getSelectedItem();
        if (selectedStyle == null) {
            return;
        }
        lbStatusRight.setText(selectedStyle.getName());
        tfName.setText(selectedStyle.getName());
        tfAuthor.setText(selectedStyle.getAuthor());
        tfUrl.setText(selectedStyle.getUrl());
        tfDate.setText(selectedStyle.getDateString());
        chbEnabled.setSelected(selectedStyle.isEnabled());
        taCode.setText(selectedStyle.getCode());
    }

    @FXML
    private void btSaveStyles() {
        if (selectedStyle != null) {
            selectedStyle.setName(tfName.getText().trim());
            selectedStyle.setAuthor(tfAuthor.getText().trim());
            selectedStyle.setUrl(tfUrl.getText().trim());
            selectedStyle.setEnabled(chbEnabled.isSelected());
            selectedStyle.setCode(taCode.getText());
        }
        btSave.setDisable(true);
        try {
            manager.saveStyles(UpdatRRR.DB_PATH);
        } catch (DatabaseFileException dbe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Saving failed");
            alert.setHeaderText("Error saving database");
            alert.setContentText("Error saving styles to the database at " + dbe.getDatabaseFile().getAbsolutePath() + ": " + dbe.getMessage());
            setUpAlert(alert);
            return;
        } finally {
            btSave.setDisable(false);
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Saved successfully");
        alert.setHeaderText("");
        alert.setContentText("Database successfully saved");
        setUpAlert(alert);
        refreshStyle();
    }

    @FXML
    private void btUpdateStyle() {
        if (selectedStyle == null) {
            return;
        }
        String result;
        try {
            btUpdate.setDisable(true);
            result = manager.updateStyle(selectedStyle);
        } catch (StyleException se) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update failed");
            alert.setHeaderText("Update failed");
            alert.setContentText("Getting update for " + se.getStyle().getName() + " failed: " + se.getMessage());
            setUpAlert(alert);
            return;
        } finally {
            btUpdate.setDisable(false);
        }
        if (result != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Updated successfully");
            alert.setHeaderText(selectedStyle.getName() + " updated!");
            alert.setContentText("Style " + selectedStyle.getName() + " was updated from version " + result +
                    " to version  " + selectedStyle.getDateString());
            setUpAlert(alert);
            btSaveStyles();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Update not found");
            alert.setHeaderText("Update not found");
            alert.setContentText(selectedStyle.getName() + " is already the most recent version (" + selectedStyle.getDateString() + ")");
            setUpAlert(alert);
        }
    }

    @FXML
    private void btUpdateAllStyles() {
        int updated = 0;
        int notUpdated = 0;
        int failed = 0;
        String messages = "";
        for (Style style : manager.getStyles()) {
            try {
                messages += "- " + style.getName() + ": ";
                String result = manager.updateStyle(style);
                if (result == null) {
                    messages += "Already the most recent version";
                    notUpdated++;
                } else {
                    messages += "Updated from version " + result + " to " + style.getDateString();
                    updated++;
                }
            } catch (StyleException se) {
                messages += "Getting update failed: " + se.getMessage();
                failed++;
            } finally {
                messages += "\n";
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update completed");
        alert.setHeaderText("All updated completes");
        Label label = new Label(String.format("Styles: %d | Updated: %d | Not updated: %d | Failed: %d",
                manager.getStyles().size(), updated, notUpdated, failed));
        TextArea results = new TextArea(messages);
        results.setEditable(false);
        GridPane.setVgrow(results, Priority.ALWAYS);
        GridPane.setHgrow(results, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.setVgap(10);
        expContent.add(label, 0, 0);
        expContent.add(results, 0, 1);
        alert.getDialogPane().setContent(expContent);
        setUpAlert(alert);
        btSaveStyles();
    }

    private boolean initializeStyles() {
        try {
            manager.loadStyles(UpdatRRR.DB_PATH);
            refreshStyle();
        } catch (DatabaseFileException dbe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database file error");
            alert.setHeaderText("Error loading database");
            alert.setContentText("Error loading file: " + dbe.getMessage());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.initOwner(alert.getOwner());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private void setUpAlert(Alert alert) {
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.initOwner(alert.getOwner());
        alert.showAndWait();
    }

}
