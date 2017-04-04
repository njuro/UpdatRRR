package com.github.njuro.updatrrr;

public enum Theme {
    DARK, LIGHT;


    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
