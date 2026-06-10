package io.github.mrmiumo.mi2gltf.textures;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import io.github.mrmiumo.mi2gltf.mcmodel.ModelTexture;
import io.github.mrmiumo.mi2gltf.mcmodel.Cube.Face;

public class Atlas {

    private final HashMap<ModelTexture, Position> images = new HashMap<>();
    private final int size;

    /**
     * Builds a new Atlas with the given images.
     * Assembles all the given images into one single big image.
     * @param images the images to make the atlas from
     */
    public Atlas(Collection<ModelTexture> images) {

        /* Sort images by height descending */
        images = new ArrayList<>(images);
        if (!images.contains(ModelTexture.MISSING)) images.add(ModelTexture.MISSING);
        if (!images.contains(ModelTexture.TRANSPARENT)) images.add(ModelTexture.TRANSPARENT);
        images = images.stream().sorted(Comparator.comparingInt(ModelTexture::height).reversed()).toList();
        
        /* Initialize packing variables */
        var widestImg = images.stream().mapToInt(ModelTexture::width).max().orElse(1);
        var maxWidth = (int)Math.round(Math.sqrt(images.stream().mapToDouble(r -> r.width * r.height).sum()));
        maxWidth = Math.max(widestImg, maxWidth);
        var currentX = 0;
        var currentY = 0;
        var rowHeight = 0;
        var w = 0;
        var h = 0;

        /* Place each image */
        for (var image : images) {
            if (currentX + image.width + 4 > maxWidth) { // Move to the next row
                w = Math.max(w, currentX);
                currentX = 0;
                currentY += rowHeight + 1;
                h += rowHeight + 1;
                rowHeight = 0;
            }

            // Store position
            this.images.put(image, new Position(currentX + 2, currentY + 2));

            // Update row state
            currentX += image.width + 4;
            if (image.height + 4 > rowHeight) rowHeight = image.height + 4;
        }
        h = h + rowHeight;
        w = Math.max(w, currentX);

        size = 1 << (32 - Integer.numberOfLeadingZeros(Math.max(w, h) - 1));
    }

    /**
     * Adapts the base UV of the given face to work within the Atlas.
     * @param face the face to get UV from
     * @return the face UV in the Atlas
     */
    public Uv get(Face face) {
        var img = face.texture();
        var pos = images.get(img);
        if (pos == null) {
            img = ModelTexture.MISSING;
            pos = images.get(img);
        }

        return new Uv(
            (pos.x + face.fromX() * img.width) / size,
            (pos.y + face.fromY() * img.height) / size,
            (pos.x + face.toX() * img.width) / size,
            (pos.y + face.toY() * img.height) / size
        );
    }

    /**
     * Builds the Atlas into one larger image that contains all others.
     * @return the atlas image
     */
    public BufferedImage image() {
        var img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();

        for (var entry : images.entrySet()) {
            var image = entry.getKey().img;
            var pos = entry.getValue();
            var w = image.getWidth();
            var h = image.getHeight();

            /* 2px border */
            g.drawImage(image.getSubimage(0, 0, 1, h), pos.x-1, pos.y, null); // Left
            g.drawImage(image.getSubimage(w-1, 0, 1, h), pos.x+w, pos.y, null); // Right
            g.drawImage(image.getSubimage(0, 0, w, 1), pos.x, pos.y-1, null); // Top
            g.drawImage(image.getSubimage(0, h-1, w, 1), pos.x, pos.y+h, null); // Bottom


            g.drawImage(image.getSubimage(0,   0,   1, 1), pos.x-1, pos.y-1, null); // TopLeft
            g.drawImage(image.getSubimage(w-1, 0,   1, 1), pos.x+w, pos.y-1, null); // TopRight
            g.drawImage(image.getSubimage(0,   h-1, 1, 1), pos.x-1, pos.y+h, null); // BottomLeft
            g.drawImage(image.getSubimage(w-1, h-1, 1, 1), pos.x+w, pos.y+h, null); // BottomRight

            /* Final image draw */
            g.drawImage(image, pos.x, pos.y, null);
        }
        g.dispose();
        return img;
    }

    public record Uv(float fromX, float fromY, float toX, float toY) {}
    public record Position(int x, int y) {}
}
