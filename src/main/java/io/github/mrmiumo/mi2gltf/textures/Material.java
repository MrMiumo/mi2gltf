package io.github.mrmiumo.mi2gltf.textures;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Material that can be used to apply textures on a mesh
 */
public class Material {

    private final int index;

    private final Pbr pbrMetallicRoughness;

    private final String name;

    private final boolean tinted;

    /**
     * Wraps the given texture into a new material 
     * @param texture the texture to wrap
     * @param index the index of this material in the materials array
     * @param tinted whether this material applies to tintable faces
     */
    public Material(Texture texture, boolean tinted, int index) {
        pbrMetallicRoughness = new Pbr(new BaseTexture(texture.index()), 0, 1);
        this.index = index;
        this.tinted = tinted;
        if (texture.path() == null) {
            this.name = "#missing";
        } else {
            this.name = texture.path().getFileName().toString().replace(".png", "");
        }
    }

    /**
     * Gets the index of this material in the materials array
     * @return the index of this material in the materials array
     */
    public int index() { return index; }

    /**
     * The texture and reflection of the material
     * @return the PBR value of the material
     */
    @JsonGetter
    Pbr pbrMetallicRoughness() { return pbrMetallicRoughness; }

    /**
     * Enables strict alpha on this material (fully opaque or fully transparent)
     * @return "MASK"
     */
    @JsonGetter
    public String alphaMode() { return "MASK"; }

    /**
     * The alpha value bellow which the pixels are considered transparent
     * @return the alpha cutOff value
     */
    @JsonGetter
    public float alphaCutoff() { return 0.1f; }

    /**
     * The name of the material
     * @return the name of the material
     */
    @JsonGetter
    public String name() { return name + (tinted ? "!tinted" : ""); }

    /**
     * Data class to respect the GLTF format
     * @param baseColorTexture the texture
     * @param metallicFactor the metallic-ness
     * @param roughnessFactor the roughness
     */
    @JsonInclude(Include.NON_NULL)
    private record Pbr(BaseTexture baseColorTexture, float metallicFactor, float roughnessFactor) {}

    /**
     * Data class to respect the GLTF format
     * @param index the index of the texture in the textures array
     */
    private record BaseTexture(int index) {}
}
