package com.github.njuro.updatrrr.controllers;

import com.github.njuro.updatrrr.AlertBuilder;
import com.github.njuro.updatrrr.Style;
import com.github.njuro.updatrrr.UpdatRRR;
import com.github.njuro.updatrrr.UpdatRRR_GUI;
import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;

/**
 * Event controller for GUI of UpdatRRR
 *
 * @author njuro
 */
public class MainController extends BaseController {

    @FXML
    private VBox vbMain;

    @FXML
    private ComboBox<Style> cbStyles;

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
        cbStyles.setCellFactory(factory);
        cbStyles.setButtonCell(factory.call(null));
        initializeStyles();
    }

    /**
     * Populates combobox with styles from database file
     */
    private void initializeStyles() {
        try {
            manager.loadStyles();
            lbStatusLeft.setText("Successfully loaded " + manager.getStyles().size() + " styles from " +
                    manager.getDatabaseFile().getAbsolutePath());
            manager.getStyles().sort(Comparator.comparing(Style::getDate).reversed());
            //workaround for proper refresh of styles in combo box
            int index = cbStyles.getSelectionModel().getSelectedIndex();
            cbStyles.getItems().setAll(manager.getStyles());
            cbStyles.getSelectionModel().select(index);
        } catch (DatabaseFileException dbe) {
            if (manager.getDatabaseFile() == null || manager.getSettings().getProperty("dbpath").equals("")) {
                Alert firstRun = new AlertBuilder(Alert.AlertType.INFORMATION)
                        .title("Welcome!")
                        .header("Welcome to UpdatRRR")
                        .content("Please specify the path to your StylRRR database file")
                        .createAlert();
                firstRun.getDialogPane().setMinWidth(600);
                firstRun.getButtonTypes().setAll(ButtonType.NEXT, ButtonType.CLOSE);
                Optional<ButtonType> response = firstRun.showAndWait();
                response.ifPresent(buttonType -> {
                    if (buttonType.getButtonData().isCancelButton()) {
                        System.exit(0);
                    }
                });
            } else {
                Alert errorLoading = new AlertBuilder(Alert.AlertType.ERROR)
                        .title("Error!")
                        .header("Error loading database")
                        .content("Error loading file: " + dbe.getMessage())
                        .createAlert();
                errorLoading.getDialogPane().setMinWidth(600);
                ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);
                errorLoading.getButtonTypes().setAll(ButtonType.OK, exitButton);
                Optional<ButtonType> response = errorLoading.showAndWait();
                response.ifPresent(buttonType -> {
                    if (buttonType.getButtonData().isCancelButton()) {
                        System.exit(0);
                    }
                });
            }
            if (previousDatabaseFile != null) {
                manager.setDatabaseFile(previousDatabaseFile);
            }
            openSettings();
        } finally {
            cbStyles.setDisable(false);
        }
    }

    @FXML
    private void refreshStyle() {
        selectedStyle = cbStyles.getSelectionModel().getSelectedItem();
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
    private void saveStyles() {
        if (selectedStyle != null) {
            selectedStyle.setName(tfName.getText().trim());
            selectedStyle.setAuthor(tfAuthor.getText().trim());
            selectedStyle.setUrl(tfUrl.getText().trim());
            selectedStyle.setEnabled(chbEnabled.isSelected());
            selectedStyle.setCode(taCode.getText());
        }
        try {
            manager.saveStyles();
            refreshComboBox();
            refreshStyle();
        } catch (DatabaseFileException dbe) {
            new AlertBuilder(Alert.AlertType.ERROR)
                    .title("Saving failed!")
                    .header("Error saving database")
                    .content("Error saving styles to the database at " + dbe.getDatabaseFile().getAbsolutePath() + ": "
                            + dbe.getMessage())
                    .createAlert()
                    .showAndWait();
            return;
        }

        new AlertBuilder(Alert.AlertType.INFORMATION)
                .title("Saving succeeded!")
                .content("Database successfully saved")
                .createAlert()
                .showAndWait();
    }

    @FXML
    private void updateStyle() {
        if (selectedStyle == null) {
            return;
        }

        String result;
        try {
            result = manager.updateStyle(selectedStyle);
            refreshStyle();
        } catch (StyleException se) {
            new AlertBuilder(Alert.AlertType.ERROR)
                    .title("Update failed!")
                    .header("Error updating style")
                    .content("Getting update for " + se.getStyle().getName() + " failed: " + se.getMessage())
                    .createAlert()
                    .showAndWait();
            return;
        }

        if (result != null) {
            new AlertBuilder(Alert.AlertType.INFORMATION)
                    .title("Update succeeded")
                    .header(selectedStyle.getName() + " updated!")
                    .content("Style " + selectedStyle.getName() + " was updated from version " + result +
                            " to version  " + selectedStyle.getDateString())
                    .createAlert()
                    .showAndWait();
            saveStyles();
        } else {
            new AlertBuilder(Alert.AlertType.INFORMATION)
                    .title("Update not found!")
                    .header("Update was not found")
                    .content(selectedStyle.getName() + " is already the most recent version ("
                            + selectedStyle.getDateString() + ")")
                    .createAlert()
                    .showAndWait();
        }
    }

    @FXML
    private void updateAllStyles() {
        int updated = 0, notUpdated = 0, failed = 0;
        StringBuilder messages = new StringBuilder();
        for (Style style : manager.getStyles()) {
            try {
                messages.append("- " + style.getName() + ": ");
                String result = manager.updateStyle(style);
                if (result == null) {
                    messages.append("Already the most recent version");
                    notUpdated++;
                } else {
                    messages.append("Updated from version " + result + " to " + style.getDateString());
                    updated++;
                }
            } catch (StyleException se) {
                messages.append("Getting update failed: " + se.getMessage());
                failed++;
            } finally {
                messages.append("\n");
            }
        }

        Alert updateResults = new AlertBuilder(Alert.AlertType.INFORMATION)
                .title("Updates completed!")
                .header("All updates were completed")
                .createAlert();
        Label label = new Label(
                String.format("Styles: %d | Updated: %d | Not updated: %d | Failed: %d",
                        manager.getStyles().size(), updated, notUpdated, failed)
        );
        TextArea results = new TextArea(messages.toString());
        results.setEditable(false);
        GridPane expContent = new GridPane();
        expContent.setVgap(10);
        expContent.add(label, 0, 0);
        expContent.add(results, 0, 1);
        updateResults.getDialogPane().setContent(expContent);
        updateResults.showAndWait();

        saveStyles();
    }

    @FXML
    private void openSettings() {
        try {
            previousDatabaseFile = manager.getDatabaseFile();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(UpdatRRR.getResource("views/settings.fxml"));
            Scene scene = new Scene(loader.load());
            loadTheme(scene);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Settings");
            stage.setScene(scene);
            stage.getIcons().add(UpdatRRR_GUI.ICON);
            stage.showAndWait();

            loadTheme(vbMain.getScene());
            initializeStyles();
        } catch (IOException ioe) {
            new AlertBuilder(Alert.AlertType.ERROR)
                    .title("Error!")
                    .header("Error opening settings")
                    .content("Settings could not be opened: " + ioe.getMessage())
                    .createAlert()
                    .showAndWait();
        }
    }

    private void refreshComboBox() {
        Style prevStyle = selectedStyle;
        cbStyles.getSelectionModel().select(null);
        cbStyles.getSelectionModel().select(prevStyle);
    }
}
