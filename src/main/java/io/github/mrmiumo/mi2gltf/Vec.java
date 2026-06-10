package io.github.mrmiumo.mi2gltf;

public record Vec(float x, float y, float z) {
    public float[] toArray() {
        return new float[]{x, y, z};
    }

    public Vec sub(Vec other) {
        return new Vec(x - other.x, y - other.y, z - other.z);
    }
}

