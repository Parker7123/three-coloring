package org.example.algorithms.coloring;

import org.example.algorithms.separator.PlanarSeparatorFindingAlgorithm;
import org.example.algorithms.separator.SeparatorFindingAlgorithm;
import org.example.algorithms.separator.SimpleSeparatorFindingAlgorithm;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.*;
import static org.example.algorithms.coloring.ThreeColoringUtils.subgraph;
import static org.example.algorithms.coloring.ThreeColoringUtils.sumOfColorings;

public class PlanarThreeColoring<V, E> implements VertexColoringAlgorithm<V> {

    private final Graph<V, E> sourceGraph;
    private final int sourceGraphSize;

    @Override
    public Coloring<V> getColoring() {
        return threeColoringForPlanarGraphAndColoredNeighbors(sourceGraph, new HashMap<>());
    }

    public PlanarThreeColoring(Graph<V, E> sourceGraph) {
        this.sourceGraph = sourceGraph;
        this.sourceGraphSize = sourceGraph.vertexSet().size();
    }

    private Coloring<V> threeColoringForPlanarGraphAndColoredNeighbors(Graph<V, E> graph, Map<V, Set<Integer>> restrictedColors) {
        if (graph.vertexSet().size() <= Math.sqrt(sourceGraphSize)) {
            return new ThreeColoringForGraphAndColoredNeighbors<>(graph, unmodifiableMap(restrictedColors)).getColoring();
        }
        SeparatorFindingAlgorithm<V> separatorFindingAlgorithm = new PlanarSeparatorFindingAlgorithm<>(graph);
        Set<V> separator = separatorFindingAlgorithm.getSparator();
        Set<V> subsetA = separatorFindingAlgorithm.getSubsetA();
        Set<V> subsetB = separatorFindingAlgorithm.getSubsetB();
        Graph<V, E> graphInducedBySeparator = subgraph(graph, separator);

        var threeColoringAlgorithm = new ThreeColoringForGraphAndColoredNeighbors<>(graphInducedBySeparator,
                unmodifiableMap(restrictedColors));
        List<Coloring<V>> validSeparatorColorings = threeColoringAlgorithm.getListOfValidColorings();
        if (validSeparatorColorings.isEmpty()) {
            return null;
        }
        Graph<V, E> graphInducedBySubsetA = subgraph(graph, subsetA);
        Graph<V, E> graphInducedBySubsetB = subgraph(graph, subsetB);
        for (Coloring<V> separatorColoring : validSeparatorColorings) {
            var currentlyRestrictedColors = generateRestrictedColors(separatorColoring);
            var mergedRestrictedColors = mergeRestrictedColors(restrictedColors, currentlyRestrictedColors);
            // TODO: idea: introduce map: vertex -> color
            // and update these this map when returning coloring
            var subsetAColoring = threeColoringForPlanarGraphAndColoredNeighbors(graphInducedBySubsetA,
                    mergedRestrictedColors);
            var subsetBColoring = threeColoringForPlanarGraphAndColoredNeighbors(graphInducedBySubsetB,
                    mergedRestrictedColors);
            if (subsetAColoring != null && subsetBColoring != null) {
                return sumOfColorings(separatorColoring, subsetAColoring, subsetBColoring);
            }
        }
        return null;
    }

    /**
     * Complexity: O(n), iterates over edges connected to colored vertices (planar graphs have O(n) edges)
     */
    private Map<V, Set<Integer>> generateRestrictedColors(Coloring<V> coloring) {
        return coloring.getColors().entrySet().stream()
                .flatMap(vColorEntry -> sourceGraph.edgesOf(vColorEntry.getKey())
                        .stream().map(edge -> new Pair<>(
                                Graphs.getOppositeVertex(sourceGraph, edge, vColorEntry.getKey()),
                                vColorEntry.getValue()))
                ).distinct()
                .collect(groupingBy(Pair::getFirst, mapping(Pair::getSecond, toSet())));
    }

    /**
     * Complexity: O(n)
     */
    private Map<V, Set<Integer>> mergeRestrictedColors(Map<V, Set<Integer>> c1,Map<V, Set<Integer>>c2) {
        return Stream.of(c1, c2)
                .flatMap(vSetMap -> vSetMap.entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue,
                        Collectors.flatMapping(Collection::stream, toSet()))));
    }
}
