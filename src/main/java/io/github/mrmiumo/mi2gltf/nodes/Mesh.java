package io.github.mrmiumo.mi2gltf.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.mrmiumo.mi2gltf.textures.Material;

public class Mesh {
    private int index = -1;

    private final Accessor indices;
    private final Accessor positions;
    private final Accessor normals;
    private final Accessor textures;
    private final Material material;

    public Mesh(Accessor indices, Accessor positions, Accessor normals, Accessor textures, Material material) {
        this.indices = indices;
        this.positions = positions;
        this.normals = normals;
        this.textures = textures;
        this.material = material;
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
    Primitive[] primitives() {
        return new Primitive[] {
            new Primitive(
                new Attributes(positions.index(), normals.index(), textures.index()),
                indices.index(),
                material == null ? null : material.index()
            )
        };
    }

    @JsonInclude(Include.NON_NULL)
    private record Primitive(Attributes attributes, int indices, Integer material) {}

    private record Attributes(int POSITION, int NORMAL, int TEXCOORD_0) {}
}
