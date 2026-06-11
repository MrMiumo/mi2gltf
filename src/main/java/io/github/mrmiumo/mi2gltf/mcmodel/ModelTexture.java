package io.github.mrmiumo.mi2gltf.mcmodel;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import io.github.mrmiumo.mi2gltf.textures.Image;

/**
 * Utility class that holds Minecraft model texture informations
 */
public class ModelTexture {
    /** The default '#missing' texture */
    public static final ModelTexture MISSING = new ModelTexture();
    private final Path path;

    /** The image data */
    public final BufferedImage img;

    /** The width of the image in pixels */
    public final int width;

    /** The height of the image in pixels */
    public final int height;
    
    /**
     * Creates a new texture from the given texture path.
     * @param path the path of an existing image file
     * @return the model texture (or the '#missing' texture if null)
     */
    public static ModelTexture from(Path path) {
        if (path == null) return MISSING;

        var img = read(path);
        var w = img.getWidth();
        var h = img.getHeight();

        if (Files.exists(Path.of(path + ".mcmeta"))) {
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

    private ModelTexture() {
        path = Path.of("#missing");
        width = 2;
        height = 2;

        /* Draws the default 'Not renderable' texture */
        img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 1, 1);
        g.fillRect(1, 1, 1, 1);
        g.setColor(Color.MAGENTA);
        g.fillRect(1, 0, 1, 1);
        g.fillRect(0, 1, 1, 1);
        g.dispose();
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

    /**
     * Converts this model texture into a GLTF Image
     * @param index the index of the image to create
     * @return the image
     */
    public Image toImage(int index) {
        if (this == MISSING) return new Image(img, index);
        return new Image(path, index);
    }

    /**
     * The path of the texture (may start by # for default textures)
     * @return the path of the texture file
     */
    public Path path() { return path; }

    /**
     * The height of the texture image in pixels
     * @return the height of the texture
     */
    public int height() { return height; }

    /**
     * The width of the texture image in pixels
     * @return the width of the texture
     */
    public int width() { return width; }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || ModelTexture.class != obj.getClass()) return false;
        
        return path.equals(((ModelTexture) obj).path);
    }

    /**
     * Gets the pixels of this image as an array. This is sightly
     * faster than calling {@link BufferedImage#getRGB(int, int)} each
     * time.
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
