package io.github.mrmiumo.mi2gltf.textures;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Sampler is used to define how the image is scaled. Here, only one
 * sampler is used. It prevent tiling and use strict scaling methods
 * to keep the pixel perfect look of Minecraft.
 */
@SuppressWarnings("java:S6548")
public class Sampler {

    /** The default Sampler instance */
    public static final Sampler instance = new Sampler();

    private Sampler() {
        // Prevent other instances from being created
    }

    /**
     * The index of this sampler in the samplers array
     * @return 0 since only one sampler is allowed
     */
    public int index() { return 0; }

    /**
     * The magnification method to use (when scaling the image up)
     * @return the magFilter code
     */
    @JsonGetter
    public int magFilter() { return 9728; } // NEAREST(9728) or LINEAR(9729)

    /**
     * The minification method to use (when scaling the image down)
     * @return the minFilter code
     */
    @JsonGetter
    public int minFilter() { return 9728; } // NEAREST(9728) or LINEAR(9729) or MipMap(9984 - 9987)
    
    /**
     * The wrapping mode to use (horizontally). REPEAT by default
     * @return the S wrapping mode
     */
    @JsonGetter
    public int wrapS() { return 33071; } // CLAMP_TO_EDGE(33071) or MIRRORED_REPEAT(33648) or REPEAT(10497)
    
    /**
     * The wrapping mode to use (vertically). REPEAT by default
     * @return the T wrapping mode
     */
    @JsonGetter
    public int wrapT() { return 33071; } // CLAMP_TO_EDGE(33071) or MIRRORED_REPEAT(33648) or REPEAT(10497)
}
