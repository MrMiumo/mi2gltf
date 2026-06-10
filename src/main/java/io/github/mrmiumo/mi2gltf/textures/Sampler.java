package io.github.mrmiumo.mi2gltf.textures;

import com.fasterxml.jackson.annotation.JsonGetter;

@SuppressWarnings("java:S6548")
public class Sampler {

    public static final Sampler instance = new Sampler();

    // Already using default values = 10497!
    // private final int wrapS = 10497; // REPEAT(10497) or CLAMP_TO_EDGE(33071) or MIRRORED_REPEAT(33648)
    // private final int wrapT = 10497; // REPEAT(10497) or CLAMP_TO_EDGE(33071) or MIRRORED_REPEAT(33648)


    public int index() { return 0; }

    @JsonGetter
    public int magFilter() { return 9728; } // NEAREST(9728) or LINEAR(9729)

    @JsonGetter
    public int minFilter() { return 9728; } // NEAREST(9728) or LINEAR(9729) or MipMap(9984 - 9987)
}
