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

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonGetter;

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

    public Image(Path path, int index) {
        this.path = path;
        this.index = index;
        img = null;
    }

    public Image(BufferedImage img, int index) {
        this.path = null;
        this.index = index;
        this.img = img;
    }

    public int index() { return index; }

    @JsonGetter
    public String uri() {
        try {
            if (path == null) return uriFromImage();
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
