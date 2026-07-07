package io.github.mrmiumo.mi2gltf.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.github.mrmiumo.mi2gltf.textures.Material;

/**
 * Combines a sampler (keyframes) with a material to animate
 */
public class AnimationChannel {

    private final AnimationSampler sampler;

    private final Material material;
    
    public AnimationChannel(AnimationSampler sampler, Material material) {
        this.sampler = sampler;
        this.material = material;
    }

    /**
     * The index of the sampler containing keyframe timestamps
     * @return the index of the input sampler
     */
    @JsonGetter
    public int sampler() { return sampler.index(); }

    /**
     * The index of the sampler containing keyframe timestamps
     * @return the index of the input sampler
     */
    @JsonGetter
    public Target target() {
        var path = "/materials/" + material.index() + "/pbrMetallicRoughness/baseColorTexture/extensions/KHR_texture_transform/offset";
        var extension = new Extension(new Pointer(path));
        return new Target(extension, "pointer");
    }

    /**
     * Target of the animation
     * @param extensions the extension that point to the texture to animate
     * @param path the name of the animated property
     */
    public static record Target(Extension extensions, String path) {}

    static record Extension(Pointer KHR_animation_pointer) {}

    static record Pointer(String pointer) {}
}
