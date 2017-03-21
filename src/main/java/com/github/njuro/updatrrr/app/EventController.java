package com.github.njuro.updatrrr.app;

import com.github.njuro.updatrrr.Style;
import com.github.njuro.updatrrr.UpdatRRR;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
            }
        };
        cbStyleSelect.setCellFactory(factory);
        cbStyleSelect.setButtonCell(factory.call(null));
        manager = new UpdatRRR();
        initializeStyles();
        cbStyleSelect.setDisable(false);
        lbStatusLeft.setText("Successfully loaded " + manager.getStyles().size() +" styles");
    }

    @FXML
    private void changeStyle() {
        Style selectedStyle = (Style)cbStyleSelect.getSelectionModel().getSelectedItem();
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
        Style selectedStyle = (Style)cbStyleSelect.getSelectionModel().getSelectedItem();
        if (selectedStyle == null) {
            return;
        }
        selectedStyle.setName(tfName.getText().trim());
        selectedStyle.setAuthor(tfAuthor.getText().trim());
        selectedStyle.setUrl(tfUrl.getText().trim());
        selectedStyle.setEnabled(chbEnabled.isSelected());
        btSave.setDisable(true);
        manager.saveStyles(UpdatRRR.DB_PATH);
        initializeStyles();
        btSave.setDisable(false);

    }

    private void initializeStyles() {
        manager.loadStyles(UpdatRRR.DB_PATH);
        int index = cbStyleSelect.getSelectionModel().getSelectedIndex();
        cbStyleSelect.getItems().clear();
        cbStyleSelect.setItems(FXCollections.observableList(manager.getStyles()));
        cbStyleSelect.getSelectionModel().select(index);
    }

}
