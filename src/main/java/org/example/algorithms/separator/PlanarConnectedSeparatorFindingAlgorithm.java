package org.example.algorithms.separator;

import org.jgrapht.Graph;

import java.util.Set;

public class PlanarConnectedSeparatorFindingAlgorithm<V, E> implements SeparatorFindingAlgorithm<V> {
    private final Graph<V, E> sourceGraph;
    private final int n;
    private Set<V> separator;
    private Set<V> subsetA;
    private Set<V> subsetB;

    public PlanarConnectedSeparatorFindingAlgorithm(Graph<V, E> sourceGraph) {
        this.sourceGraph = sourceGraph;
        this.n = sourceGraph.vertexSet().size();
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

    private void runAlgorithm() {
        // 1
        // znajdź poziomy bfsem - BreadthFirstIterator
        // sprawdź warunki czy już ok
        // nowy graf, zastępujemi jakieś wierzchołki jednym
        // drzewo rozpinające

        // 2
        // triangulacja triangulate()

        // 3
        // całe szukanie cyklu
    }

    private Graph<V, E> triangulate() {
        // tutaj dodać algorytm triangulacji, najlepiej w nowej klasie
        return null;
    }
}
