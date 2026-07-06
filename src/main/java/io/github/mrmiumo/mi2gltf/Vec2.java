package io.github.mrmiumo.mi2gltf;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Represent a 2D vector
 * @param x the horizontal component of the 3D vector
 * @param y the vertical component of the 3D vector
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public record Vec2(float x, float y) {

    public static final Vec2 ZERO = new Vec2(0, 0);

    /**
     * Converts this vector into a 3 float long array
     * @return the corresponding array
     */
    public float[] toArray() {
        return new float[]{x, y};
    }

    /**
     * Creates a new vector resulting of the subtraction of the given
     * vector to this one.
     * @param other the vector to subtract from this one
     * @return the subtraction result
     */
    public Vec2 sub(Vec2 other) {
        return new Vec2(x - other.x, y - other.y);
    }
}

