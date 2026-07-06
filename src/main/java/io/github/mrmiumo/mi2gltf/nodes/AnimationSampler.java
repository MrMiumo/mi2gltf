package io.github.mrmiumo.mi2gltf.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * An animation sampler combines timestamps with a sequence of output
 * values and defines an interpolation algorithm.
 */
public class AnimationSampler {
    
    /** An accessor containing keyframe timestamps */
    private final Accessor input;

    /** An accessor containing keyframe output values */
    private final Accessor output;

    /** Interpolation algorithm */
    private final Interpolation interpolation; // LINEAR by default
    
    private int index = 0;

    public AnimationSampler(Accessor input, Accessor output, Interpolation interpolation) {
        this.input = input;
        this.output = output;
        this.interpolation = interpolation;
    }

    /**
     * Sets the ID of this sampler (eq. its index in the samplers array).
     * @param i the index of the sampler
     * @return this sampler
     */
    public AnimationSampler setIndex(int i) {
        index = i;
        return this;
    }

    /**
     * Gets the index of this sampler in the global samplers array
     * @return the index of this sampler
     */
    public int index() { return index; }

    /**
     * The index of the accessor containing keyframe timestamps
     * @return the index of the input accessor
     */
    @JsonGetter
    public int input() { return input.index(); }

    /**
     * The index of the accessor containing keyframe output values
     * @return the index of the output accessor
     */
    @JsonGetter
    public int output() { return output.index(); }

    /**
     * Interpolation algorithm: LINEAR or STEP or CUBIC
     * @return the interpolation algorithm
     */
    @JsonGetter
    public String interpolation() {
        return interpolation == null ? null : interpolation.toString();
    }

    public enum Interpolation {
        LINEAR, STEP, CUBICSPLINE;

        @Override
        public String toString() {
            return this == LINEAR ? null : name();
        }
    }
}
