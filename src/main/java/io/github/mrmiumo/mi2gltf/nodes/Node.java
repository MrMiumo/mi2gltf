package io.github.mrmiumo.mi2gltf.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.mrmiumo.mi2gltf.Vec;

/**
 * Represents an element of the model (a cube here)
 */
@JsonInclude(Include.NON_NULL)
public class Node {
    private int index = -1;
    private Mesh mesh = null;
    private Vec translation = null;
    private float[] rotation = null;

    /**
     * Sets the ID of this Node (eq. its index in the nodes array).
     * @param i the index of the node
     * @return this Node
     */
    public Node setIndex(int i) {
        index = i;
        return this;
    }

    /**
     * Gets the index of the this node in the nodes array
     * @return the index of the this node
     */
    public int index() { return index; }

    /**
     * Sets the mesh referenced by this node.
     * @param mesh the mesh to use in this node
     * @return this node
     */
    public Node setMesh(Mesh mesh) {
        this.mesh = mesh;
        return this;
    }

    /**
     * Gets the mesh of this node as a Java object
     * @return the mesh object
     */
    public Mesh rawMesh() {
        return mesh;
    }

    /**
     * Gets the index of this node mesh
     * @return the index of this node mesh
     */
    @JsonGetter
    public Integer mesh() {
        return mesh == null ? null : mesh.index();
    }

    /**
     * Sets the translation applied on this node (modifier).
     * @param translation the translation to apply in this node
     * @return this node
     */
    public Node translate(Vec translation) {
        this.translation = translation;
        return this;
    }

    /**
     * Gets the vector (nullable) of the translation of this node mesh
     * @return the translation vector of this node mesh
     */
    @JsonGetter
    public float[] translation() {
        return translation == null ? null : translation.toArray();
    }

    /**
     * Sets the rotation applied on this node (modifier).
     * @param rotation the rotation to apply in this node
     * @return this node
     */
    public Node rotate(float[] rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * Gets the quaternion (nullable) of the rotation of this node mesh
     * @return the rotation quaternion of this node mesh
     */
    @JsonGetter
    public float[] rotation() {
        return rotation;
    }
}

