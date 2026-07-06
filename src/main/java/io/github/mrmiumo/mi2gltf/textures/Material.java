package io.github.mrmiumo.mi2gltf.textures;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.mrmiumo.mi2gltf.Vec2;

/**
 * Material that can be used to apply textures on a mesh
 */
public class Material {

    private final int index;

    private final Texture texture;

    private final String name;

    private final boolean tinted;

    private Extension extension = null;

    /**
     * Wraps the given texture into a new material 
     * @param texture the texture to wrap
     * @param index the index of this material in the materials array
     * @param tinted whether this material applies to tintable faces
     */
    public Material(Texture texture, boolean tinted, int index) {
        this.texture = texture;
        this.index = index;
        this.tinted = tinted;
        if (texture.path() == null) {
            this.name = "#missing";
        } else {
            this.name = texture.path().getFileName().toString().replace(".png", "");
        }
    }

    /**
     * Adds the KHR_texture_transform extension to this material.
     * @param frames the number of frames contained in the atlas
     * @return this material
     */
    public Material setTextureTransform(int frames) {
        extension = new Extension(new TextureTransform(Vec2.ZERO, new Vec2(1, 1f/frames)));
        return this;
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
    Pbr pbrMetallicRoughness() {
        return new Pbr(new BaseTexture(extension, texture.index()), 0, 1);
    }

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
     * @param extensions the extensions of the material
     * @param index the index of the texture in the textures array
     */
    @JsonInclude(Include.NON_NULL)
    private record BaseTexture(Extension extensions, int index) {}

    private record Extension(TextureTransform KHR_texture_transform) {}
    private record TextureTransform(Vec2 offset, Vec2 scale) {}
}
