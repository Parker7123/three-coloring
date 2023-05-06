package org.example.algorithms.separator;

import com.google.common.collect.Sets;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Finds separator that separates single vertex from the rest of the graph.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class SimpleSeparatorFindingAlgorithm<V, E> implements SeparatorFindingAlgorithm<V> {

    private final Graph<V, E> sourceGraph;
    private Set<V> separator;
    private Set<V> subsetA;
    private Set<V> subsetB;

    public SimpleSeparatorFindingAlgorithm(Graph<V, E> sourceGraph) {
        this.sourceGraph = sourceGraph;
        computeSeparatorAndSets();
    }

    private void computeSeparatorAndSets() {
        if (sourceGraph.vertexSet().isEmpty()) {
            this.separator =  Collections.emptySet();
        }
        V isolatedVertex = sourceGraph.vertexSet().stream().findFirst().orElseThrow();
        this.separator = sourceGraph.edgesOf(isolatedVertex).stream()
                .map(edge -> Graphs.getOppositeVertex(sourceGraph, edge, isolatedVertex))
                .collect(toSet());
        this.subsetA = Set.of(isolatedVertex);
        this.subsetB = Sets.difference(sourceGraph.vertexSet(), Sets.union(subsetA, separator));
    }

    @Override
    public Set<V> getSparator() {
        return this.separator;
    }

    @Override
    public Set<V> getSubsetA() {
        return this.subsetA;
    }

    @Override
    public Set<V> getSubsetB() {
        return this.subsetB;
    }
}
