package io.github.mrmiumo.mi2gltf.mcmodel;

/**
 * Names to identify each 6 faces of a cube
 */
public enum FaceName {
    /** The front face */
    FRONT("north"),
    /** The right face */
    RIGHT("east"),
    /** The bottom face */
    BOTTOM("down"),
    /** The back face */
    BACK("south"),
    /** The left face */
    LEFT("west"),
    /** The top face */
    TOP("up");

    private final String cardinality;

    FaceName(String c) { cardinality = c; }

    /**
     * Gets the face having the given cardinality (north, east, ...)
     * @param cardinality the lowercase cardinality to search
     * @return the corresponding face or null if not found
     */
    public static FaceName from(String cardinality) {
        for (var v : values()) {
            if (v.cardinality.equals(cardinality)) return v;
        }
        return null;
    }
}
