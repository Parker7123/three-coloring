package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.util.DoublyLinkedList;

import java.util.List;

public class EmbeddingWithFaces<V, E> {
    private final List<Face<V, E>> faces;
    private final Graph<V, E> graph;

    public EmbeddingWithFaces(List<Face<V, E>> faces, Graph<V, E> graph) {
        this.faces = faces;
        this.graph = graph;
    }

    public List<Face<V, E>> getFaces() {
        return faces;
    }

    public Graph<V, E> getGraph() {
        return graph;
    }

    public static final class Face<V, E> {
        private final DoublyLinkedList<Node<V, E>> edges;

        public Face(DoublyLinkedList<Node<V, E>> edges) {
            this.edges = edges;
        }

        public DoublyLinkedList<Node<V, E>> edges() {
            return edges;
        }

        @Override
        public String toString() {
            return "Face[" +
                    "edges=" + edges + ']';
        }
    }

    public record Node<V, E>(V v, V target, E edge) {
    }
}
