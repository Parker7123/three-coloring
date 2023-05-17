package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.PlanarityTestingAlgorithm;
import org.jgrapht.util.DoublyLinkedList;

import java.util.*;

/**
 * Converts combinatorial embedding to simplified doubly connected edge list <p>
 * <a href="https://math.stackexchange.com/questions/4564963/from-combinatorial-embedding-to-dcel-in-linear-time">
 *     link to algorithm description
 * </a>
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
                V oppositeVertex = Graphs.getOppositeVertex(graph, edge, v);
                if (prevnode == null) {
                    prevnode = new DCEL.Node<>(oppositeVertex, v, edge);
                    firstNode = prevnode;
                } else {
                    DCEL.Node<V, E> node = new DCEL.Node<>(v, oppositeVertex, edge);
                    edgeToNextEdge.put(prevnode, node);
                    prevnode = new DCEL.Node<>(oppositeVertex, v, edge);
                }
            }
            if (firstNode != prevnode) {
                edgeToNextEdge.put(prevnode, new DCEL.Node<>(firstNode.target(), firstNode.v(), firstNode.edge()));
            }
        }
        while (!edgeToNextEdge.isEmpty()) {
            var nodesInFace = new LinkedHashSet<DCEL.Node<V,E>>();
            var currentNode = edgeToNextEdge.keySet().stream().findFirst().get();
            var nextNode = edgeToNextEdge.get(currentNode);
            while (!nodesInFace.contains(nextNode)) {
                nodesInFace.add(currentNode);
                currentNode = nextNode;
                nextNode = edgeToNextEdge.get(currentNode);
            }
            nodesInFace.add(currentNode);

            DoublyLinkedList<DCEL.Node<V, E>> face = new DoublyLinkedList<>();
            nodesInFace.forEach(face::addLast);
            faces.add(new DCEL.Face<>(face));
            nodesInFace.forEach(edgeToNextEdge::remove);
        }
        return new DCEL<>(faces);
    }
}
