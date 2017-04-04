package com.github.njuro.updatrrr.controllers;

import com.github.njuro.updatrrr.Theme;
import com.github.njuro.updatrrr.UpdatRRR;
import javafx.scene.Scene;

/**
 * Abstract base class for event controllers of UpdatRRR GUI
 *
 * @author njuro
 */
public abstract class BaseController {

    protected static UpdatRRR manager;
    protected static Theme theme;

    public static UpdatRRR getManager() {
        return manager;
    }

    public static void setManager(UpdatRRR manager) {
        if (BaseController.manager == null) {
            BaseController.manager = manager;
        }
    }

    public static Theme getTheme() {
        return theme;
    }

    public static void setTheme(String theme) {
        try {
            setTheme(Theme.valueOf(theme.toUpperCase()));
        } catch (IllegalArgumentException iae) {
            setTheme(Theme.LIGHT);
        }
    }

    public static void setTheme(Theme theme) {
        BaseController.theme = theme;
        manager.getSettings().setProperty("theme", theme.toString());
    }

    /**
     * Loads current chosen theme
     *
     * @param scene to which is theme applied
     */
    public static void loadTheme(Scene scene) {
        loadTheme(theme, scene);
    }

    /**
     * Loads specific theme
     *
     * @param theme to load
     * @param scene to which is theme applied
     */
    public static void loadTheme(Theme theme, Scene scene) {
        if (scene == null || theme == null) return;
        scene.getStylesheets().clear();
        switch (theme) {
            case DARK:
                scene.getStylesheets().addAll(
                        UpdatRRR.getResource("css/modena_dark.css").toExternalForm(),
                        UpdatRRR.getResource("css/modena_dark_custom.css").toExternalForm()
                );
                break;
            case LIGHT:
                //default theme, do nothing
                break;
            default:
                //undefined theme, reverts to default
                setTheme(Theme.LIGHT);
        }
    }
}
