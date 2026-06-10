package io.github.mrmiumo.mi2gltf;

import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.github.mrmiumo.mi2gltf.nodes.Node;

public class Scene {
    private final String name;
    private final HashSet<Node> nodes = new HashSet<>();

    public Scene(String name) { this.name = name; }

    /**
     * Adds a new node to the nodes list.
     * @param i the index of the node to reference
     * @return this scene
     */
    public Scene addNodes(Node... nodes) {
        for (var node : nodes) this.nodes.add(node);
        return this;
    }
    
    @JsonGetter
    public String name() {
        return name; 
    }
    
    @JsonGetter
    public int[] nodes() {
        return nodes.stream().mapToInt(Node::index).toArray(); 
    }
}

