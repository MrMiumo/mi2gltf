package io.github.mrmiumo.mi2gltf.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.mrmiumo.mi2gltf.textures.Material;

/**
 * Represent a group of triangles
 */
public class Mesh {
    private int index = -1;

    private final Accessor indices;
    private final Accessor positions;
    private final Accessor normals;
    private final Accessor textures;
    private final Material material;

    /**
     * Creates a new mesh with the given accessors
     * @param indices the ordered indices of positions to build triangles from
     * @param positions the [xyz] positions of all cube points
     * @param normals the normals of each points (used for lightning)
     * @param textures the UV mapping for each point
     * @param material the material to use for texturing
     */
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

    /**
     * Gets the index of this mesh in the meshes array
     * @return the index of this mesh
     */
    public int index() { return index; }

    /**
     * GLTF data structure for mesh data
     * @return the mesh data as a primitive object
     */
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

    /**
     * GLTF data structure for meshes
     * @param attributes the positions, normals and textures accessors indexes
     * @param indices the indices accessor index
     * @param material the material index (nullable)
     */
    @JsonInclude(Include.NON_NULL)
    private record Primitive(Attributes attributes, int indices, Integer material) {}

    /**
     * GLTF data structure for meshes
     * @param POSITION the positions accessor index
     * @param NORMAL the normals accessor index
     * @param TEXCOORD_0 the UV mapping accessor index
     */
    private record Attributes(int POSITION, int NORMAL, int TEXCOORD_0) {}
}
