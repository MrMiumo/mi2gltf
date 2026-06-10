package io.github.mrmiumo.mi2gltf;

import static io.github.mrmiumo.mi2gltf.FaceName.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrmiumo.mi2gltf.Cube.Face;
import io.github.mrmiumo.mi2gltf.nodes.Accessor;
import io.github.mrmiumo.mi2gltf.nodes.Accessor.ComponentType;
import io.github.mrmiumo.mi2gltf.nodes.Accessor.Type;
import io.github.mrmiumo.mi2gltf.nodes.BufferView.Target;
import io.github.mrmiumo.mi2gltf.nodes.Mesh;
import io.github.mrmiumo.mi2gltf.nodes.Node;
import io.github.mrmiumo.mi2gltf.textures.Material;

public class GltfBuilder {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final Gltf gltf = new Gltf();
    private final Accessor indicesAcc = indicesAccessor();
    private final Accessor normalsAcc = normalsAccessor();

    private boolean pretty = false;

    public GltfBuilder add(Cube cube) {
        final var nodes = gltf.nodes();

        if (cube.axis() == 0) {
            /* No rotation, one node is enough */
            var node = new Node()
                .setReferenced(true)
                .setMesh(newCubeMesh(cube.from(), cube.to(), cube.faces()));
            
            nodes.add(node);
        } else {
            var node = new Node()
                .setReferenced(true)
                .setMesh(newCubeMesh(
                    cube.from().sub(cube.pivot()),
                    cube.to().sub(cube.pivot()),
                    cube.faces()
                ))
                .translate(cube.pivot())
                .rotate(cube.quaternion());
            
            nodes.add(node);
        }
        return this;
    }

    @SuppressWarnings("java:S1659")
    private Mesh newCubeMesh(Vec from, Vec to, Collection<Face> faces) {
        var buffer = gltf.getBuffer("Mesh");

        var view = buffer.newView(Target.ARRAY_BUFFER);
        var positionsAcc = view.newAccessor(Type.VEC3, ComponentType.FLOAT);

        var fx = from.x(); var fy = from.y(); var fz = from.z();
        var tx = to.x();   var ty = to.y();   var tz = to.z();
        positionsAcc.add(
            fx, fy, fz,  fx, fy, fz,  fx, fy, fz, // (0) Front Bottom Left
            tx, fy, fz,  tx, fy, fz,  tx, fy, fz, // (1) Front Bottom Right
            tx, ty, fz,  tx, ty, fz,  tx, ty, fz, // (2) Front Top Right
            fx, ty, fz,  fx, ty, fz,  fx, ty, fz, // (3) Front Top Left
            fx, fy, tz,  fx, fy, tz,  fx, fy, tz, // (4) Back Bottom Left
            tx, fy, tz,  tx, fy, tz,  tx, fy, tz, // (5) Back Bottom Right
            tx, ty, tz,  tx, ty, tz,  tx, ty, tz, // (6) Back Top Right
            fx, ty, tz,  fx, ty, tz,  fx, ty, tz  // (7) Back Top Left
        );

        Material material = null;
        Accessor texturesAcc = null;
        if (!faces.isEmpty()) {
            var texture = faces.iterator().next().texture();
            material = gltf.getMaterial(texture.path());
            texturesAcc = uvs(faces);
        }
        return new Mesh(indicesAcc, positionsAcc, normalsAcc, texturesAcc, material);
    }

    private Accessor uvs(Collection<Face> faces) {
        if (faces.isEmpty()) return null;
        
        var buffer = gltf.getBuffer("Mesh");
        var view = buffer.newView(Target.ARRAY_BUFFER);
        var acc = view.newAccessor(Type.VEC2, ComponentType.FLOAT);

        var map = mapFaces(faces);

        uv(acc, map.get(FRONT), 0, 0);  // (0) FRONT - Bottom Left
        uv(acc, map.get(LEFT), 1, 0);   // (1) LEFT - Front Bottom
        uv(acc, map.get(BOTTOM), 1, 0); // (2) BOTTOM - Front Left
        uv(acc, map.get(FRONT), 1, 0);  // (3) FRONT - Bottom Right
        uv(acc, map.get(FRONT), 0, 0);  // (4) BOTTOM - Front Right
        uv(acc, map.get(RIGHT), 0, 0);  // (5) RIGHT - Front Bottom
        uv(acc, map.get(FRONT), 1, 1);  // (6) FRONT - Top Right
        uv(acc, map.get(RIGHT), 0, 1);  // (7) RIGHT - Front Top
        uv(acc, map.get(TOP), 0, 1);    // (8) TOP - Front Right
        uv(acc, map.get(FRONT), 0, 1);  // (9) FRONT - Top Left
        uv(acc, map.get(LEFT), 1, 1);   // (10) LEFT - Front Top
        uv(acc, map.get(TOP), 1, 1);    // (11) TOP - Front Left
        uv(acc, map.get(LEFT), 0, 0);   // (12) LEFT - Back Bottom
        uv(acc, map.get(FRONT), 1, 1);  // (13) BOTTOM - Back Left
        uv(acc, map.get(BACK), 1, 0);   // (14) BACK - Bottom Left
        uv(acc, map.get(FRONT), 0, 1);  // (15) BOTTOM - Back Right
        uv(acc, map.get(BACK), 0, 0);   // (16) BACK - Bottom Right
        uv(acc, map.get(RIGHT), 1, 0);  // (17) RIGHT - Back Bottom
        uv(acc, map.get(BACK), 0, 1);   // (18) BACK - Top Right
        uv(acc, map.get(RIGHT), 1, 1);  // (19) RIGHT - Back Top
        uv(acc, map.get(TOP), 0, 0);    // (20) TOP - Back Right
        uv(acc, map.get(LEFT), 0, 1);   // (21) LEFT - Back Top
        uv(acc, map.get(BACK), 1, 1);   // (22) BACK - Top Left
        uv(acc, map.get(TOP), 1, 0);    // (23) TOP - Back Left

        return acc;
    }

    private void uv(Accessor acc, Face face, int w, int h) {
        var rotate = face.rotation();
        float u;
        float v;
        if (rotate == 0) {
            u = face.fromX() * w + face.toX() * (1-w);
            v = face.fromY() * h + face.toY() * (1-h);
        } else if (rotate == 1) {
            u = face.fromX() * h + face.toX() * (1-h);
            v = face.fromY() * (1-w) + face.toY() * w;
        } else if (rotate == 2) {
            u = face.fromX() * (1-w) + face.toX() * w;
            v = face.fromY() * (1-h) + face.toY() * h;
        } else {
            u = face.fromX() * (1-h) + face.toX() * h;
            v = face.fromY() * w + face.toX() * (1-w);
        }
        acc.add(u, v);
    }

    /**
     * Organize the given faces into a map to retrieve any face by its
     * name, filling missing ones using 'Transparent' texture
     * @param faces the faces to organize
     * @return the map
     */
    private EnumMap<FaceName, Face> mapFaces(Collection<Face> faces) {
        var map = new EnumMap<FaceName, Face>(FaceName.class);

        /* Put all given faces in the map */
        for (var face : faces) {
            map.put(face.name(), face);
        }

        /* Fill missing faces with transparent */
        for (var name : FaceName.values()) {
            map.computeIfAbsent(name, n ->
                map.put(n, new Face(n, 0, 0, 1, 1, 0, ModelTexture.TRANSPARENT))
            );
        }
        return map;
    }


    /**
     * Build one cube indices accessor
     * @return the indices accessor
     */
    private Accessor indicesAccessor() {
        var buffer = gltf.getBuffer("Mesh");
        var view = buffer.newView(Target.ELEMENT_ARRAY_BUFFER);
        var acc = view.newAccessor(Type.SCALAR, ComponentType.UNSIGNED_SHORT);

        acc.add(
             3,  0,  6,   6,  0,  9, // Front
            10,  1, 21,   1, 12, 21, // Left
             2,  4, 15,  13,  2, 15, // Bottom
            18, 22, 14,  16, 18, 14, // Back
            19, 17,  5,   7, 19,  5, // Right
            23, 20, 11,  20,  8, 11  // Top
        );

        return acc;
    }

    /**
     * Build one cube normals accessor
     * @return the normals accessor
     */
    private Accessor normalsAccessor() {
        var buffer = gltf.getBuffer("Mesh");
        var view = buffer.newView(Target.ARRAY_BUFFER);
        var acc = view.newAccessor(Type.VEC3, ComponentType.FLOAT);

        acc.add(
            0, 0, -1,   1, 0, 0,  0, -1, 0, // Front Right Bottom
            0, 0, -1,  0, -1, 0,  -1, 0, 0, // Front Bottom Left
            0, 0, -1,  -1, 0, 0,  0,  1, 0, // Front Left Top
            0, 0, -1,   1, 0, 0,  0,  1, 0, // Front Right Top
             1, 0, 0,  0, -1, 0,  0, 0,  1, // Right Bottom Back
            0, -1, 0,  0, 0,  1,  -1, 0, 0, // Bottom Back Left
            0, 0,  1,  -1, 0, 0,  0,  1, 0, // Back Left Top
             1, 0, 0,  0, 0,  1,  0,  1, 0f  // Right Back Top
        );

        return acc;
    }

    /**
     * Enables pretty JSON formatting.
     * @return this builder
     */
    public GltfBuilder prettify() {
        this.pretty = true;
        return this;
    }

    @Override
    public String toString() {
        gltf.build();
        try {
            return pretty
                ? JSON.writer(getPrettifier()).writeValueAsString(gltf)
                : JSON.writeValueAsString(gltf);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private static PrettyPrinter getPrettifier() {
        var indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        var printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);

        return printer;
    }



    public static void main(String[] args) throws IOException {
        var data = new GltfBuilder()
            .prettify();

        // var model = new ModelParser().parse(Path.of("model.json"));
        var model = new ModelParser().parse(Path.of("assets/minecraft/models/item/backpack/buzz.json"));
        for (var cube : model) data.add(cube);

        var asStr = data.toString();
        System.out.println(asStr);
        Files.writeString(Path.of("cube.gltf"), asStr);
    }
}
