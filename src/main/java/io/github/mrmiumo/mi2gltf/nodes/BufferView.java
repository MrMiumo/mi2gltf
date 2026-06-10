package io.github.mrmiumo.mi2gltf.nodes;
import java.util.Collection;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.mrmiumo.mi2gltf.nodes.Accessor.ComponentType;
import io.github.mrmiumo.mi2gltf.nodes.Accessor.Type;

/**
 * First chunking of a buffer to separate data types: indices, normals,
 * positions and textures.
 */
@JsonInclude(Include.NON_EMPTY)
public class BufferView {

    private int index = -1;

    private final Buffer buffer;

    private int byteOffset = 0;

    private final Target target;
    
    private final HashSet<Accessor> accessors = new HashSet<>();

    /**
     * Creates a new buffer view on the given buffer.
     * @param buffer the buffer to build the view onto
     * @param target the type of data read by the view
     */
    BufferView(Buffer buffer, Target target) {
        this.buffer = buffer;
        this.target = target;
    }

    /**
     * Creates a new accessor linked to this bufferView.
     * @param type the type of numbers to store in the accessor
     * @param componentType the layout of those numbers
     * @return the Accessor
     */
    public Accessor newAccessor(Type type, ComponentType componentType) {
        var accessor = new Accessor(this, type, componentType);
        accessors.add(accessor);
        return accessor;
    }


    /**
     * Gets the raw data of this view (eq. collect and merge the data
     * of this view accessors).
     * @return the bytes of the view
     */
    @JsonIgnore
    byte[] getBytes() {
        var data = new byte[byteLength()];
        var len = 0;

        for (var accessor : accessors) {
            /* Initializes view offset */
            accessor.setOffset(len);

            /* Copy data */
            var bytes = accessor.getBytes();
            System.arraycopy(bytes, 0, data, len, bytes.length);
            len += bytes.length;
        }

        return data;
    }

    /**
     * Gets the accessors contained in this buffer
     * @return this buffer's accessors
     */
    @JsonIgnore
    public Collection<Accessor> getAccessors() {
        return accessors;
    }

    /**
     * Sets the ID of this BufferView (eq. its index in the BufferView array).
     * @param i the index of the BufferView
     * @return this BufferView
     */
    public BufferView setIndex(int i) {
        index = i;
        return this;
    }

    /**
     * Gets the index of this view in the bufferViews array
     * @return the index of this view
     */
    public int index() { return index; }

    /**
     * Gets the index of the buffer wrapped in this view
     * @return index of the buffer
     */
    @JsonGetter
    public int buffer() {
        return buffer.index();
    }

    /**
     * Sets the number of bytes to skip from the target buffer
     * @param offset the number of bytes to skip
     * @return this view
     */
    public BufferView setOffset(int offset) {
        this.byteOffset = offset;
        return this;
    }

    /**
     * Gets the number of bytes to skip from the buffer before reading
     * bytes for this view
     * @return the offset in bytes of this view
     */
    @JsonGetter
    public Integer byteOffset() {
        return byteOffset == 0 ? null : byteOffset;
    }

    /**
     * Gets the number of bytes readable by the view
     * @return the size of this view in bytes
     */
    @JsonGetter
    public int byteLength() {
        return accessors.stream().mapToInt(Accessor::size).sum();
    }

    /**
     * The type of data that is read by this view
     * @return the target data type of the view
     */
    @JsonGetter
    public int target() {
        return target.value;
    }

    /**
     * Type of data that can be contained by a view
     */
    public enum Target {
        /** Vertex attributes (positions and/or normals) */
        ARRAY_BUFFER(34962),

        /** Vertex indices */
        ELEMENT_ARRAY_BUFFER(34963);

        /** The GLTF code that corresponds to this target type */
        public final int value;

        Target(int value) { this.value = value; }
    }
}
