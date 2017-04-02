package com.github.njuro.updatrrr;

import com.github.njuro.updatrrr.controllers.BaseController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Frontend for the GUI of UpdatRRR
 *
 * @author njuro
 */
public class UpdatRRR_GUI extends Application {
    public static final Image ICON = new Image(UpdatRRR_GUI.class.getClassLoader()
            .getResourceAsStream("views/icon.png"));
    private UpdatRRR manager;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            manager = new UpdatRRR();
            BaseController.initManager(manager);
        } catch (IOException ioe) {
            new AlertBuilder(Alert.AlertType.ERROR).title("Properties file error").header("Error loading properties")
                    .content("Error loading properties file: " + ioe.getMessage()).createAlert().showAndWait();
            System.exit(1);
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(UpdatRRR_GUI.class.getClassLoader().getResource("views/layout.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("UpdatRRR");
        stage.getIcons().add(ICON);
        stage.setOnCloseRequest(windowEvent -> {
            try {
                manager.getSettings().setProperty("dbpath", manager.getDatabaseFile().getAbsolutePath());
                manager.getSettings().store(new FileOutputStream(UpdatRRR.PROPERTIES_FILE), "UpdatRRR config file");
            } catch (IOException ioe) {
                new AlertBuilder(Alert.AlertType.ERROR).title("Properties file error").header("Error saving properties")
                        .content("Error loading properties file: " + ioe.getMessage()).createAlert().showAndWait();

            } finally {
                System.exit(1);
            }
        });
        stage.show();
    }
}
