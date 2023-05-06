package org.example.algorithms.coloring;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.Coloring;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.ColoringImpl;

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

    public static <V, E> Set<V> vertexSetNeighbors(Graph<V, E> graph, Set<V> vertices) {
        return vertices.stream().flatMap(vertex -> graph.edgesOf(vertex).stream()
                        .map(edge -> Graphs.getOppositeVertex(graph, edge, vertex)))
                .filter(v -> !vertices.contains(v))
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
}
