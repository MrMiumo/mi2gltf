package io.github.mrmiumo.mi2gltf.nodes;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.mrmiumo.mi2gltf.mcmodel.ModelAnimation;
import io.github.mrmiumo.mi2gltf.nodes.Accessor.ComponentType;
import io.github.mrmiumo.mi2gltf.nodes.Accessor.Type;
import io.github.mrmiumo.mi2gltf.nodes.AnimationSampler.Interpolation;
import io.github.mrmiumo.mi2gltf.nodes.BufferView.Target;
import io.github.mrmiumo.mi2gltf.textures.Material;

/**
 * Structure used to animate textures
 */
@JsonInclude(Include.NON_EMPTY)
public class Animation {

    private final List<AnimationSampler> samplers;
    
    private final List<AnimationChannel> channels;

    /**
     * Converts a ModelAnimation into a GLTF Animation
     * @param animation the Minecraft animation data
     * @param material the material to animate
     * @param buffer the buffer to store the animation into
     */
    public Animation(ModelAnimation animation, Material material, Buffer buffer) {
        var sampler = sampler(animation , buffer);
        material.setTextureTransform(animation.atlasSize());
        samplers = List.of(sampler);
        channels = List.of(new AnimationChannel(sampler, material));
    }

    /**
     * Creates a new AnimationSampler and fill it with the keyframes of
     * the given animation.
     * @param animation the animation to convert into an AnimationSampler
     * @param buffer the buffer to store the keyframes into
     * @return the sampler
     */
    private static AnimationSampler sampler(ModelAnimation animation, Buffer buffer) {
        /* Initialize buffers */
        var ft = 0.05f * animation.frametime(); // frametime in seconds
        var view = buffer.newView(null);
        var input = view.newAccessor(Type.SCALAR, ComponentType.FLOAT);
        var output = view.newAccessor(Type.VEC2, ComponentType.FLOAT);

        /* Fill buffers */
        var prev = -1;
        var y = 1f / animation.atlasSize();
        var t = 0f; // Time pointer
        for (var i : animation.frames()) {
            if (prev != i) {
                input.add(t);
                output.add(0f, i * y);
            }
            t += ft;
        }

        // TODO Generate missing frames if interpolate==true && frametime>1
        // This should be done in ModelAnimation

        return new AnimationSampler(input, output, Interpolation.STEP);
    }


    /**
     * Gets the samplers used by this animation (keyframes)
     * @return the samplers
     */
    @JsonGetter
    public List<AnimationSampler> samplers() { return samplers; }

    /**
     * Gets this animations channels (link between sampler and material)
     * @return the channels
     */
    @JsonGetter
    public List<AnimationChannel> channels() { return channels; }
}
