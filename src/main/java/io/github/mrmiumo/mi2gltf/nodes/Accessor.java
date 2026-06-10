package io.github.mrmiumo.mi2gltf.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.mrmiumo.mi2gltf.ByteArray;

/**
 * Sort of masks used to read chunks of BufferViews.
 */
@JsonInclude(Include.NON_EMPTY)
public class Accessor {
    
    private int index = -1;

    private final BufferView bufferView;

    private int byteOffset = 0;

    /** How many elements it contains */
    private int count = 0;

    /** The layout of values that defines an element */
    private final Type type;

    /** The type of values stored */
    private final ComponentType componentType;

    private final ByteArray data = new ByteArray();

    /** The minimal value(s) of this accessor */
    private final float[] min;

    /** The maximal value(s) of this accessor */
    private final float[] max;

    Accessor(BufferView bufferView, Type type, ComponentType componentType) {
        this.bufferView = bufferView;
        this.type = type;
        this.componentType = componentType;

        var vMax = componentType == ComponentType.FLOAT ? Float.MAX_VALUE : Short.MAX_VALUE;
        var vMin = -vMax;
        var len = type.size;
        this.min = new float[len];
        this.max = new float[len];
        for (var i = 0 ; i < len ; i++) {
            min[i] = vMax;
            max[i] = vMin;
        }
    }

    /**
     * Adds new values to this accessor. Warning, it must fit the set
     * type and componentType.
     * @param floats the values to add
     * @return this array
     */
    public Accessor add(float... floats) {
        /* Preconditions */
        if (componentType != ComponentType.FLOAT) {
            throw new IllegalArgumentException("Cannot add floats to an UNSIGNED_SHORT accessor");
        }
        if (floats.length % type.size != 0) {
            throw new IllegalArgumentException("Input data doesn't match set type: " + type);
        }

        /* Values saving + min/max */
        var mod = type.size;
        for (var i = 0 ; i < floats.length ; i++) {
            data.addFloatLE(floats[i]);
            if (this.min[i % mod] > floats[i]) this.min[i % mod] = floats[i];
            if (this.max[i % mod] < floats[i]) this.max[i % mod] = floats[i];
        }
        count += floats.length / mod;
        return this;
    }

    /**
     * Adds new values to this accessor. Warning, it must fit the set
     * type and componentType.
     * @param shorts the values to add
     * @return this array
     */
    public Accessor add(short... shorts) {
        /* Preconditions */
        if (componentType != ComponentType.UNSIGNED_SHORT) {
            throw new IllegalArgumentException("Cannot add shorts to an FLOAT accessor");
        }
        if (shorts.length % type.size != 0) {
            throw new IllegalArgumentException("Input data doesn't match set type: " + type);
        }

        /* Values saving + min/max */
        var mod = type.size;
        for (var i = 0 ; i < shorts.length ; i++) {
            data.addShortLE(shorts[i]);
            if (this.min[i % mod] > shorts[i]) this.min[i % mod] = shorts[i];
            if (this.max[i % mod] < shorts[i]) this.max[i % mod] = shorts[i];
        }
        count += shorts.length / mod;
        return this;
    }

    /**
     * Adds new values to this accessor. Warning, it must fit the set
     * type and componentType.
     * @param shorts the values to add
     * @return this array
     */
    public Accessor add(int... shorts) {
        /* Preconditions */
        if (componentType != ComponentType.UNSIGNED_SHORT) {
            throw new IllegalArgumentException("Cannot add shorts to an FLOAT accessor");
        }
        if (shorts.length % type.size != 0) {
            throw new IllegalArgumentException("Input data doesn't match set type: " + type);
        }

        /* Values saving + min/max */
        var mod = type.size;
        for (var i = 0 ; i < shorts.length ; i++) {
            data.addShortLE((short)shorts[i]);
            if (this.min[i % mod] > shorts[i]) this.min[i % mod] = shorts[i];
            if (this.max[i % mod] < shorts[i]) this.max[i % mod] = shorts[i];
        }
        count += shorts.length / mod;
        return this;
    }

    /**
     * Gets the size of this accessor in bytes
     * @return the size of this accessor in bytes
     */
    public int size() {
        return data.size();
    }

    /**
     * Gets the raw data of this accessor
     * @return the bytes of the accessor
     */
    @JsonIgnore
    public byte[] getBytes() {
        return data.bytes();
    }

    /**
     * Sets the ID of this Accessor (eq. its index in the Accessor array).
     * @param i the index of the Accessor
     * @return this Accessor
     */
    public Accessor setIndex(int i) {
        index = i;
        return this;
    }

    /**
     * Gets the index of this accessor in the global accessors array
     * @return the index of this accessor
     */
    public int index() { return index; }

    /**
     * Index of the BufferView to read bytes from.
     * @return the index of the buffer view
     */
    @JsonGetter
    public int bufferView() {
        return bufferView.index();
    }

    /**
     * Sets the number of bytes to skip from the buffer view
     * @param offset the number of bytes to skip
     * @return this accessor
     */
    public Accessor setOffset(int offset) {
        this.byteOffset = offset;
        return this;
    }

    /**
     * Number of bytes of the BufferView to skip before starting to
     * read bytes of the accessor.
     * @return the offsets in bytes
     */
    @JsonGetter
    public Integer byteOffset() {
        return byteOffset == 0 ? null : byteOffset;
    }

    /**
     * The type of numbers read by this accessor
     * @return the value corresponding to the set component type
     */
    @JsonGetter
    public int componentType() {
        return componentType.value;
    }

    /**
     * The number of elements (of type 'Type') contained in this accessor
     * @return the number of elements contained in the accessor
     */
    @JsonGetter
    public int count() {
        return count;
    }

    /**
     * The layout of values that defines an element that can be red
     * by the accessor.
     * @return the layout of this accessor values
     */
    @JsonGetter
    public String type() {
        return type.name();
    }

    /**
     * The smallest combined element of this accessor
     * @return the smallest value(s) of this accessor
     */
    @JsonGetter
    public float[] min() {
        return min;
    }

    /**
     * The greatest combined element of this accessor
     * @return the greatest value(s) of this accessor
     */
    @JsonGetter
    public float[] max() {
        return max;
    }


    /**
     * Type of number read by the accessor
     */
    public enum ComponentType {
        /** An unsigned short on 2 bytes */
        UNSIGNED_SHORT(5123),
        /** A float on 4 bytes */
        FLOAT(5126);

        /** The GLTF code that correspond to this component type */
        public final int value;
        ComponentType(int value) { this.value = value; }
    }

    /**
     * Layout of numbers read by the accessor
     */
    public enum Type {
        /** Element that is made of a single number */
        SCALAR(1),

        /** Element that is a vector of two numbers */
        VEC2(2),

        /** Element that is a vector of three numbers */
        VEC3(3);

        /** The amount of number contained by this type */
        public final int size;
        Type(int size) { this.size = size; }
    }
}
