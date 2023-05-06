package org.example.algorithms.coloring;

import org.example.algorithms.separator.SeparatorFindingAlgorithm;
import org.example.algorithms.separator.SimpleSeparatorFindingAlgorithm;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.graph.AsSubgraph;

import java.util.List;
import java.util.Set;

import static org.example.algorithms.coloring.ThreeColoringUtils.emptyThreeColoring;
import static org.example.algorithms.coloring.ThreeColoringUtils.sumOfColorings;

public class PlanarThreeColoring<V, E> implements VertexColoringAlgorithm<V> {

    private final Graph<V, E> sourceGraph;
    private final int sourceGraphSize;

    @Override
    public Coloring<V> getColoring() {
        return threeColoringForPlanarGraphAndColoredNeighbors(sourceGraph, emptyThreeColoring());
    }

    public PlanarThreeColoring(Graph<V, E> sourceGraph) {
        this.sourceGraph = sourceGraph;
        this.sourceGraphSize = sourceGraph.vertexSet().size();
    }

    private Coloring<V> threeColoringForPlanarGraphAndColoredNeighbors(Graph<V, E> graph, Coloring<V> coloredNeighbors) {
        if (graph.vertexSet().size() <= Math.sqrt(sourceGraphSize)) {
            return new ThreeColoringForGraphAndColoredNeighbors<>(graph, sourceGraph, coloredNeighbors).getColoring();
        }
        // TODO: Implement PlanarSeparatorFindingAlgorithm and replace
        SeparatorFindingAlgorithm<V> separatorFindingAlgorithm = new SimpleSeparatorFindingAlgorithm<>(graph);
        Set<V> separator = separatorFindingAlgorithm.getSparator();
        Set<V> subsetA = separatorFindingAlgorithm.getSubsetA();
        Set<V> subsetB = separatorFindingAlgorithm.getSubsetB();
        // TODO: AsSubgraph is not so optimal, replace with creating separate grap
        Graph<V, E> graphInducedBySeparator = new AsSubgraph<>(graph, separator);
        Graph<V, E> graphInducedBySubsetA = new AsSubgraph<>(graph, subsetA);
        Graph<V, E> graphInducedBySubsetB = new AsSubgraph<>(graph, subsetB);

        var threeColoringAlgorithm = new ThreeColoringForGraphAndColoredNeighbors<>(graphInducedBySeparator, graph,
                coloredNeighbors);
        List<Coloring<V>> validSeparatorColorings = threeColoringAlgorithm.getListOfValidColorings();
        if (validSeparatorColorings.isEmpty()) {
            return null;
        }
        for (Coloring<V> separatorColoring : validSeparatorColorings) {
            var coloredNeighborsWithSeparator = sumOfColorings(coloredNeighbors, separatorColoring);
            var subsetAColoring = threeColoringForPlanarGraphAndColoredNeighbors(graphInducedBySubsetA,
                    coloredNeighborsWithSeparator);
            var subsetBColoring = threeColoringForPlanarGraphAndColoredNeighbors(graphInducedBySubsetB,
                    coloredNeighborsWithSeparator);
            if (subsetAColoring != null && subsetBColoring != null) {
                return sumOfColorings(separatorColoring, subsetAColoring, subsetBColoring);
            }
        }
        return null;
    }
}
