package org.example.model;

public enum RebarType {
    SMOOTH("Гладкая", 190.0),      // А240
    RIBBED("Периодического профиля", 240.0); // А400

    private final String displayName;
    private final double rs; // МПа

    RebarType(String displayName, double rs) {
        this.displayName = displayName;
        this.rs = rs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getRs() {
        return rs;
    }

    @Override
    public String toString() {
        return displayName + " (Rₛ = " + rs + " МПа)";
    }
}
