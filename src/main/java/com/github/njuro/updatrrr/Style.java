package com.github.njuro.updatrrr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Entity for userstyle as represented in StylRRR JSON DB file.
 *
 * @author njuro
 */
public class Style {
    //Date pattern used by StylRRR (must be used with Locale set to English)
    public static final String DATE_PATTERN = "MMM dd, yyyy";

    //attributes must have same name as in StylRRR file to make JSON Object mapping possible
    private int id;
    private String namE = "-";
    private String author = "-";
    private String url = "-";
    private String datE = "-";
    private String stylE = "-";
    private boolean enabled;

    public Style() {
    }

    //converts string representation of date to date object
    public static Date parseStringToDate(String date) {
        if (date == null) {
            return new Date(0L);
        }
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
            return dateFormatter.parse(date);
        } catch (ParseException pse) {
            System.err.printf("Date %s is in invald format (using pattern: %s)\n", date, DATE_PATTERN);
            return new Date(0L);
        }
    }

    //converts date to string representation
    public static String parseDateToString(Date date) {
        if (date == null) {
            return parseDateToString(new Date(0L));
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
        return dateFormatter.format(date);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return namE;
    }

    public void setName(String namE) {
        this.namE = namE;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getDate() {
        return Style.parseStringToDate(datE);
    }

    public void setDate(String datE) {
        this.datE = datE;
    }

    public String getDateString() {
        return (datE == null || datE.isEmpty()) ? "Unknown" : datE;
    }

    public String getCode() {
        return stylE;
    }

    public void setCode(String code) {
        this.stylE = code;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return namE + ":" +
                "\n\tAuthor: " + author +
                "\n\tURL: " + url +
                "\n\tLast updated: " + datE;
    }
}
