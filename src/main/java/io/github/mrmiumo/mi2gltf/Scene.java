package io.github.mrmiumo.mi2gltf;

import java.util.Arrays;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.github.mrmiumo.mi2gltf.nodes.Node;

/**
 * GLTF object that list all the root nodes of the model
 */
public class Scene {
    private final String name;
    private final HashSet<Node> nodes = new HashSet<>();

    /**
     * Creates a new scene with the given name
     * @param name the name of the scene
     */
    public Scene(String name) { this.name = name; }

    /**
     * Adds a new node to the nodes list.
     * @param nodes the nodes to add
     * @return this scene
     */
    public Scene addNodes(Node... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        return this;
    }
    
    /**
     * Gets the name of the scene
     * @return the name of the scene
     */
    @JsonGetter
    public String name() {
        return name; 
    }
    
    /**
     * Gets the root nodes to display in this scene
     * @return the nodes of this scene
     */
    @JsonGetter
    public int[] nodes() {
        return nodes.stream().mapToInt(Node::index).toArray(); 
    }
}

