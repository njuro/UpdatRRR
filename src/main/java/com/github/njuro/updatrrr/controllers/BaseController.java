package com.github.njuro.updatrrr.controllers;

import com.github.njuro.updatrrr.Theme;
import com.github.njuro.updatrrr.UpdatRRR;
import javafx.scene.Scene;

import java.net.URL;

/**
 * Base class for com.github.njuro.updatrrr.controllers
 *
 * @author njuro
 */
public abstract class BaseController {

    protected static UpdatRRR manager;
    protected static Theme theme;

    public static void initManager(UpdatRRR manager) {
        if (BaseController.manager == null) {
            BaseController.manager = manager;
        }
    }

    public static void loadTheme(Scene scene) {
        loadTheme(theme, scene);
    }

    public static void loadTheme(Theme theme, Scene scene) {
        if (scene == null || theme == null) return;
        switch (theme) {
            case DARK:
                scene.getStylesheets().addAll(
                        getResource("views/modena_dark.css").toExternalForm(),
                        getResource("views/custom.css").toExternalForm()
                );
                break;
            case LIGHT:
                scene.getStylesheets().clear();
                break;
            default:
        }
    }


    public static URL getResource(String name) {
        return BaseController.class.getClassLoader().getResource(name);
    }

    public static void setTheme(String theme) {
        setTheme(Theme.valueOf(theme.toUpperCase()));
    }

    public static Theme getTheme() {
        return theme;
    }

    public static void setTheme(Theme theme) {
        BaseController.theme = theme;
    }
}
