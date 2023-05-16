package org.example.algorithms.planar;

import org.jgrapht.util.DoublyLinkedList;

import java.util.List;

public class DCEL<V, E> {
    private final List<Face<V, E>> faces;

    public DCEL(List<Face<V, E>> faces) {
        this.faces = faces;
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

    public static final class Node<V, E> {
        private final V v;
        private final V target;
        private final E edge;

        public Node(V v, V target, E edge) {
            this.v = v;
            this.target = target;
            this.edge = edge;
        }

        public V v() {
            return v;
        }

        public V target() {
            return target;
        }

        public E edge() {
            return edge;
        }

        @Override
        public String toString() {
            return "Node[" +
                    "v=" + v + ", " +
                    "target=" + target + ", " +
                    "edge=" + edge + ']';
        }
    }
}
