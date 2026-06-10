package io.github.mrmiumo.mi2gltf;

/**
 * Represent a 3D vector
 * @param x the horizontal component of the 3D vector
 * @param y the vertical component of the 3D vector
 * @param z the depth component of the 3D vector
 */
public record Vec(float x, float y, float z) {
    /**
     * Converts this vector into a 3 float long array
     * @return the corresponding array
     */
    public float[] toArray() {
        return new float[]{x, y, z};
    }

    /**
     * Creates a new vector resulting of the subtraction of the given
     * vector to this one.
     * @param other the vector to subtract from this one
     * @return the subtraction result
     */
    public Vec sub(Vec other) {
        return new Vec(x - other.x, y - other.y, z - other.z);
    }
}

