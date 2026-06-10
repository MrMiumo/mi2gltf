package io.github.mrmiumo.mi2gltf;

public enum FaceName {
    FRONT("north"),
    RIGHT("east"),
    BOTTOM("down"),
    BACK("south"),
    LEFT("west"),
    TOP("up");

    private final String cardinality;

    FaceName(String c) { cardinality = c; }

    public static FaceName from(String cardinality) {
        for (var v : values()) {
            if (v.cardinality.equals(cardinality)) return v;
        }
        return null;
    }
}
