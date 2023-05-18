package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.PlanarityTestingAlgorithm;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.util.DoublyLinkedList;

import java.util.*;

/**
 * Converts combinatorial embedding to simplified doubly connected edge list <p>
 * <a href="https://math.stackexchange.com/questions/4564963/from-combinatorial-embedding-to-dcel-in-linear-time">
 *     link to algorithm description
 * </a>
 */

public class EmbeddingToEmbeddingWithFacesConverter {

    public static <V, E> EmbeddingWithFaces<V, E> buildEmbeddingFromGraph(Graph<V, E> graph) {
        var embedding = new BoyerMyrvoldPlanarityInspector<>(graph).getEmbedding();
        return convert(embedding);
    }

    public static <V, E> EmbeddingWithFaces<V, E> convert(PlanarityTestingAlgorithm.Embedding<V, E> embedding) {
        List<EmbeddingWithFaces.Face<V, E>> faces = new ArrayList<>();
        Graph<V, E> graph = embedding.getGraph();
        Map<EmbeddingWithFaces.Node<V, E>, EmbeddingWithFaces.Node<V, E>> edgeToNextEdge = new HashMap<>();
        for (V v : graph.vertexSet()) {
            List<E> edgesAroundV = embedding.getEdgesAround(v);
            EmbeddingWithFaces.Node<V, E> firstNode = null;
            EmbeddingWithFaces.Node<V, E> prevnode = null;
            for (E edge : edgesAroundV) {
                V oppositeVertex = Graphs.getOppositeVertex(graph, edge, v);
                if (prevnode == null) {
                    prevnode = new EmbeddingWithFaces.Node<>(oppositeVertex, v, edge);
                    firstNode = prevnode;
                } else {
                    EmbeddingWithFaces.Node<V, E> node = new EmbeddingWithFaces.Node<>(v, oppositeVertex, edge);
                    edgeToNextEdge.put(prevnode, node);
                    prevnode = new EmbeddingWithFaces.Node<>(oppositeVertex, v, edge);
                }
            }
            if (firstNode != prevnode) {
                edgeToNextEdge.put(prevnode, new EmbeddingWithFaces.Node<>(firstNode.target(), firstNode.v(), firstNode.edge()));
            }
        }
        while (!edgeToNextEdge.isEmpty()) {
            var nodesInFace = new LinkedHashSet<EmbeddingWithFaces.Node<V,E>>();
            var currentNode = edgeToNextEdge.keySet().stream().findFirst().get();
            var nextNode = edgeToNextEdge.get(currentNode);
            while (!nodesInFace.contains(nextNode)) {
                nodesInFace.add(currentNode);
                currentNode = nextNode;
                nextNode = edgeToNextEdge.get(currentNode);
            }
            nodesInFace.add(currentNode);

            DoublyLinkedList<EmbeddingWithFaces.Node<V, E>> face = new DoublyLinkedList<>();
            nodesInFace.forEach(face::addLast);
            faces.add(new EmbeddingWithFaces.Face<>(face));
            nodesInFace.forEach(edgeToNextEdge::remove);
        }
        return new EmbeddingWithFaces<>(faces, graph);
    }
}
