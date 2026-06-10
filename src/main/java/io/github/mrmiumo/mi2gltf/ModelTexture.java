package io.github.mrmiumo.mi2gltf;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class ModelTexture {
    public static final ModelTexture MISSING = new ModelTexture("#missing");
    public static final ModelTexture TRANSPARENT = new ModelTexture("#transparent");
    private final Path path;
    public final BufferedImage img;
    public final int width;
    public final int height;
    
    public static ModelTexture from(Path path) {
        if (path == null) return MISSING;

        var img = read(path);
        var w = img.getWidth();
        var h = img.getHeight();

        if (Files.exists(Path.of(path.toString() + ".mcmeta"))) {
            /* Animated texture */
            h = w;
            var crop = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            var g = crop.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            img = crop;
        }

        return new ModelTexture(path, img, w, h);
    }

    private ModelTexture(String code) {
        path = Path.of(code);
        if ("#missing".equals(code)) {
            width = 16;
            height = 16;

            /* Draws the default 'Not renderable' texture */
            img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            var g = img.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 8, 8);
            g.fillRect(8, 8, 8, 8);
            g.setColor(Color.MAGENTA);
            g.fillRect(8, 0, 8, 8);
            g.fillRect(0, 8, 8, 8);
            g.dispose();
        } else {
            width = 1;
            height = 1;
            img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private ModelTexture(Path path, BufferedImage img, int width, int height) {
        this.path = path;
        this.img = img;
        this.width = width;
        this.height = height;
    }

    /**
     * Loads an image, throwing UncheckedIO instead of classic IO.
     * @param file the file to load
     * @return the image
     */
    private static BufferedImage read(Path file) {
        try {
            return ImageIO.read(Files.newInputStream(file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path path() { return path; }

    /**
     * Gets the pixels of the image as an array. This is sightly
     * faster than calling {@link BufferedImage#getRGB(int, int)} each
     * time.
     * @param image the image to get pixels from
     * @return all the pixels of the image
     */
    public int[] pixels() {
        var argb = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var g = argb.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return ((DataBufferInt)argb.getRaster().getDataBuffer()).getData();
    }
}
