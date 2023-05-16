package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.PlanarityTestingAlgorithm;
import org.jgrapht.util.DoublyLinkedList;

import java.util.*;

/**
 * Converts combinatorial embedding to simplified doubly connected edge list <p>
 * <a href="https://en.wikipedia.org/wiki/Doubly_connected_edge_list">link</a>
 */

public class EmbeddingToDCELConverter {

    public static <V, E> DCEL<V, E> convert(PlanarityTestingAlgorithm.Embedding<V, E> embedding) {
        List<DCEL.Face<V, E>> faces = new ArrayList<>();
        Graph<V, E> graph = embedding.getGraph();
        Map<DCEL.Node<V, E>, DCEL.Node<V, E>> edgeToNextEdge = new HashMap<>();
        for (V v : graph.vertexSet()) {
            List<E> edgesAroundV = embedding.getEdgesAround(v);
            DCEL.Node<V, E> firstNode = null;
            DCEL.Node<V, E> prevnode = null;
            for (E edge : edgesAroundV) {
                V target = Graphs.getOppositeVertex(graph, edge, v);
                if (prevnode == null) {
                    prevnode = new DCEL.Node<>(v, target, edge);
                    firstNode = prevnode;
                } else {
                    DCEL.Node<V, E> node = new DCEL.Node<>(v, target, edge);
                    edgeToNextEdge.put(prevnode, node);
                    prevnode = node;
                }
            }
            if (firstNode != prevnode) {
                edgeToNextEdge.put(prevnode, firstNode);
            }
        }
        while (!edgeToNextEdge.isEmpty()) {
            var nodesInFace = edgeToNextEdge.keySet().stream()
                    .takeWhile(node -> !edgeToNextEdge.get(node).equals(node))
                    .toList();
            DoublyLinkedList<DCEL.Node<V, E>> face = new DoublyLinkedList<>();
            nodesInFace.forEach(face::addLast);
            faces.add(new DCEL.Face<>(face));
        }
        return new DCEL<>(faces);
    }
}
