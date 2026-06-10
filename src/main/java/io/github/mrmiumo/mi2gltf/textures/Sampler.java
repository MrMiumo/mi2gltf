package io.github.mrmiumo.mi2gltf.textures;

import com.fasterxml.jackson.annotation.JsonGetter;

@SuppressWarnings("java:S6548")
public class Sampler {

    public static final Sampler instance = new Sampler();

    public int index() { return 0; }

    @JsonGetter
    public int magFilter() { return 9728; } // NEAREST(9728) or LINEAR(9729)

    @JsonGetter
    public int minFilter() { return 9728; } // NEAREST(9728) or LINEAR(9729) or MipMap(9984 - 9987)
}
