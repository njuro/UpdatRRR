package com.github.njuro.updatrrr;

import com.github.njuro.updatrrr.controllers.BaseController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Frontend for the GUI of UpdatRRR
 *
 * @author njuro
 */
public class UpdatRRR_GUI extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    private UpdatRRR manager;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            manager = new UpdatRRR();
            BaseController.initManager(manager);
        } catch (IOException ioe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Properties file error");
            alert.setHeaderText("Error loading properties");
            alert.setContentText("Error loading properties file: " + ioe.getMessage());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.initOwner(alert.getOwner());
            alert.showAndWait();
            System.exit(1);
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(UpdatRRR_GUI.class.getClassLoader().getResource("views/layout.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("UpdatRRR");
        stage.getIcons().add(new Image(UpdatRRR_GUI.class.getClassLoader().getResourceAsStream("views/icon.png")));
        stage.setOnCloseRequest(windowEvent -> {
            try {
                manager.getSettings().store(new FileOutputStream(UpdatRRR.PROPERTIES_FILE), "UpdatRRR config file");
            } catch (IOException ioe) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Properties file error");
                alert.setHeaderText("Error saving properties");
                alert.setContentText("Error loading properties file: " + ioe.getMessage());
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
                alert.initOwner(alert.getOwner());
                alert.showAndWait();
                System.exit(1);
            }
        });
        stage.show();
    }
}
