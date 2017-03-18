package com.github.njuro;

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
    boolean loadStyles(String filePath);

    /**
     * Saves edited, updated styles back into database file
     *
     * @param filePath Path to StylRRR database
     *
     * @return true if styles were successfully loaded, false if something happened
     */
    boolean saveStyles(String filePath);

    /**
     * Adds new style to database
     * <p>
     * -- NOT IMPLEMENTED YET --
     */
    boolean addStyle(Style style);

    /**
     * Finds if more recent version of style is available on userstyles.org and replaces the code
     *
     * @return true if update was found and successfully applied, false otherwise
     */
    boolean updateStyle(Style style);

    /**
     * Updates all styles
     * */
    void updateAllStyles();

    /**
     * Removes style from database
     * <p>
     * -- NOT IMPLEMENTED YET --
     */
    boolean removeStyle(Style style);

}
