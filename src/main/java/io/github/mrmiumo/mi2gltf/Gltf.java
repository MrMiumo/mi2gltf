package io.github.mrmiumo.mi2gltf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.mrmiumo.mi2gltf.mcmodel.ModelTexture;
import io.github.mrmiumo.mi2gltf.nodes.Accessor;
import io.github.mrmiumo.mi2gltf.nodes.Animation;
import io.github.mrmiumo.mi2gltf.nodes.Buffer;
import io.github.mrmiumo.mi2gltf.nodes.BufferView;
import io.github.mrmiumo.mi2gltf.nodes.Mesh;
import io.github.mrmiumo.mi2gltf.nodes.Node;
import io.github.mrmiumo.mi2gltf.textures.Image;
import io.github.mrmiumo.mi2gltf.textures.Material;
import io.github.mrmiumo.mi2gltf.textures.Sampler;
import io.github.mrmiumo.mi2gltf.textures.Texture;

/**
 * Main GLTF json file structure
 */
@JsonInclude(Include.NON_EMPTY)
public class Gltf {

    /** Header of the file with the GLTF version and generator name */
    private final Asset asset = new Asset("Mi²Gltf", "2.0");

    /** List of used extensions in this GLTF */
    private final Set<String> extensionsUsed = new HashSet<>();

    /** List of extensions required by this GLTF */
    private final Set<String> extensionsRequired = new HashSet<>();

    /** List of scenes (only one by default) */
    private final Scene[] scenes = new Scene[]{ new Scene("Scene") };

    /** List of nodes (one per cube) */
    private final ArrayList<Node> nodes = new ArrayList<>();

    /** List of meshes (initialized during build) */
    private List<Mesh> meshes = List.of();

    /** List of Accessors (initialized during build) */
    private List<Accessor> accessors = List.of();

    /** List of bufferViews (initialized during build) */
    private List<BufferView> views = List.of();

    /** List of buffers */
    private final HashMap<String, Buffer> buffers = new HashMap<>();
    

    /** List of animations */
    private final List<Animation> animations = new ArrayList<>();

    /** List of samplers (only one by default) */
    private final List<Sampler> samplers = new ArrayList<>();

    /** List of images */
    private final List<Image> images = new ArrayList<>();

    /** List of textures */
    private final LinkedHashMap<String, Texture> textures = new LinkedHashMap<>();

    /** List of materials */
    private final LinkedHashMap<String, Material> materials = new LinkedHashMap<>();

    Gltf() {
        samplers.add(Sampler.instance);
    }

    /**
     * Prepare the data to be serialized into json.
     * @return this Gltf
     */
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

    /**
     * Register a new used extension in the file by its name
     * @param extension the name of the extension
     * @return this GLTF
     */
    public Gltf useExtension(String extension) {
        extensionsUsed.add(extension);
        return this;
    }

    /**
     * Register a new required extension in the file by its name
     * @param extension the name of the extension
     * @return this GLTF
     */
    public Gltf requireExtension(String extension) {
        extensionsRequired.add(extension);
        return this;
    }

    /**
     * Gets the file header
     * @return the asset section
     */
    @JsonGetter
    Asset asset() { return asset; }

    /**
     * Gets the used extensions list
     * @return the list of used extensions
     */
    @JsonGetter
    @JsonInclude(Include.NON_EMPTY)
    Set<String> extensionsUsed() { return extensionsUsed; }

    /**
     * Gets the required extensions list
     * @return the list of required extensions
     */
    @JsonGetter
    @JsonInclude(Include.NON_EMPTY)
    Set<String> extensionsRequired() { return extensionsRequired; }

    /**
     * Gets the index of the main scene
     * @return the index of the main scene
     */
    @JsonGetter
    public int scene() { return 0; }

    /**
     * Gets the list of scenes
     * @return the list of scenes
     */
    @JsonGetter
    public Scene[] scenes() { return scenes; }

    /**
     * Gets the list of nodes
     * @return the list of nodes
     */
    @JsonGetter
    public List<Node> nodes() { return nodes; }

    /**
     * Gets the list of meshes
     * @return the list of meshes
     */
    @JsonGetter
    public List<Mesh> meshes() { return meshes; }
    
    /**
     * Gets the list of buffers
     * @return the list of buffers
     */
    @JsonGetter
    public Collection<Buffer> buffers() {
        return buffers.values().stream()
            .sorted(Comparator.comparingInt(Buffer::index))
            .toList();}
    
    /**
     * Gets the list of bufferViews
     * @return the list of bufferViews
     */
    @JsonGetter
    public List<BufferView> bufferViews() { return views; }

    /**
     * Gets the list of accessors
     * @return the list of accessors
     */
    @JsonGetter
    public List<Accessor> accessors() { return accessors; }

    /**
     * Gets the list of animations
     * @return the list of animations
     */
    @JsonGetter
    public List<Animation> animations() { return animations; }

    /**
     * Gets the list of samplers
     * @return the list of samplers
     */
    @JsonGetter
    public List<Sampler> samplers() { return samplers; }

    /**
     * Gets the list of images
     * @return the list of images
     */
    @JsonGetter
    public List<Image> images() { return images; }

    /**
     * Gets the list of textures
     * @return the list of textures
     */
    @JsonGetter
    public Collection<Texture> textures() { 
        return textures.values().stream()
            .sorted(Comparator.comparingInt(Texture::index))
            .toList();
     }

    /**
     * Gets the list of materials
     * @return the list of materials
     */
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
     * Finds a material with the given texture or creates a new one if
     * not existing yet.
     * @param model the texture of the material to get (image)
     * @param tinted whether the material is used on tintable faces or not
     * @return the material
     */
    public Material getMaterial(ModelTexture model, boolean tinted) {
        var key = model.path().toString();
        if (tinted) key += "!tinted";
        return materials.computeIfAbsent(key, k -> {
            var texture = getTexture(model);
            var material = new Material(texture, tinted, materials.size());
            if (model.animation != null) {
                animations.add(new Animation(
                    model.animation, material, getBuffer("animation")));
            }
            return material;
        });
    }

    /**
     * Finds a texture with the given ModelTexture or creates a new
     * one if not existing yet.
     * @param model the ModelTexture to get (image)
     * @return the texture
     */
    public Texture getTexture(ModelTexture model) {
        var key = model.path().toString();
        
        return textures.computeIfAbsent(key, k -> {
            var image = model.toImage(images.size());
            images.add(image);
            return new Texture(image, textures.size());
        });
    }

    /**
     * Format of the file header
     * @param generator author mark
     * @param version GLTF file version
     */
    private record Asset(String generator, String version) {}
}
