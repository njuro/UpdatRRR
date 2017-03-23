package com.github.njuro.updatrrr.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Frontend for the GUI of UpdatRRR
 *
 * @author njuro
 */
public class UpdatRRR_GUI extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(UpdatRRR_GUI.class.getClassLoader().getResource("view/layout.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("UpdatRRR");
        stage.getIcons().add(new Image(UpdatRRR_GUI.class.getClassLoader().getResourceAsStream("view/icon.png")));
        stage.show();

    }
}
