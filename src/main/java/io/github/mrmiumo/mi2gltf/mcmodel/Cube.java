package io.github.mrmiumo.mi2gltf.mcmodel;

import java.util.Collection;
import java.util.EnumMap;

import io.github.mrmiumo.mi2gltf.Vec;

public class Cube {

    private final Vec from;
    
    private final Vec to;
    
    private float angle = 0;
    private char axis = 0;
    private Vec pivot = new Vec(0, 0, 0);
    private final EnumMap<FaceName, Face> faces = new EnumMap<>(FaceName.class);

    public Cube(Vec from, Vec to) {
        this.from = from;
        this.to = to;
    }

    public Vec from() { return from; }
    public Vec to() { return to; }

    /**
     * Sets the rotation of this cube.
     * @param angle the amount of rotation to set
     * @param axis the axis on which the rotation is applied
     * @return this cube
     */
    public Cube rotate(float angle, char axis) {
        this.angle = angle;
        this.axis = axis;
        return this;
    }

    public float angle() { return angle; }
    public char axis() { return axis; }

    /**
     * Sets the rotation pivot point
     * @param pivot the coordinates of the pivot point
     * @return
     */
    public Cube pivot(Vec pivot) {
        this.pivot = pivot;
        return this;
    }

    public Vec pivot() { return pivot; }

    public Cube texture(FaceName face, ModelTexture texture, int rotate, float fromX, float fromY, float toX, float toY) {
        faces.put(face, new Face(face, fromX/16, fromY/16, toX/16, toY/16, rotate, texture));
        return this;
    }

    public Collection<Face> faces() {
        return faces.values();
    }

    /**
     * Gets the rotation as a quaternion [x,y,z,w] if a rotation is set.
     * @return the rotation quaternion, or null if no rotation is set
     */
    @SuppressWarnings("java:S1168")
    public float[] quaternion() {
        float rad = (float) Math.toRadians(angle);
        if (axis == 'x') return new float[]{ sin2(rad), 0, 0, cos2(rad) };
        if (axis == 'y') return new float[]{ 0, sin2(rad), 0, cos2(rad) };
        if (axis == 'z') return new float[]{ 0, 0, sin2(rad), cos2(rad) };
        return null;
    }

    private static float cos2(float a) { return (float)Math.cos(a/2); }
    private static float sin2(float a) { return (float)Math.sin(a/2); }

    public record Face(FaceName name, float fromX, float fromY, float toX, float toY, int rotation, ModelTexture texture) {}
}