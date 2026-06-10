package io.github.mrmiumo.mi2gltf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrmiumo.mi2gltf.Accessor.ComponentType;
import io.github.mrmiumo.mi2gltf.Accessor.Type;
import io.github.mrmiumo.mi2gltf.BufferView.Target;

public class GltfBuilder {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final Gltf gltf = new Gltf();

    private boolean pretty = false;

    public GltfBuilder add(Cube cube) {
        final var nodes = gltf.nodes();

        if (cube.axis == 0) {
            /* No rotation, one node is enough */
            var node = new Node()
                .setReferenced(true)
                .setMesh(newCubeMesh(cube.from(), cube.to()));
            
            nodes.add(node);
        } else {
            /* Rotation, pivot node needed */
            var child = new Node()
                .setReferenced(false)
                .setMesh(newCubeMesh(
                    cube.from().sub(cube.pivot),
                    cube.to().sub(cube.pivot)
                ));
            var parent = new Node()
                .setReferenced(true)
                .translate(cube.pivot)
                .rotate(cube.quaternion())
                .addChild(child);
            
            nodes.add(child);
            nodes.add(parent);
        }
        return this;
    }

    @SuppressWarnings("java:S1659")
    private Mesh newCubeMesh(Vec from, Vec to) {
        var buffer = gltf.getBuffer("Mesh");

        var viewPositions = buffer.newView(Target.ARRAY_BUFFER);
        var viewNormals = buffer.newView(Target.ARRAY_BUFFER);
        var viewIndices = buffer.newView(Target.ELEMENT_ARRAY_BUFFER);

        var accPositions = viewPositions.newAccessor(Type.VEC3, ComponentType.FLOAT);
        var accNormals = viewNormals.newAccessor(Type.VEC3, ComponentType.FLOAT);
        var accIndices = viewIndices.newAccessor(Type.SCALAR, ComponentType.UNSIGNED_SHORT);

        var fx = from.x(); var fy = from.y(); var fz = from.z();
        var tx = to.x();   var ty = to.y();   var tz = to.z();
        accPositions.add(
            fx, fy, fz,  fx, fy, fz,  fx, fy, fz, // (0) Front Bottom Left
            tx, fy, fz,  tx, fy, fz,  tx, fy, fz, // (1) Front Bottom Right
            tx, ty, fz,  tx, ty, fz,  tx, ty, fz, // (2) Front Top Right
            fx, ty, fz,  fx, ty, fz,  fx, ty, fz, // (3) Front Top Left
            fx, fy, tz,  fx, fy, tz,  fx, fy, tz, // (4) Back Bottom Left
            tx, fy, tz,  tx, fy, tz,  tx, fy, tz, // (5) Back Bottom Right
            tx, ty, tz,  tx, ty, tz,  tx, ty, tz, // (6) Back Top Right
            fx, ty, tz,  fx, ty, tz,  fx, ty, tz  // (7) Back Top Left
        );

        accIndices.add(
             3,  0,  6,   6,  0,  9, // Front
            10,  1, 21,   1, 12, 21, // Right
             2,  4, 15,  13,  2, 15, // Bottom
            18, 22, 14,  16, 18, 14, // Back
            19, 17,  5,   7, 19,  5, // Left
            23, 20, 11,  20,  8, 11  // Top
        );

        var n = 1f;
        accNormals.add(
            0, 0, -n,   n, 0, 0,  0, -n, 0, // Front Right Bottom
            0, 0, -n,  0, -n, 0,  -n, 0, 0, // Front Bottom Left
            0, 0, -n,  -n, 0, 0,  0,  n, 0, // Front Left Top
            0, 0, -n,   n, 0, 0,  0,  n, 0, // Front Right Top
             n, 0, 0,  0, -n, 0,  0, 0,  n, // Right Bottom Back
            0, -n, 0,  0, 0,  n,  -n, 0, 0, // Bottom Back Left
            0, 0,  n,  -n, 0, 0,  0,  n, 0, // Back Left Top
             n, 0, 0,  0, 0,  n,  0,  n, 0   // Right Back Top
        );

        return new Mesh(accIndices, accPositions, accNormals);
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

    public record Cube(Vec from, Vec to, float angle, char axis, Vec pivot, Face... faces) {

        Cube(Vec from, Vec to, Face... faces) {
            this(from, to, 0f, (char)0, null, faces);
        }

        public Cube rotate(float angle, char axis, Vec pivot) {
            return new Cube(from, to, angle, axis, pivot, faces);
        }

        /**
         * Gets the rotation as a quaternion [x,y,z,w] if a rotation is set.
         * @return the rotation quaternion, or null if no rotation is set
         */
        @SuppressWarnings("java:S1168")
        public float[] quaternion() {
            float rad = (float) Math.toRadians(angle);
            if (axis == 'x') return new float[]{ sin2(rad), 0, 0, cos2(rad) };
            if (axis == 'y') return new float[]{ 0, sin2(rad), 0, cos2(rad) };
            if (axis == 'z') return new float[]{ 0, 0, sin2(rad), cos2(rad) };
            return null;
        }

        private static float cos2(float a) { return (float)Math.cos(a/2); }
        private static float sin2(float a) { return (float)Math.sin(a/2); }
    }

    public record Face(String face, float uvX, float uvY, float uvW, float uvH, int rotation, String texture) {}
}

