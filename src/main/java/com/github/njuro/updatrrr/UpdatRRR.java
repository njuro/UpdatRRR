package com.github.njuro.updatrrr;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Manager for updating and editing userstyles for StylRRR addon
 * (https://addons.mozilla.org/en-Us/firefox/addon/stylrrr/)
 * Database file usually found in ${FIREFOX_PROFILE_PATH}/stylRRR/stylRRR_DB.json
 *
 * @author njuro
 */
public class UpdatRRR implements StyleManager {
    public static final String PROPERTIES_FILE = "updatrrr.properties";
    private Properties settings = new Properties();
    private File databaseFile;
    private List<Style> styles;
    private ObjectMapper mapper;

    /**
     * @throws IOException when properties file is missing, or corrupted
     */
    public UpdatRRR() throws IOException {
        settings.load(new FileInputStream("updatrrr.properties"));
        databaseFile = new File(settings.getProperty("dbpath"));
        mapper = new ObjectMapper();
        //configure mapper to load values based on field names and not getters/setters
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
        );
    }

    public static void main(String[] args) throws Exception {
        UpdatRRR manager = new UpdatRRR();
        manager.loadStyles();
        manager.updateAllStyles();
        manager.saveStyles();
    }

    /**
     * Gets file from resources folder
     *
     * @param path to resource
     * @return URL URL of resource file (can be null, if file is not found)
     */
    public static URL getResource(String path) {
        return UpdatRRR.class.getClassLoader().getResource(path);
    }

    @Override
    public void loadStyles() throws DatabaseFileException {
        if (databaseFile == null) {
            databaseFile = (settings.getProperty("dbpath").isEmpty()) ? null : new File(settings.getProperty("dbpath"));
        }
        try {
            JsonNode root = mapper.readTree(databaseFile);
            styles = new ArrayList<>();
            //This exception rethrowing is a mess, but there is no easy way to throw checked exceptions inside
            //lambda expressions.
            root.forEach(node -> {
                try {
                    Style style = mapper.readValue(node.traverse(), Style.class);
                    styles.add(style);
                } catch (IOException ioe) {
                    throw new IllegalArgumentException("Failed to read data, possible corruption");
                }
            });
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            throw new DatabaseFileException(e.getMessage(), databaseFile);
        }
    }

    @Override
    public void saveStyles() throws DatabaseFileException {
        try {
            getMapper().writeValue(databaseFile, getStyles());
        } catch (IOException ioe) {
            throw new DatabaseFileException(ioe.getMessage(), databaseFile);
        }
    }

    @Override
    public String updateStyle(Style style) throws StyleException {
        try {
            if (style.getUrl() == null || !style.getUrl().toLowerCase().contains("://userstyles.org")) {
                throw new StyleException("Invalid userstyles.org URL", style);
            }
            Document stylePage = Jsoup.connect(style.getUrl()).get();
            String updated = stylePage.select("#style-author-info tr:nth-child(4) td").text();
            String old = style.getDateString();
            if (Style.parseStringToDate(updated).compareTo(style.getDate()) > 0) {
                String updatedCode = "";
                URL styleURL = new URL(style.getUrl() + ".css");
                try (BufferedReader input = new BufferedReader(new InputStreamReader(styleURL.openStream()))) {
                    String line;
                    while ((line = input.readLine()) != null) {
                        updatedCode += line + System.lineSeparator();
                    }
                }
                style.setDate(updated);
                style.setCode(updatedCode);
                return old;
            }
        } catch (IOException ioe) {
            throw new StyleException("Connection failed: " + ioe.getMessage(), style);
        } catch (IllegalArgumentException iae) {
            throw new StyleException("Illegal argument: " + iae.getMessage(), style);
        }
        return null;
    }

    @Override
    public List<StyleException> updateAllStyles() {
        List<StyleException> exceptions = new ArrayList<>();
        for (Style style : styles) {
            try {
                updateStyle(style);
            } catch (StyleException se) {
                exceptions.add(se);
            }
        }
        return exceptions;
    }

    @Override
    public void addStyle(Style style) {
        if (styles != null) {
            style.setDate(Style.parseDateToString(new Date()));
            styles.add(style);
        }
    }

    @Override
    public void removeStyle(Style style) {
        if (style == null) {
            return;
        }
        styles.remove(style);
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    public void setDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    public Properties getSettings() {
        return settings;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

}
