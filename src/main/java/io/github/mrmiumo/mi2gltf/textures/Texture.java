package io.github.mrmiumo.mi2gltf.textures;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Link between an image and a sampler.
 */
public class Texture {

    private final int index;

    private final Image source;

    /**
     * Creates a new texture from the given image using the default sampler
     * @param source the image to user
     * @param index the index of this texture in the textures array
     */
    public Texture(Image source, int index) {
        this.source = source;
        this.index = index;
    }

    /**
     * Gets the index of this texture in the textures array
     * @return the index of this texture in the textures array
     */
    public int index() { return index; }

    /**
     * Gets the index of the the wrapped image
     * @return the index of this texture's image
     */
    @JsonGetter
    public int source() { return source.index(); }

    /**
     * Gets the index of the the wrapped sampler
     * @return the index of this texture's sampler
     */
    @JsonGetter
    public int sampler() { return Sampler.instance.index(); }
}
