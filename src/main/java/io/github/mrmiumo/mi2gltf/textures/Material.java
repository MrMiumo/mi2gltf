package io.github.mrmiumo.mi2gltf.textures;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Material {

    private final int index;

    private final Pbr pbrMetallicRoughness;

    public Material(Texture texture, int index) {
        pbrMetallicRoughness = new Pbr(new BaseTexture(texture.index()), 0, 1);
        this.index = index;
    }

    public int index() { return index; }

    @JsonGetter
    Pbr pbrMetallicRoughness() { return pbrMetallicRoughness; }

    @JsonGetter
    public String alphaMode() { return "MASK"; }

    @JsonGetter
    public float alphaCutoff() { return 0.5f; }

    @JsonInclude(Include.NON_NULL)
    private record Pbr(BaseTexture baseColorTexture, float metallicFactor, float roughnessFactor) {}

    private record BaseTexture(int index) {}
}
