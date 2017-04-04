package com.github.njuro.updatrrr;

/**
 * Enum representing available themes for UpdatRRR GUI
 *
 * @author njuro
 */
public enum Theme {
    DARK, LIGHT;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
