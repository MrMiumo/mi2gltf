package io.github.mrmiumo.mi2gltf.mcmodel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Object containing all animation related information read from a
 * <code>.mcmeta</code> file.
 * @param interpolate whether additional frames must be generated to
 *     fill all 20 game-ticks<p>
 *     Default: <code>false</code>
 * @param width the width of a single frame in pixels<p>
 *     Default: image's width if height is defined, smallest of the
 *     image's dimensions otherwise
 * @param height the height of a single frame in pixels<p>
 *     Default: image's height if width is defined, smallest of the
 *     image's dimensions otherwise
 * @param frametime time between two frames in game ticks<p>
 *     Default: <code>1</code>
 * @param frames ordered list of frames indices<p>
 *     Default: All frames from the first to last
 * @param atlasSize computed number of images contained in the atlas
 */
public record ModelAnimation(
    boolean interpolate,
    int width,
    int height,
    int frametime,
    int[] frames,
    int atlasSize
) {
    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * Parses a <code>.mcmeta</code> file and return a ModelAnimation
     * that contains the file data.
     * @param path the path of the base image file
     * @param img the content of the image
     * @return the data of the associated <code>.mcmeta</code> if any,
     *     or null
     * @throws IOException in case of error while reading the .mcmeta file
     */
    public static ModelAnimation from(Path path, BufferedImage img) throws IOException {
        path = Path.of(path + ".mcmeta");
        if (!Files.exists(path)) return null;

        var json = JSON.readTree(Files.readString(path)).get("animation");
        if (json == null) return null; // Not an animation .mcmeta

        /* Animated texture */
        var interpolate = json.optional("interpolate").map(JsonNode::asBoolean).orElse(false);
        var min = Math.min(img.getWidth(), img.getHeight());
        var width = json.optional("width").map(JsonNode::asInt).orElseGet(() -> json.has("height") ? img.getWidth() : min);
        var height = json.optional("height").map(JsonNode::asInt).orElseGet(() -> json.has("width") ? img.getHeight() : min);
        var atlasSize = img.getHeight() / height;
        var frametime = json.optional("frametime").map(JsonNode::asInt).orElse(1);
        var frames = json.optional("frames")
            .map(f -> Arrays.stream(f.toString().replace("[", "").replace("]", "").split(",")).mapToInt(Integer::valueOf))
            .orElseGet(() -> IntStream.range(0, atlasSize))
            .toArray();

        return new ModelAnimation(interpolate, width, height, frametime, frames, atlasSize);
    }
}
