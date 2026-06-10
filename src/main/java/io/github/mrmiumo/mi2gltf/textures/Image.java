package io.github.mrmiumo.mi2gltf.textures;

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
import io.github.mrmiumo.mi2gltf.ModelTexture;

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

    public Image(Path path, int index) {
        this.path = path;
        this.index = index;
    }

    public int index() { return index; }

    @JsonGetter
    public String uri() {
        var filename = path.getFileName().toString();
        var pos = filename.lastIndexOf(".") + 1;
        var ext = filename.substring(pos).toLowerCase();
        try {
            if (path.toString().startsWith("#")) {
                var texture = "#missing".equals(path.toString()) ? ModelTexture.MISSING : ModelTexture.TRANSPARENT;
                var stream = new ByteArrayOutputStream();
                ImageIO.write(texture.img, "png", stream);
                return "data:" + MIMES.get("png") + ";base64,"
                    + Base64.getEncoder().encodeToString(stream.toByteArray());
            } else {
                return "data:" + MIMES.get(ext) + ";base64,"
                    + Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
