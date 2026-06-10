package io.github.mrmiumo.mi2gltf;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Node {
    private int index = -1;
    private Mesh mesh = null;
    private Vec translation = null;
    private float[] rotation = null;
    private ArrayList<Node> children = new ArrayList<>();

    private boolean referenced = true;

    /**
     * Sets the ID of this Node (eq. its index in the nodes array).
     * @param i the index of the node
     * @return this Node
     */
    public Node setIndex(int i) {
        index = i;
        return this;
    }

    public int index() { return index; }

    /**
     * Indicates whether this node should be referenced in the scene
     * or not.
     * @param referenced true to reference this node, false otherwise
     * @return this node
     */
    public Node setReferenced(boolean referenced) {
        this.referenced = referenced;
        return this;
    }

    /**
     * Whether this Node should be referenced in the scene or not.
     * @return true if the node should be referenced in the scene
     */
    @JsonIgnore
    public boolean isReferenced() {
        return referenced;
    }

    /**
     * Sets the mesh referenced by this node.
     * @param mesh the mesh to use in this node
     * @return this node
     */
    public Node setMesh(Mesh mesh) {
        this.mesh = mesh;
        return this;
    }

    public Mesh rawMesh() {
        return mesh;
    }

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

    @JsonGetter
    public float[] rotation() {
        return rotation;
    }

    /**
     * Adds a child to this node.
     * @param child the child to add to this node
     * @return this node
     */
    public Node addChild(Node child) {
        children.add(child);
        return this;
    }

    @JsonGetter
    @SuppressWarnings("java:S1168")
    public int[] children() {
        if (children.isEmpty()) return null;
        return children.stream().mapToInt(n -> n.index).toArray();
    }
}

