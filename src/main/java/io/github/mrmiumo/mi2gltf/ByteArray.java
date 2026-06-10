package io.github.mrmiumo.mi2gltf;

import java.util.Arrays;

/**
 * Dynamically size byte array that behaves as a list
 */
public class ByteArray {
    private byte[] data = new byte[4];

    private int len = 0;

    /**
     * Adds a new float to the array, encoded as a 4 bytes long number
     * in Little Endian
     * @param value the number to encode
     * @return this array
     */
    public ByteArray addFloatLE(float value) {
        int bits = Float.floatToRawIntBits(value);
        return add(
            (byte) (bits & 0xFF),
            (byte) ((bits >> 8) & 0xFF),
            (byte) ((bits >> 16) & 0xFF),
            (byte) ((bits >> 24) & 0xFF)
        );
    }

    /**
     * Adds a new short to the array, encoded as a 2 bytes long number
     * in Little Endian
     * @param value the number to encode
     * @return this array
     */
    public ByteArray addShortLE(short value) {
        return add(
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF)
        );
    }

    /**
     * Adds new bytes to this array
     * @param bytes the bytes to add
     * @return this array
     */
    public ByteArray add(byte... bytes) {
        if (len + bytes.length > data.length) {
            data = Arrays.copyOf(data, data.length * 2 );
        }

        System.arraycopy(bytes, 0, data, len, bytes.length);
        len += bytes.length;
        return this;
    }

    /**
     * Gets the content of this ByteArray
     * @return the bytes of this array
     */
    public byte[] bytes() {
        return Arrays.copyOf(data, len);
    }

    /**
     * Gets the current size of the array
     * @return the number of bytes currently stored
     */
    public int size() {
        return len;
    }
}
