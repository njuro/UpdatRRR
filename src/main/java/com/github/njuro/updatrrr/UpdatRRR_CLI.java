package com.github.njuro.updatrrr;

import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;

import java.io.IOException;
import java.util.Scanner;

/**
 * CLI for UpdatRRR.
 *
 * Correct path to StylRRR database must be specified in "updatrrr.properties" file (dbpath parameter)
 *
 * @author njuro
 */
public class UpdatRRR_CLI {
    private static UpdatRRR manager;

    //prompt string
    private static final String PREFIX = "updatrrr> ";

    /**
     * Parses input and executes appropriate command
     *
     * @param command user input
     */
    private static void parseCommand(String command) {
        String[] args = command.split(" ");
        switch (args[0].trim().toLowerCase()) {
            case "?":
            case "h":
            case "help":
                printHelp();
                break;
            case "list":
                printAll();
                break;
            case "update":
                try {
                    update(Integer.parseInt(args[1]));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    System.out.println("Syntax: update <index>");
                }
                break;
            case "update-all":
                updateAll();
                break;
            case "save":
                save();
                break;
            case "info":
                try {
                    printInfo(Integer.parseInt(args[1]));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    System.out.println("Syntax: info <index>");
                }
                break;
            case "q":
            case "quit":
            case "exit":
                System.out.println("Bye.");
                System.exit(0);
                break;
            default:
                System.out.println("Unknown command \"" + command + "\". Type \"help\" for list of available commands");
        }
    }

    /**
     * Prints list of available commands and what they do
     */
    private static void printHelp() {
        System.out.println("Help:");
        System.out.println("\tlist - Displays all styles along with their indices");
        System.out.println("\tupdate <index> - Updates style with more recent version, if such version is found");
        System.out.println("\tupdate-all - Updates all styles");
        System.out.println("\tsave - Saves styles to the database");
        System.out.println("\tinfo <index> - Displays detailed info about style");
        System.out.println("\thelp - Displays this help");
        System.out.println("\texit - Exits UpdatRRR");
    }

    /**
     * Prints all styles along with their indices
     */
    private static void printAll() {
        for (int i = 0; i < manager.getStyles().size(); i++) {
            System.out.println((i + 1) + ". " + manager.getStyles().get(i).getName());
        }
    }

    /**
     * Tries to replace style with a more recent version, if such version is found
     *
     * @param index of style
     * @return 0 if provided URL is invalid, 1 if style is already the most recent version, 2 if style was updated
     */
    private static int update(int index) {
        if (!checkValidIndex(index)) return 0;
        String result;
        Style style = manager.getStyles().get(index - 1);
        try {
            result = manager.updateStyle(style);
        } catch (StyleException se) {
            System.out.println("Update failed for style " + style.getName() + ": " + se.getMessage());
            return 0;
        }
        if (result == null) {
            System.out.println(style.getName() + " is already the most recent version (" + style.getDateString() + ")");
            return 1;
        }
        System.out.println(style.getName() + " was updated from version" + style.getDateString() + " to version "
                + result);
        return 2;
    }

    /**
     * Tries to update all styles and prints the results
     */
    private static void updateAll() {
        int updated = 0;
        int notUpdated = 0;
        int failed = 0;
        for (int i = 0; i < manager.getStyles().size(); i++) {
            int result = update(i + 1);
            switch (result) {
                case 0:
                    failed++;
                    break;
                case 1:
                    notUpdated++;
                    break;
                case 2:
                    updated++;
            }
        }
        System.out.printf("\nStyles: %d | Updated: %d | Not updated: %d | Failed: %d\n", manager.getStyles().size(),
                updated, notUpdated, failed);
    }

    /**
     * Saves styles to database file
     */
    private static void save() {
        try {
            manager.saveStyles();
        } catch (DatabaseFileException dbe) {
            System.out.println("Saving failed: " + dbe.getMessage());
            return;
        }
        System.out.println("Saved successfully to " + manager.getDatabaseFile().getAbsolutePath());
    }

    /**
     * Displays detailed info about style
     *
     * @param index of style
     */
    private static void printInfo(int index) {
        if (!checkValidIndex(index)) return;
        System.out.println(manager.getStyles().get(index - 1));
    }

    /**
     * Checks whether the style index is in valid range
     *
     * @param index of style
     * @return true if index is in interval <1, numOfStyles)
     */
    private static boolean checkValidIndex(int index) {
        if (index < 1 || index > manager.getStyles().size()) {
            System.out.println("Invalid index.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println("Welcome to UpdatRRR!\n");
        boolean failed = false;
        try {
            manager = new UpdatRRR();
            manager.loadStyles();
            System.out.println("Loaded " + manager.getStyles().size() + " styles from " + manager.getDatabaseFile().getAbsolutePath());
        } catch (DatabaseFileException dbe) {
            System.out.println("Failed to load styles: " + dbe.getMessage());
            System.out.println("Please fill in \"dbpath\" in updatrrr.properties file");
            failed = true;
        } catch (IOException ioe) {
            System.out.println("Failed to load properties file: " + ioe.getMessage());
            failed = true;
        }
        String command;
        try (Scanner input = new Scanner(System.in)) {
            if (failed) {
                System.out.println("Press ENTER to exit...");
                input.nextLine();
                System.exit(1);
            }
            while (true) {
                System.out.print(PREFIX);
                command = input.nextLine();
                parseCommand(command);
            }
        }
    }
}
