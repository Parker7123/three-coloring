package org.example.algorithms.coloring;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.Coloring;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.ColoringImpl;
import org.jgrapht.graph.SimpleGraph;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ThreeColoringUtils {
    public final static int NUMBER_OF_COLORS = 3;

    public static <V> Coloring<V> emptyThreeColoring() {
        return new ColoringImpl<>(Map.of(), NUMBER_OF_COLORS);
    }

    public static <V, E> Set<V> vertexNeighbors(Graph<V, E> graph, V vertex) {
        return graph.edgesOf(vertex).stream()
                        .map(edge -> Graphs.getOppositeVertex(graph, edge, vertex))
                .collect(toSet());
    }

    @SafeVarargs
    public static <V> Coloring<V> sumOfColorings(Coloring<V>... colorings) {
        return new ColoringImpl<>(
                Stream.of(colorings)
                        .flatMap(m -> m.getColors().entrySet().stream())
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)
                ), NUMBER_OF_COLORS);
    }

    public static<V, E> Map<V, E> toImmutableMap(Map<V, E> map) {
        return map.entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <V, E> Graph<V, E> subgraph(Graph<V, E> graph, Set<V> subgraphVertices) {
        if (graph.edgeSet().isEmpty()) {
            return graph;
        }
        Graph<V, E> subgraph = new SimpleGraph<>(null, graph.getEdgeSupplier(), false);
        Graphs.addAllVertices(subgraph, subgraphVertices);
        Set<E> inducedEdges = graph.edgeSet().stream()
                .filter(e -> subgraphVertices.contains(graph.getEdgeSource(e)) &&
                        subgraphVertices.contains(graph.getEdgeTarget(e)))
                .collect(toSet());
        Graphs.addAllEdges(subgraph, graph, inducedEdges);

        return subgraph;
    }
}
