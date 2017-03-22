package com.github.njuro.updatrrr;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.crypto.Data;
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
    public static final String DB_PATH = getDbPath();
    private File dbFile;
    private List<Style> styles;
    private ObjectMapper mapper;

    public UpdatRRR() {
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
        manager.loadStyles(DB_PATH);
        manager.updateAllStyles();
        manager.saveStyles(DB_PATH);
    }

    @Override
    public boolean loadStyles(String filePath) throws DatabaseFileException {
        try {
            JsonNode root = mapper.readTree(dbFile);
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
        } catch (FileNotFoundException | NullPointerException e) {
            throw new DatabaseFileException("File not found", dbFile);
        } catch (IOException | IllegalArgumentException e) {
            throw new DatabaseFileException(e.getMessage(), dbFile);
        }
    }

    @Override
    public boolean saveStyles(String filePath) throws DatabaseFileException {
        try {
            styles.sort(Comparator.comparing(Style::getDate).reversed());
            getMapper().writeValue(new File(filePath), getStyles());
            return true;
        } catch (IOException ioe) {
            throw new DatabaseFileException("Failed to save styles: " + ioe.getMessage(), dbFile);
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
            if (style.getUrl() == null || style.getUrl().equals("-") ||
                   !style.getUrl().toLowerCase().contains("://userstyles.org")) {
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
        return "-";
    }

    @Override
    public List<StyleException> updateAllStyles() {
        List<StyleException> exceptions = new ArrayList<>();
        for(Style style: styles) {
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

    public List<Style> getStyles() {
        return styles;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    private static String getDbPath() {
        try(Scanner scanner = new Scanner(new File(UpdatRRR.class.getClassLoader().getResource("dbpath.txt").getFile()))) {
            return scanner.nextLine().trim();
        } catch (IOException ioe) {
            System.err.println("-");
        }
        return "-";
    }
}
