package io.github.mrmiumo.mi2gltf;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.mrmiumo.mi2gltf.nodes.Accessor;
import io.github.mrmiumo.mi2gltf.nodes.Buffer;
import io.github.mrmiumo.mi2gltf.nodes.BufferView;
import io.github.mrmiumo.mi2gltf.nodes.Mesh;
import io.github.mrmiumo.mi2gltf.nodes.Node;
import io.github.mrmiumo.mi2gltf.textures.Image;
import io.github.mrmiumo.mi2gltf.textures.Material;
import io.github.mrmiumo.mi2gltf.textures.Sampler;
import io.github.mrmiumo.mi2gltf.textures.Texture;

@JsonInclude(Include.NON_EMPTY)
public class Gltf {

    /** Header of the file with the GLTF version and generator name */
    private final Asset asset = new Asset("Miumo", "2.0");

    /** Index of the default scene (0 by default) */
    private int scene = 0;

    /** List of scenes (only one by default) */
    private Scene[] scenes = new Scene[]{ new Scene("Scene") };

    /** List of nodes (one per cube) */
    private ArrayList<Node> nodes = new ArrayList<>();

    /** List of meshes (initialized during build) */
    private List<Mesh> meshes = List.of();

    /** List of Accessors (initialized during build) */
    private List<Accessor> accessors = List.of();

    /** List of bufferViews (initialized during build) */
    private List<BufferView> views = List.of();

    /** List of buffers */
    private HashMap<String, Buffer> buffers = new HashMap<>();
    

    /** List of samplers (only one by default) */
    private Sampler[] samplers = new Sampler[]{ Sampler.instance };

    /** List of images */
    private List<Image> images = new ArrayList<>();

    /** List of textures */
    private List<Texture> textures = new ArrayList<>();

    /** List of materials */
    private HashMap<Path, Material> materials = new HashMap<>();

    // materials

    public Gltf build() {
        /* Meshes */
        meshes = nodes.stream().map(Node::rawMesh).filter(Objects::nonNull).toList();
        var i = 0 ;
        for (var mesh : meshes) mesh.setIndex(i++);

        /* Nodes */
        var scene0 = scenes[0];
        i = 0 ;
        for (var node : nodes) {
            node.setIndex(i++);
            if (node.isReferenced()) scene0.addNodes(node);
        }

        /* Buffers */
        buffers.values().forEach(Buffer::build);

        /* Views */
        views = buffers.values().stream()
            .flatMap(buff -> buff.getViews().stream())
            .toList();
        
        i = 0;
        for (var view : views) view.setIndex(i++);

        /* Accessors */
        accessors = views.stream()
            .flatMap(view -> view.getAccessors().stream())
            .toList();

        i = 0;
        for (var accessor : accessors) accessor.setIndex(i++);

        return this;
    }

    @JsonGetter
    public Asset asset() { return asset; }

    @JsonGetter
    public int scene() { return scene; }

    @JsonGetter
    public Scene[] scenes() { return scenes; }

    @JsonGetter
    public List<Node> nodes() { return nodes; }

    @JsonGetter
    public List<Mesh> meshes() { return meshes; }
    
    @JsonGetter
    public Collection<Buffer> buffers() {
        return buffers.values().stream()
            .sorted(Comparator.comparingInt(Buffer::index))
            .toList();}
    
    @JsonGetter
    public List<BufferView> bufferViews() { return views; }

    @JsonGetter
    public List<Accessor> accessors() { return accessors; }


    @JsonGetter
    public Sampler[] samplers() { return samplers; }

    @JsonGetter
    public List<Image> images() { return images; }

    @JsonGetter
    public List<Texture> textures() { return textures; }

    @JsonGetter
    public Collection<Material> materials() {
        return materials.values().stream()
            .sorted(Comparator.comparingInt(Material::index))
            .toList();
    }


    /**
     * Finds a buffer with the given key or creates a new one if not
     * existing yet.
     * @param key the key to get/save the buffer under
     * @return the buffer
     */
    public Buffer getBuffer(String key) {
        var buffer = buffers.get(key);
        if (buffer != null) return buffer;

        buffer = new Buffer().setIndex(buffers.size());
        buffers.put(key, buffer);
        return buffer;
    }

    /**
     * Finds a material with the given path or creates a new one if not
     * existing yet.
     * @param path the path of the material to get (image)
     * @return the material
     */
    public Material getMaterial(Path path) {
        return materials.computeIfAbsent(path, p -> {
            var image = new Image(p, images.size());
            images.add(image);
            var texture = new Texture(image, textures.size());
            textures.add(texture);
            return new Material(texture, materials.size());
        });
    }

    private record Asset(String generator, String version) {}
}
