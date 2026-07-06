package io.github.mrmiumo.mi2gltf.mcmodel;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
 * @param img the texture image, that may be different from the base
 *     file if the animation have interpolation
 * @param atlasSize computed number of images contained in the atlas
 */
public record ModelAnimation(
    boolean interpolate,
    int width,
    int height,
    int frametime,
    int[] frames,
    BufferedImage img,
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
        var interpolate = Boolean.TRUE.equals(json.optional("interpolate").map(JsonNode::asBoolean).orElse(false));
        var min = Math.min(img.getWidth(), img.getHeight());
        var width = json.optional("width").map(JsonNode::asInt).orElseGet(() -> json.has("height") ? img.getWidth() : min);
        var height = json.optional("height").map(JsonNode::asInt).orElseGet(() -> json.has("width") ? img.getHeight() : min);
        var atlasSize = img.getHeight() / height;
        var frametime = json.optional("frametime").map(JsonNode::asInt).orElse(1);
        var frames = json.optional("frames")
            .map(f -> Arrays.stream(f.toString().replace("[", "").replace("]", "").split(",")).mapToInt(Integer::valueOf))
            .orElseGet(() -> IntStream.range(0, atlasSize))
            .toArray();

        if (interpolate && frametime > 1) {
            return interpolated(img, frametime, frames, width, height);
        }

        return new ModelAnimation(interpolate, width, height, frametime, frames, img, atlasSize);
    }

    /**
     * Generates missing images and frames to add interpolation.
     * @param img the base image (original atlas)
     * @param frametime the base frametime > 1
     * @param frames the original frames sequence
     * @param width the with of one frame
     * @param height the height of one frame
     * @return the model animation with interpolation precomputed
     */
    private static ModelAnimation interpolated(BufferedImage img, int frametime, int[] frames, int width, int height) {
        var multiplier = 1f * frametime; // Amount of frames to interpolates between each
        var interpolated = new ArrayList<Interpolated>();

        /* Generate new frame array with interpolated frames */
        for (var f = 0 ; f < frames.length ; f++) {
            var frame = frames[f];
            var next = frames[(f + 1) % frames.length];
            interpolated.add(new Interpolated(frame, frame, 1));

            for (var i = 1 ; i < multiplier ; i++) {
                interpolated.add(new Interpolated(frame, next, (multiplier - i) / multiplier));
            }
        }

        /** Generate the atlas image */
        var atlasMap = interpolated.stream().distinct().toList();
        var atlas = new BufferedImage(width, height * atlasMap.size(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        for (var i = 0 ;  i < atlasMap.size() ; i++) {
            var y = i * height;
            var map = atlasMap.get(i);
            var from = img.getSubimage(0, map.from * height, width, height);
            var to = img.getSubimage(0, map.to * height, width, height);

            if (map.opacity == 1) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
                g.drawImage(from, 0, y, null);
            } else {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, map.opacity));
                g.drawImage(from, 0, y, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - map.opacity));
                g.drawImage(to, 0, y, null);
            }
        }
        g.dispose();

        /* Build the ModelAnimation */
        var newFrames = interpolated.stream().mapToInt(atlasMap::indexOf).toArray();
        return new ModelAnimation(true, width, height, 1, newFrames, atlas, atlasMap.size());
    }

    private record Interpolated(int from, int to, float opacity) {
        @Override
        public String toString() {
            return from + "->" + to + " x" + opacity;
        }
    }
}
