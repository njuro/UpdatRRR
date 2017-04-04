package com.github.njuro.updatrrr;

import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;

import java.util.List;

/**
 * Styles manager interface for loading, updating and saving styles
 *
 * @author njuro
 */
public interface StyleManager {

    /**
     * Loads userstyles from database file.
     *
     * @throws DatabaseFileException if loading from file failed
     */
    void loadStyles() throws DatabaseFileException;

    /**
     * Saves edited, updated styles back into database file
     *
     * @throws DatabaseFileException if saving to file failed
     */
    void saveStyles() throws DatabaseFileException;


    /**
     * Finds if more recent version of style is available on userstyles.org and replaces the code
     *
     * @return date associated with style before update, empty string if update failed
     * @throws StyleException when URL associated with style is invalid
     */
    String updateStyle(Style style) throws StyleException;

    /**
     * Updates all styles
     *
     * @return List of exceptions thrown during update
     */
    List<StyleException> updateAllStyles();

    /**
     * Adds new style to database
     *
     * @param style style to add
     *              <p>
     *              TODO: Implement integrity check
     */
    void addStyle(Style style);

    /**
     * Removes style from database
     *
     * @param style style to remove
     *              <p>
     *              TODO: Implement integrity check
     */
    void removeStyle(Style style);

}
