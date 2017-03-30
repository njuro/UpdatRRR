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
import java.util.*;

/**
 * Manager for updating and editing userstyles for StylRRR addon
 * <p>
 * (https://addons.mozilla.org/en-Us/firefox/addon/stylrrr/)
 *
 * @author njuro
 */
public class UpdatRRR implements StyleManager {
    private Properties settings = new Properties();
    private File databaseFile;
    private List<Style> styles;
    private ObjectMapper mapper;
    public static final String PROPERTIES_FILE = "updatrrr.properties";

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

    @Override
    public boolean loadStyles() throws DatabaseFileException {
        try {
            JsonNode root = mapper.readTree(databaseFile);
            styles = new ArrayList<>();
            //This exception rethrowing is a mess, but apparently there is no easy way to throw checked exceptions inside
            //lambda expressions. Thanks Oracle.
            root.forEach(node -> {
                try {
                    Style style = mapper.readValue(node.traverse(), Style.class);
                    styles.add(style);
                } catch (IOException ioe) {
                    throw new IllegalArgumentException("Failed to read data, possible corruption");
                }
            });
            styles.sort(Comparator.comparing(Style::getDate));
            return true;
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            throw new DatabaseFileException(e.getMessage(), databaseFile);
        }
    }

    @Override
    public boolean saveStyles() throws DatabaseFileException {
        try {
            styles.sort(Comparator.comparing(Style::getDate).reversed());
            getMapper().writeValue(databaseFile, getStyles());
            return true;
        } catch (IOException ioe) {
            throw new DatabaseFileException(ioe.getMessage(), databaseFile);
        }
    }

    @Override
    public boolean addStyle(Style style) {
        if (styles != null) {
            style.setDate(Style.parseDateToString(new Date()));
            styles.add(style);
        }
        return false;
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
    public boolean removeStyle(Style style) {
        if (style == null) {
            return false;
        }
        return styles.remove(style);
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
