package com.github.njuro.updatrrr;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import javafx.stage.Modality;

/**
 * Builder for custom Alerts
 *
 * @author njuro
 */
public class AlertBuilder {
    private Alert.AlertType type = Alert.AlertType.NONE;
    private String title = "";
    private String header = "";
    private String content = "";
    private Modality modality = Modality.NONE;

    public AlertBuilder() {
    }

    public AlertBuilder(Alert.AlertType type) {
        this.type = type;
    }

    public AlertBuilder type(Alert.AlertType type) {
        this.type = type;
        return this;
    }

    public AlertBuilder title(String title) {
        this.title = title;
        return this;
    }

    public AlertBuilder header(String header) {
        this.header = header;
        return this;
    }

    public AlertBuilder content(String content) {
        this.content = content;
        return this;
    }

    public AlertBuilder modality(Modality modality) {
        this.modality = modality;
        return this;
    }

    public Alert createAlert() {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initModality(modality);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.initOwner(alert.getOwner());
        return alert;
    }


}
