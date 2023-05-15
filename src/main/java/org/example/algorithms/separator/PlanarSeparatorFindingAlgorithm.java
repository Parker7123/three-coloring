package org.example.algorithms.separator;

import com.google.common.collect.Sets;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.PlanarityTestingAlgorithm;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.graph.AsSubgraph;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class PlanarSeparatorFindingAlgorithm<V, E> implements SeparatorFindingAlgorithm<V>{

    private final Graph<V, E> sourceGraph;
    private final int n;
    private Set<V> separator;
    private Set<V> subsetA;
    private Set<V> subsetB;
    private List<Set<V>> connectedComponents;
    public PlanarSeparatorFindingAlgorithm(Graph<V, E> sourceGraph) {
        this.sourceGraph = sourceGraph;
        this.n = sourceGraph.vertexSet().size();
        runAlgorithm();
    }

    @Override
    public Set<V> getSparator() {
        return separator;
    }

    @Override
    public Set<V> getSubsetA() {
        return subsetA;
    }

    @Override
    public Set<V> getSubsetB() {
        return subsetB;
    }

    private void preprocessing() {
        PlanarityTestingAlgorithm<V, E> planarityTestingAlgorithm = new BoyerMyrvoldPlanarityInspector<>(sourceGraph);
        if (!planarityTestingAlgorithm.isPlanar()) {
            throw new IllegalStateException("Graph is not planar");
        }
        ConnectivityInspector<V, E> connectivityInspector = new ConnectivityInspector<>(sourceGraph);
        connectedComponents = connectivityInspector.connectedSets();
        if (canApplySimplePartition(connectivityInspector)) {
            findSimplePartition();
        }
    }

    private boolean canApplySimplePartition(ConnectivityInspector<V, E> connectivityInspector) {
        return !connectivityInspector.isConnected() &&
                connectivityInspector.connectedSets().stream().allMatch(set -> set.size() <= n * 2 / 3);
    }

    private void findSimplePartition() {
        separator = Set.of();
        Optional<Set<V>> vertexSetExceedingOneThird = connectedComponents.stream()
                .filter(set -> set.size() > n / 3)
                .findFirst();
        subsetA = vertexSetExceedingOneThird.orElseGet(() -> connectedComponents.stream()
                .reduce((set1, set2) -> set1.size() <= n / 3 ? Sets.union(set1, set2) : set1).orElseThrow()
                .stream().collect(toUnmodifiableSet()));
        subsetB = sourceGraph.vertexSet().stream()
                .filter(v -> !subsetA.contains(v)).collect(toUnmodifiableSet());
    }

    private void runAlgorithm() {
        preprocessing();
        if (this.separator != null) {
            return;
        }
       Set<V> biggestComponent = connectedComponents.stream()
                .max(Comparator.comparing(Set::size))
                .orElseThrow();
        Graph<V, E> biggestComponentGraph = new AsSubgraph<>(sourceGraph, biggestComponent);

        var connectedSeparatorAlg = new PlanarConnectedSeparatorFindingAlgorithm<>(biggestComponentGraph);

        this.separator = connectedSeparatorAlg.getSparator();
        if (connectedSeparatorAlg.getSubsetA().size() > connectedSeparatorAlg.getSubsetB().size()) {
            this.subsetA = connectedSeparatorAlg.getSubsetA();
            this.subsetB = connectedSeparatorAlg.getSubsetB();
        } else {
            this.subsetA = connectedSeparatorAlg.getSubsetB();
            this.subsetB = connectedSeparatorAlg.getSubsetA();
        }
        this.subsetB = sourceGraph.vertexSet().stream()
                .filter(v -> !subsetA.contains(v) && !separator.contains(v))
                .collect(toUnmodifiableSet());
    }
}
