package com.github.njuro.updatrrr;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public static final String DB_PATH = getDbPath();
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
    public boolean loadStyles(String filePath) {
        try {
            File dbFile = new File(filePath);
            JsonNode root = mapper.readTree(dbFile);
            styles = new ArrayList<>();
            root.forEach(node -> {
                try {
                    Style style = mapper.readValue(node.traverse(), Style.class);
                    styles.add(style);
                } catch (IOException ioe) {
                    System.err.println("Failed to read node: " + ioe.getMessage());
                }
            });
            styles.sort(Comparator.comparing(Style::getDate));
        } catch (FileNotFoundException fnfe) {
            System.err.printf("%s - Database file not found\n", DB_PATH);
            return false;
        } catch (IOException ioe) {
            System.err.printf("%s - Failed to read file: %s\n", DB_PATH, ioe.getMessage()/**/);
            return false;
        }
        return true;
    }

    @Override
    public boolean saveStyles(String filePath) {
        try {
            styles.sort(Comparator.comparing(Style::getDate).reversed());
            getMapper().writeValue(new File(filePath), getStyles());
            return true;
        } catch (IOException ioe) {
            System.err.println("Saving file failed: " + ioe.getMessage());
            return false;
        }
    }

    @Override
    public boolean addStyle(Style style) {
        return false;
    }

    @Override
    public boolean updateStyle(Style style) {
        try {
            if (style.getUrl() == null || style.getUrl().equals("-")) return false;
            Document stylePage = Jsoup.connect(style.getUrl()).get();
            String updated = stylePage.select("#style-author-info tr:nth-child(4) td").text();
            if (Style.parseDate(updated).compareTo(style.getDate()) > 0) {
                String updatedCode = "";
                URL styleURL = new URL(style.getUrl() + ".css");
                try (BufferedReader input = new BufferedReader(new InputStreamReader(styleURL.openStream()))) {
                    String line;
                    while ((line = input.readLine()) != null) {
                        updatedCode += line + System.lineSeparator();
                    }
                }
                System.out.printf("Updated style %s with version from %s (before: %s)\n", style.getName(), updated,
                        style.getDateString());
                style.setDate(updated);
                style.setCode(updatedCode);
                return true;
            }
        } catch (IOException ioe) {
            System.out.println("Error: Failed to connect to style page: " + ioe.getMessage());
        } catch (IllegalArgumentException iae) {
            return false;
        }
        return false;
    }

    @Override
    public void updateAllStyles() {
        System.out.printf("Searching updates for %d styles.\n", styles.size());
        int count = 0;
        for(Style style: styles) {
            boolean updated = updateStyle(style);
            if (updated) {
                count++;
            }
        }
        System.out.printf("Update complete. Updated: %d, Not updated: %d\n", count, styles.size() - count);
    }

    @Override
    public boolean removeStyle(Style style) {
        return false;
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
