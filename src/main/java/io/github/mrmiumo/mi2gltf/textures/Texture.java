package io.github.mrmiumo.mi2gltf.textures;

import com.fasterxml.jackson.annotation.JsonGetter;

public class Texture {

    private final int index;

    private final Image source;

    public Texture(Image source, int index) {
        this.source = source;
        this.index = index;
    }

    public int index() { return index; }

    @JsonGetter
    public int source() { return source.index(); }

    @JsonGetter
    public int sampler() { return Sampler.instance.index(); }
}
