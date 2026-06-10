package io.github.mrmiumo.mi2gltf;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Mesh {
    private int index = -1;

    private final Accessor indices;
    private final Accessor positions;
    private final Accessor normals;
    
    // private Integer material; // material ID

    public Mesh(Accessor indices, Accessor positions, Accessor normals) {
        this.indices = indices;
        this.positions = positions;
        this.normals = normals;
    }

    /**
     * Sets the ID of this Mesh (eq. its index in the meshes array).
     * @param i the index of the mesh
     * @return this Mesh
     */
    public Mesh setIndex(int i) {
        index = i;
        return this;
    }

    public int index() { return index; }

    @JsonGetter
    public Primitive[] primitives() {
        return new Primitive[] {
            new Primitive(
                new Attributes(positions.index(), normals.index()),
                indices.index(),
                null
            )
        };
    }

    @JsonInclude(Include.NON_NULL)
    private record Primitive(Attributes attributes, int indices, Integer material) {}

    private record Attributes(int POSITION, int NORMAL) {}
}

