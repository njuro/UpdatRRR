package com.github.njuro.updatrrr;

import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;

import java.util.List;

/**
 * Styles manager interface
 *
 * @author njuro
 */
public interface StyleManager {

    /**
     * Loads userstyles from StylRRR database file. Usually found in {FIREFOX_PROFILE_PATH}/stylRRR/stylRRR_DB.json
     *
     * @param filePath Path to StylRRR database
     * @return true if styles were successfully loaded, false if something happened
     */
    boolean loadStyles(String filePath) throws DatabaseFileException;

    /**
     * Saves edited, updated styles back into database file
     *
     * @param filePath Path to StylRRR database
     * @return true if styles were successfully loaded, false if something happened
     */
    boolean saveStyles(String filePath) throws DatabaseFileException;

    /**
     * Adds new style to database
     *
     * @param style style to be added
     */
    boolean addStyle(Style style);

    /**
     * Finds if more recent version of style is available on userstyles.org and replaces the code
     *
     * @return date associated with style before update, "-" if update failed
     */
    String updateStyle(Style style) throws StyleException;

    /**
     * Updates all styles
     *
     * @return List of exceptions throwed during updates;
     */
    List<StyleException> updateAllStyles();

    /**
     * Removes style from database
     * <p>
     * -- NOT IMPLEMENTED YET --
     */
    boolean removeStyle(Style style);

}
