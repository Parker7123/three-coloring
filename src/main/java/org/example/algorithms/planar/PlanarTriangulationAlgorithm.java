package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.PlanarityTestingAlgorithm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.example.algorithms.coloring.ThreeColoringUtils.vertexNeighbors;

/**
 * https://ti.inf.ethz.ch/ew/courses/Geo20/lecture/gca20-2.pdf
 * Theorem 2.30. For a given connected plane graph G = (V, E) on n vertices one can
 * compute in O(n) time and space a maximal plane graph G1 = (V, E1) with E ⊆ E1
 *
 * @param <V>
 * @param <E>
 */
public class PlanarTriangulationAlgorithm<V, E> {
    private EmbeddingWithFaces<V, E> embedding;
    private final Graph<V, E> sourceGraph;

    public PlanarTriangulationAlgorithm(PlanarityTestingAlgorithm.Embedding<V, E> embedding) {
        this.embedding = EmbeddingToEmbeddingWithFacesConverter.convert(embedding);
        this.sourceGraph = embedding.getGraph();
    }

    public PlanarTriangulationAlgorithm(EmbeddingWithFaces<V, E> embedding) {
        this.embedding = embedding;
        this.sourceGraph = embedding.getGraph();
    }

    private void makeEachFaceACycle() {
        List<EmbeddingWithFaces.Face<V, E>> faces = embedding.getFaces();
        for (var face : faces) {
            Set<V> verticesTraversed = new HashSet<>();
            var edgeIterator = face.edges().listIterator();
            while (edgeIterator.hasNext()) {
                var edge = edgeIterator.next();
                if (verticesTraversed.contains(edge.v())) {
                    var prevEdge = edgeIterator.previous();
                    edgeIterator.next();
                    sourceGraph.addEdge(prevEdge.v(), edge.target());
                }
                verticesTraversed.add(edge.v());
            }
        }
        embedding = EmbeddingToEmbeddingWithFacesConverter.buildEmbeddingFromGraph(sourceGraph);
    }

    public EmbeddingWithFaces<V, E> triangulate() {
        makeEachFaceACycle();
       var vertexToFaces =  embedding.getFaces().stream()
                .collect(groupingBy(face -> face.edges().getFirst().v(), mapping(face -> face, toSet())));
        for (V vertex : vertexToFaces.keySet()) {
            Set<V> vertexNeighbors = vertexNeighbors(sourceGraph, vertex);
            for (var face : vertexToFaces.get(vertex)) {
                var neighborsOnTheCycle = face.edges().stream()
                        .skip(1)
                        .dropWhile(node -> !vertexNeighbors.contains(node.target()))
                        .limit(2)
                        .toList();
                if (!neighborsOnTheCycle.get(1).target().equals(vertex)) {
                    var neighbor = neighborsOnTheCycle.get(0);
                    var nneighbor = neighborsOnTheCycle.get(1);
                    var e1 = face.edges().stream().findFirst().orElseThrow();
                    // bi-star triangulation
                    face.edges().stream()
                            .skip(1)
                            .takeWhile(node -> !node.equals(neighbor))
                            .map(EmbeddingWithFaces.Node::target)
                            .forEach(v -> {
                                sourceGraph.addEdge(nneighbor.target(), v);
                            });
                    face.edges().stream()
                            .dropWhile(node -> !node.equals(neighbor))
                            .map(EmbeddingWithFaces.Node::target)
                            .forEach(v -> {
                                if (!v.equals(e1.v()) && !v.equals(neighbor.target())) {
                                    sourceGraph.addEdge(v, e1.target());
                                }
                            });
                } else {
                    // no risk of double edges star triangulation
                    face.edges().stream()
                            .skip(1)
                            .limit(face.edges().size() - 3)
                            .map(EmbeddingWithFaces.Node::target)
                            .forEach(v -> {
                                sourceGraph.addEdge(vertex, v);
                            });
                }
            }
        }
        return EmbeddingToEmbeddingWithFacesConverter.buildEmbeddingFromGraph(sourceGraph);
    }
}
