package io.github.mrmiumo.mi2gltf.nodes;

import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.mrmiumo.mi2gltf.nodes.BufferView.Target;

/**
 * Binary buffer that contains all mesh positions, normals, textures
 * mapping and indices.
 */
public class Buffer {

    private int index = -1;
    
    private byte[] data;
    
    private final HashSet<BufferView> views = new HashSet<>();

    /**
     * Creates a new view linked to this buffer. This is necessary for
     * the view to be configured correctly with length, padding, ...
     * @param target the type of element to store in the view
     * @return the BufferView
     */
    public BufferView newView(Target target) {
        var view = new BufferView(this, target);
        views.add(view);
        return view;
    }

    /**
     * Gets the views contained in this buffer
     * @return this buffer's views
     */
    @JsonIgnore
    public Collection<BufferView> getViews() {
        return views;
    }

    /**
     * Collect and merge data of the buffer views and inject byte
     * offsets.
     * @return this buffer
     */
    public Buffer build() {
        data = new byte[byteLength()];
        var len = 0;

        for (var view : views) {
            /* Initializes view offset */
            view.setOffset(len);

            /* Copy data */
            var bytes = view.getBytes();
            System.arraycopy(bytes, 0, data, len, bytes.length);
            len += bytes.length;
        }

        return this;
    }

    /**
     * Sets the ID of this Buffer (eq. its index in the buffers array).
     * @param i the index of the buffer
     * @return this Buffer
     */
    public Buffer setIndex(int i) {
        index = i;
        return this;
    }

    /**
     * Gets the index of this buffer in the buffers array
     * @return the index of this buffer
     */
    public int index() { return index; }

    /**
     * Converts this buffer into a base64 URI containing all its data
     * @return the buffer as a base64 URI
     */
    @JsonGetter
    public String uri() {
        return "data:application/octet-stream;base64,"
            + Base64.getEncoder().encodeToString(data);
    }

    /**
     * The amount of bytes stored in the buffer
     * @return the size of the buffer in bytes
     */
    @JsonGetter
    public int byteLength() {
        return views.stream().mapToInt(BufferView::byteLength).sum();
    }
}
