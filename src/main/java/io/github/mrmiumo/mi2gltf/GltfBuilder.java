package io.github.mrmiumo.mi2gltf;

import static io.github.mrmiumo.mi2gltf.nodes.Accessor.ComponentType.*;
import static io.github.mrmiumo.mi2gltf.nodes.Accessor.Type.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.mrmiumo.mi2gltf.mcmodel.Cube;
import io.github.mrmiumo.mi2gltf.mcmodel.FaceName;
import io.github.mrmiumo.mi2gltf.mcmodel.ModelParser;
import io.github.mrmiumo.mi2gltf.mcmodel.Cube.Face;
import io.github.mrmiumo.mi2gltf.nodes.Accessor;
import io.github.mrmiumo.mi2gltf.nodes.BufferView.Target;
import io.github.mrmiumo.mi2gltf.nodes.Mesh;
import io.github.mrmiumo.mi2gltf.nodes.Node;
import io.github.mrmiumo.mi2gltf.textures.Atlas;
import io.github.mrmiumo.mi2gltf.textures.Material;

/**
 * Main class used to convert from Minecraft models to GLTF files.
 */
public class GltfBuilder {
    /** Oh no, a logger... Just kidding, its fine! */
    private static final Logger LOGGER = LoggerFactory.getLogger(GltfBuilder.class);
    
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final EnumMap<FaceName, Positions> POSITIONS = initPositions();
    private static final EnumMap<FaceName, UVs> UVS = initUVs();

    private final Gltf gltf = new Gltf();
    private final Accessor indicesAcc = indicesAccessor();
    private final EnumMap<FaceName, Accessor> normals = initNormals();
    private final Atlas atlas;
    private final Material atlasMaterial;

    private boolean pretty = false;

    /**
     * Loads a minecraft model from a pack and convert it into a
     * GLTF model.
     * @param model path to the model file (json)
     * @return the builder to output the GLTF raw or formatted
     * @throws IOException in case of error while loading the file
     */
    public static GltfBuilder from(Path model) throws IOException {
        var cubes = new ModelParser().parse(model);
        return new GltfBuilder(cubes);
    }

    private GltfBuilder(Collection<Cube> cubes) {
        var textures = cubes.stream()
            .flatMap(c -> c.faces().stream())
            .map(Face::texture)
            .distinct()
            .toList();
        atlas = new Atlas(textures);
        atlasMaterial = gltf.setAtlas(atlas);

        cubes.forEach(this::add);
    }

    private GltfBuilder add(Cube cube) {
        final var nodes = gltf.nodes();
        var from = cube.from();
        var to = cube.to();
        var node = new Node();

        /* Add rotation */
        if (cube.axis() != 0) {
            from = from.sub(cube.pivot());
            to = to.sub(cube.pivot());
            node.translate(cube.pivot())
                .rotate(cube.quaternion());
        }
        
        /* Build mesh */
        var mesh = new Mesh();
        for (var face : cube.faces()) {
            addFace(mesh, from, to, face);
        }

        node.setMesh(mesh);
        nodes.add(node);
        return this;
    }

    /**
     * Adds a face to the given mesh.
     * @param mesh the mesh to add the face to
     * @param from the first corner of the cube
     * @param to the opposite corner of the cube
     * @param face the details of the face
     */
    @SuppressWarnings("java:S1659")
    private void addFace(Mesh mesh, Vec from, Vec to, Face face) {
        var buffer = gltf.getBuffer("Mesh");

        /* Positions */
        var view = buffer.newView(Target.ARRAY_BUFFER);
        var positions = view.newAccessor(VEC3, FLOAT);
        POSITIONS.get(face.name()).add(positions,
            from.x(), from.y(), from.z(),
            to.x(), to.y(), to.z()
        );

        /* Texture */
        view = buffer.newView(Target.ARRAY_BUFFER);
        var texture = view.newAccessor(VEC2, FLOAT);
        UVS.get(face.name()).add(atlas, texture, face);

        /* Add to mesh */
        mesh.addPrimitive(indicesAcc, positions, normals.get(face.name()), texture, atlasMaterial);
    }

    private static void uv(Atlas atlas, Accessor acc, Face face, int w, int h) {
        if (face == null) {
            acc.add(0, 0);
            return;
        }
        var rotate = face.rotation() % 4;
        var uv = atlas.get(face);
        
        float u;
        float v;
        if (rotate == 0) {
            u = uv.fromX() * w + uv.toX() * (1-w);
            v = uv.fromY() * h + uv.toY() * (1-h);
        } else if (rotate == 1) {
            u = uv.fromX() * h + uv.toX() * (1-h);
            v = uv.fromY() * (1-w) + uv.toY() * w;
        } else if (rotate == 2) {
            u = uv.fromX() * (1-w) + uv.toX() * w;
            v = uv.fromY() * (1-h) + uv.toY() * h;
        } else {
            u = uv.fromX() * (1-h) + uv.toX() * h;
            v = uv.fromY() * w + uv.toY() * (1-w);
        }
        acc.add(u, v);
    }

    /**
     * Build one cube indices accessor
     * @return the indices accessor
     */
    private Accessor indicesAccessor() {
        var buffer = gltf.getBuffer("Mesh");
        var view = buffer.newView(Target.ELEMENT_ARRAY_BUFFER);
        var acc = view.newAccessor(SCALAR, UNSIGNED_SHORT);

        acc.add(1, 0, 2,  2, 0, 3);

        return acc;
    }

    /**
     * Build one cube normals accessor
     * @return the normals accessor
     */
    private EnumMap<FaceName, Accessor> initNormals() {
        var map = new EnumMap<FaceName, Accessor>(FaceName.class);
        var buffer = gltf.getBuffer("Mesh");

        var view = buffer.newView(Target.ARRAY_BUFFER);
        map.put(FaceName.FRONT, view.newAccessor(VEC3, FLOAT)
            .add(0, 0, -1,  0, 0, -1,  0, 0, -1,  0, 0, -1f));
        view = buffer.newView(Target.ARRAY_BUFFER);
        map.put(FaceName.LEFT, view.newAccessor(VEC3, FLOAT)
            .add(-1, 0, 0,  -1, 0, 0,  -1, 0, 0,  -1, 0, 0f));
        view = buffer.newView(Target.ARRAY_BUFFER);
        map.put(FaceName.BOTTOM, view.newAccessor(VEC3, FLOAT)
            .add(0, -1, 0,  0, -1, 0,  0, -1, 0,  0, -1, 0f));
        view = buffer.newView(Target.ARRAY_BUFFER);
        map.put(FaceName.BACK, view.newAccessor(VEC3, FLOAT)
            .add(0, 0,  1,  0, 0,  1,  0, 0,  1,  0, 0,  1f));
        view = buffer.newView(Target.ARRAY_BUFFER);
        map.put(FaceName.RIGHT, view.newAccessor(VEC3, FLOAT)
            .add( 1, 0, 0,   1, 0, 0,   1, 0, 0,   1, 0, 0f));
        view = buffer.newView(Target.ARRAY_BUFFER);
        map.put(FaceName.TOP, view.newAccessor(VEC3, FLOAT)
            .add(0,  1, 0,  0,  1, 0,  0,  1, 0,  0,  1, 0f));

        return map;
    }

    /**
     * Build a map with position generators for each face
     * @return the positions generators
     */
    private static EnumMap<FaceName, Positions> initPositions() {
        var map = new EnumMap<FaceName, Positions>(FaceName.class);

        map.put(FaceName.FRONT, (acc, fx, fy, fz, tx, ty, tz) -> acc.add(
            fx, fy, fz, // (0) Front Bottom Left
            tx, fy, fz, // (3) Front Bottom Right
            tx, ty, fz, // (6) Front Top Right
            fx, ty, fz  // (9) Front Top Left
        ));
        map.put(FaceName.LEFT, (acc, fx, fy, fz, tx, ty, tz) -> acc.add(
            fx, fy, tz, // (12) Back Bottom Left
            fx, fy, fz, // (1) Front Bottom Left
            fx, ty, fz, // (10) Front Top Left
            fx, ty, tz  // (21) Back Top Left
        ));
        map.put(FaceName.BOTTOM, (acc, fx, fy, fz, tx, ty, tz) -> acc.add(
            fx, fy, tz, // (13) Back Bottom Left
            tx, fy, tz, // (15) Back Bottom Right
            tx, fy, fz, // (4) Front Bottom Right
            fx, fy, fz  // (2) Front Bottom Left
        ));
        map.put(FaceName.BACK, (acc, fx, fy, fz, tx, ty, tz) -> acc.add(
            tx, fy, tz, // (16) Back Bottom Right
            fx, fy, tz, // (14) Back Bottom Left
            fx, ty, tz, // (22) Back Top Left
            tx, ty, tz  // (18) Back Top Right
        ));
        map.put(FaceName.RIGHT, (acc, fx, fy, fz, tx, ty, tz) -> acc.add(
            tx, fy, fz, // (5) Front Bottom Right
            tx, fy, tz, // (17) Back Bottom Right
            tx, ty, tz, // (19) Back Top Right
            tx, ty, fz  // (7) Front Top Right
        ));
        map.put(FaceName.TOP, (acc, fx, fy, fz, tx, ty, tz) -> acc.add(
            tx, ty, tz, // (20) Back Top Right
            fx, ty, tz, // (23) Back Top Left
            fx, ty, fz, // (11) Front Top Left
            tx, ty, fz  // (8) Front Top Right
        ));

        return map;
    }

    /**
     * Build a map with UVs generators for each face
     * @return the UVs generators
     */
    private static EnumMap<FaceName, UVs> initUVs() {
        var map = new EnumMap<FaceName, UVs>(FaceName.class);

        map.put(FaceName.FRONT, (atlas, acc, face) -> {
            uv(atlas, acc, face, 0, 0);  // (0) FRONT - Bottom Left
            uv(atlas, acc, face, 1, 0);  // (3) FRONT - Bottom Right
            uv(atlas, acc, face, 1, 1);  // (6) FRONT - Top Right
            uv(atlas, acc, face, 0, 1);  // (9) FRONT - Top Left
        });
        map.put(FaceName.LEFT, (atlas, acc, face) -> {
            uv(atlas, acc, face, 0, 0);   // (12) LEFT - Back Bottom
            uv(atlas, acc, face, 1, 0);   // (1) LEFT - Front Bottom
            uv(atlas, acc, face, 1, 1);   // (10) LEFT - Front Top
            uv(atlas, acc, face, 0, 1);   // (21) LEFT - Back Top
        });
        map.put(FaceName.BOTTOM, (atlas, acc, face) -> {
            uv(atlas, acc, face, 1, 1); // (13) BOTTOM - Back Left
            uv(atlas, acc, face, 0, 1); // (15) BOTTOM - Back Right
            uv(atlas, acc, face, 0, 0); // (4) BOTTOM - Front Right
            uv(atlas, acc, face, 1, 0); // (2) BOTTOM - Front Left
        });
        map.put(FaceName.BACK, (atlas, acc, face) -> {
            uv(atlas, acc, face, 0, 0);   // (16) BACK - Bottom Right
            uv(atlas, acc, face, 1, 0);   // (14) BACK - Bottom Left
            uv(atlas, acc, face, 1, 1);   // (22) BACK - Top Left
            uv(atlas, acc, face, 0, 1);   // (18) BACK - Top Right
        });
        map.put(FaceName.RIGHT, (atlas, acc, face) -> {
            uv(atlas, acc, face, 0, 0);  // (5) RIGHT - Front Bottom
            uv(atlas, acc, face, 1, 0);  // (17) RIGHT - Back Bottom
            uv(atlas, acc, face, 1, 1);  // (19) RIGHT - Back Top
            uv(atlas, acc, face, 0, 1);  // (7) RIGHT - Front Top
        });
        map.put(FaceName.TOP, (atlas, acc, face) -> {
            uv(atlas, acc, face, 0, 0);    // (20) TOP - Back Right
            uv(atlas, acc, face, 1, 0);    // (23) TOP - Back Left
            uv(atlas, acc, face, 1, 1);    // (11) TOP - Front Left
            uv(atlas, acc, face, 0, 1);    // (8) TOP - Front Right
        });

        return map;
    }

    /**
     * Enables pretty JSON formatting.
     * @return this builder
     */
    public GltfBuilder prettify() {
        this.pretty = true;
        return this;
    }

    /**
     * Saves this GLTF file into a file
     * @param p the path of the file to save the model under
     * @return the path
     * @throws IOException in case of error while writing the file
     */
    public Path save(Path p) throws IOException {
        return Files.writeString(p, this.toString());
    }

    @Override
    public String toString() {
        gltf.build();
        try {
            return pretty
                ? JSON.writer(getPrettifier()).writeValueAsString(gltf)
                : JSON.writeValueAsString(gltf);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to generate GLTF file", e);
            return "{}";
        }
    }

    private static PrettyPrinter getPrettifier() {
        var indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        var printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);

        return printer;
    }

    /**
     * Sets the path of the default resource pack folder. If the
     * 'default.minecraft.pack' property is already defined, no need
     * to use this function!
     * @param location the path of the default minecraft resource pack
     */
    public static void setDefaultPack(Path location) {
        ModelParser.setDefaultPack(location);
    }


    @FunctionalInterface
    private interface Positions {
        public void add(Accessor acc, float fx, float fy, float fz, float tx, float ty, float tz);
    }

    @FunctionalInterface
    private interface UVs {
        public void add(Atlas atlas, Accessor acc, Face face);
    }
}
