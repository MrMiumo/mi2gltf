package io.github.mrmiumo.mi2gltf.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.mrmiumo.mi2gltf.textures.Material;

/**
 * Represent a group of triangles
 */
public class Mesh {
    private int index = -1;

    private final ArrayList<Primitive> primitives = new ArrayList<>();

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
     * Adds a new primitive to this mesh. A primitive is basically a
     * part of a mesh.
     * @param indices the ordered indices of positions to build triangles from
     * @param positions the [xyz] positions of all cube points
     * @param normals the normals of each points (used for lightning)
     * @param textures the UV mapping for each point
     * @param material the material to use for texturing
     * @return this mesh
     */
    public Mesh addPrimitive(Accessor indices, Accessor positions, Accessor normals, Accessor textures, Material material) {
        primitives.add(new Primitive(positions, normals, textures, indices, material));
        return this;
    }

    /**
     * GLTF data structure for mesh data
     * @return the mesh data as a primitive object
     */
    @JsonGetter
    List<Primitive> primitives() {
        return primitives;
    }

    /**
     * Represent a part of a mesh
     */
    @JsonInclude(Include.NON_NULL)
    private static class Primitive {
        private final Accessor positions;
        private final Accessor normals;
        private final Accessor textures;
        private final Accessor indices;
        private final Material material;

        /**
         * Creates a new Primitive with the given accessors that
         * defines the shape positions and texture.
         * @param positions the positions accessor
         * @param normals the normals accessor
         * @param textures the textures accessor
         * @param indices the indices accessor
         * @param material the material
         */
        public Primitive(Accessor positions, Accessor normals, Accessor textures, Accessor indices, Material material) {
            this.positions = positions;
            this.normals = normals;
            this.textures = textures;
            this.indices = indices;
            this.material = material;
        }
    
        
        /**
         * Gets the attributes of this primitive (positions, normals and textures)
         * @return this primitive attributes
         */
        @JsonGetter
        public Attributes attributes() {
            return new Attributes(positions.index(), normals.index(), textures.index());
        }
        
        /**
         * Gets index of the indices accessor
         * @return the index of the indices accessor
         */
        @JsonGetter
        public int indices() {
            return indices.index();
        }
        
        /**
         * Gets index of the material
         * @return the index of the material
         */
        @JsonGetter
        public int material() {
            return material.index();
        }
    }

    /**
     * GLTF data structure for meshes
     * @param POSITION the positions accessor index
     * @param NORMAL the normals accessor index
     * @param TEXCOORD_0 the UV mapping accessor index
     */
    private record Attributes(int POSITION, int NORMAL, int TEXCOORD_0) {}
}
