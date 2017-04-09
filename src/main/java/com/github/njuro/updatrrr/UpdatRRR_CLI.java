package com.github.njuro.updatrrr;

import com.github.njuro.updatrrr.exceptions.DatabaseFileException;
import com.github.njuro.updatrrr.exceptions.StyleException;

import java.io.IOException;
import java.util.Scanner;

/**
 * CLI for UpdatRRR
 *
 * @author njuro
 */
public class UpdatRRR_CLI {
    private static UpdatRRR manager;

    private static final String PREFIX = "updatrrr> ";

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
                System.out.println("Unknown command - \"" + command + "\". Type \"help\" for list of available commands");
        }
    }

    private static void printHelp() {
        System.out.println("Help:");
        System.out.println("\tlist - Displays all styles with their indexes");
        System.out.println("\tupdate <index> - Update style with more recent version, if such version exists");
        System.out.println("\tupdate-all - Updates all styles");
        System.out.println("\tsave - Save styles to the database");
        System.out.println("\tinfo <index> - Displays detailed info about style");
        System.out.println("\thelp - Displays this help");
        System.out.println("\texit - Exits UpdatRRR");
    }

    private static void printAll() {
        for (int i = 0; i < manager.getStyles().size(); i++) {
            System.out.println((i + 1) + ". " + manager.getStyles().get(i).getName());
        }
    }

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

    private static void save() {
        try {
            manager.saveStyles();
        } catch (DatabaseFileException dbe) {
            System.out.println("Saving failed: " + dbe.getMessage());
            return;
        }
        System.out.println("Saving successful");
    }

    private static void printInfo(int index) {
        if (!checkValidIndex(index)) return;
        System.out.println(manager.getStyles().get(index - 1));
    }

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
        } catch (DatabaseFileException dbe) {
            System.out.println("Failed to load styles: " + dbe.getMessage());
            failed = true;
        } catch (IOException ioe) {
            System.out.println("Failed to launch properties file: " + ioe.getMessage());
            failed = true;
        }
        String command = "";
        try (Scanner input = new Scanner(System.in)) {
            if (failed) {
                System.out.println("Press any key to exit...");
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
