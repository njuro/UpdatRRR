package com.github.njuro.updatrrr.exceptions;

import java.io.File;
import java.io.IOException;

/**
 * Exception for illegal operations with StylRRR database file
 *
 * @author njuro
 */
public class DatabaseFileException extends IOException {
    private File databaseFile;

    public DatabaseFileException(String message, File databaseFile) {
        super(message);
        this.databaseFile = databaseFile;
    }

    public File getDatabaseFile() {
        return databaseFile;
    }
}
