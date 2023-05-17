package org.example.algorithms.planar;

import org.jgrapht.util.DoublyLinkedList;

import java.util.List;

public class DCEL<V, E> {
    private final List<Face<V, E>> faces;

    public DCEL(List<Face<V, E>> faces) {
        this.faces = faces;
    }

    public List<Face<V, E>> getFaces() {
        return faces;
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
