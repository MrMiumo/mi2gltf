package io.github.mrmiumo.mi2gltf.mcmodel;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mrmiumo.mi2gltf.textures.Image;

/**
 * Utility class that holds Minecraft model texture informations
 */
public class ModelTexture {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelTexture.class);

    /** The default '#missing' texture */
    public static final ModelTexture MISSING = new ModelTexture();
    private final Path path;

    /** The animations if any */
    public ModelAnimation animation;

    /** The image data */
    private BufferedImage img;

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

        try {
            var img = ImageIO.read(Files.newInputStream(path));
            return new ModelTexture(path, img);
        } catch (IOException e) {
            LOGGER.warn("Cannot load texture '{}':", path, e);
            return MISSING;
        }
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
        animation = null;
    }

    private ModelTexture(Path path, BufferedImage img) throws IOException {
        this.path = path;
        this.img = img;
        this.animation = ModelAnimation.from(path, img);
        this.width = animation == null ? img.getWidth() : animation.width();
        this.height = animation == null ? img.getHeight() : animation.height();
    }

    /**
     * Updates this texture depending on the animatable GLTF state.
     * If this texture is animated and set as not animated, only the
     * first frame will be kept.
     * @param animated whether GLTF animation are supported or not
     * @return true if this texture is animated, false otherwise
     */
    public boolean setAnimated(boolean animated) {
        if (animation == null) return false;

        if (!animated) {
            /* Keep first frame only */
            var crop = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var g = crop.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            img = crop;
            animation = null;
            return false;
        }
        return true;
    }

    /**
     * Converts this model texture into a GLTF Image
     * @param index the index of the image to create
     * @return the image
     */
    public Image toImage(int index) {
        var image = animation == null ? img : animation.img();
        if (this == MISSING || image != null) return new Image(image, path, index);
        return new Image(path, index);
    }

    /**
     * The path of the texture (may start by # for default textures)
     * @return the path of the texture file
     */
    public Path path() { return path; }

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
        var image = animation == null ? img : animation.img();
        var argb = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var g = argb.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return ((DataBufferInt)argb.getRaster().getDataBuffer()).getData();
    }
}
