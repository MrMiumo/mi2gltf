package io.github.mrmiumo.mi2gltf.mcmodel;

import java.util.Collection;
import java.util.EnumMap;

import io.github.mrmiumo.mi2gltf.Vec;

/**
 * Represent a Minecraft cube with its position and textures.
 */
public class Cube {

    private final Vec from;
    
    private final Vec to;
    
    private float angle = 0;
    private char axis = 0;
    private Vec pivot = new Vec(0, 0, 0);
    private final EnumMap<FaceName, Face> faces = new EnumMap<>(FaceName.class);

    /**
     * Creates a new cube of the given size
     * @param from the first corner of the cube
     * @param to the opposite corner
     */
    public Cube(Vec from, Vec to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Gets the 'from' corner coordinates
     * @return the 'from' corner coordinates
     */
    public Vec from() { return from; }
    
    /**
     * Gets the 'to' corner coordinates
     * @return the 'to' corner coordinates
     */
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

    /**
     * Gets the 'angle' of the cube
     * @return the 'angle' of the cube
     */
    public float angle() { return angle; }
    
    /**
     * Gets the name of the axis to rotate around (0) if not set
     * @return the name of the axis to rotate around
     */
    public char axis() { return axis; }

    /**
     * Sets the rotation pivot point
     * @param pivot the coordinates of the pivot point
     * @return this cube
     */
    public Cube pivot(Vec pivot) {
        this.pivot = pivot;
        return this;
    }

    /**
     * Gets the coordinates of the rotation origin (nullable)
     * @return the coordinates of the pivot point
     */
    public Vec pivot() { return pivot; }

    /**
     * Sets a texture on a face of the cube
     * @param face the face to set the texture for
     * @param texture the texture
     * @param rotate the rotation of the texture 
     * @param fromX the coordinate of the texture UV beginning (base 16px)
     * @param fromY the coordinate of the texture UV beginning (base 16px)
     * @param toX the coordinate of the texture UV ending (base 16px)
     * @param toY the coordinate of the texture UV ending (base 16px)
     * @return this cube
     */
    public Cube texture(FaceName face, ModelTexture texture, int rotate, float fromX, float fromY, float toX, float toY) {
        faces.put(face, new Face(face, fromX/16, fromY/16, toX/16, toY/16, rotate, texture));
        return this;
    }

    /**
     * Gets all the faces of the cube
     * @return this cube faces
     */
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

    /**
     * A cube face
     * @param name the side of the cube
     * @param fromX the coordinate of the texture UV beginning (percent of width)
     * @param fromY the coordinate of the texture UV beginning (percent of height)
     * @param toX the coordinate of the texture UV ending (percent of width)
     * @param toY the coordinate of the texture UV ending (percent of height)
     * @param rotation the rotation of the texture 
     * @param texture the texture
     */
    public record Face(FaceName name, float fromX, float fromY, float toX, float toY, int rotation, ModelTexture texture) {}
}