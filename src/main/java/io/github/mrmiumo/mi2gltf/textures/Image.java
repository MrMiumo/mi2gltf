package io.github.mrmiumo.mi2gltf.textures;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * A link or the data of an image, that can then be used in a Texture
 * using a Sampler.
 */
public class Image {

    private static final Map<String, String> MIMES;
    static {
        MIMES = new HashMap<>();
        MIMES.put("png", "image/png");
        MIMES.put("bmp", "image/bmp");
        MIMES.put("gif", "image/gif");
        MIMES.put("jpeg", "image/jpeg");
        MIMES.put("jpg", "image/jpeg");
        MIMES.put("webp", "image/webp");
        MIMES.put("", "application/octet-stream");
    }

    private final int index;

    private final Path path;

    private final BufferedImage img;

    /**
     * Creates a new image from a file path
     * @param path the path of the image
     * @param index the index of this image in the images array
     */
    public Image(Path path, int index) {
        this.path = Objects.requireNonNull(path);
        this.index = index;
        img = null;
    }

    /**
     * Creates a new image from a BufferedImage
     * @param img the data of the image
     * @param path to get the image name (nullable)
     * @param index the index of this image in the images array
     */
    public Image(BufferedImage img, Path path, int index) {
        this.path = path;
        this.index = index;
        this.img = Objects.requireNonNull(img);
    }

    /**
     * Gets the index of this image in the images array
     * @return the index of this image
     */
    public int index() { return index; }

    /**
     * Gets the path of this image
     * @return the path of the image
     */
    public Path path() { return path; }

    /**
     * Converts the data of this image into a base64 URI
     * @return the base64 URI of the image data
     */
    @JsonGetter
    public String uri() {
        try {
            if (img != null) return uriFromImage();
            return uriFromPath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Builds a base64 uri that contains the file data using the set
     * BufferedImage.
     * @return the data of the image as base64 URI
     * @throws IOException in case of error while writing the image
     */
    private String uriFromImage() throws IOException {
        var stream = new ByteArrayOutputStream();
        ImageIO.write(img, "png", stream);
        return "data:" + MIMES.get("png") + ";base64,"
            + Base64.getEncoder().encodeToString(stream.toByteArray());
    }
    
    /**
     * Builds a base64 uri that contains the file data using the set
     * path.
     * @return the data of the path as base64 URI
     * @throws IOException in case of error while reading the file
     */
    private String uriFromPath() throws IOException {
        var filename = path.getFileName().toString();
        var pos = filename.lastIndexOf(".") + 1;
        var ext = filename.substring(pos).toLowerCase();
        return "data:" + MIMES.get(ext) + ";base64,"
            + Base64.getEncoder().encodeToString(Files.readAllBytes(path));
    }
}
