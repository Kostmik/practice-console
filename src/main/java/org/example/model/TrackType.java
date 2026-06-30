package org.example.model;

public enum TrackType {
    LINKED("Звеньевой"),
    CONTINUOUS("Бесстыковой");

    private final String displayName;

    TrackType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}