package com.github.njuro.updatrrr.exceptions;

import com.github.njuro.updatrrr.Style;

/**
 * Exception for illegal values of styles' attributes
 *
 * @author njuro
 */
public class StyleException extends IllegalArgumentException {
    private Style style;

    public StyleException(String message, Style style) {
        super(message);
        this.style = style;
    }

    public Style getStyle() {
        return style;
    }
}
