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
}

