package org.example.algorithms.coloring;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.example.algorithms.coloring.ThreeColoringUtils.*;

/**
 * Brute force implementation of three coloring
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class ThreeColoringForGraphAndColoredNeighbors<V, E> implements VertexColoringAlgorithm<V> {
    private final Graph<V, E> sourceGraph;
    private final Map<V, Set<Integer>> restrictedColors;

    public ThreeColoringForGraphAndColoredNeighbors(Graph<V, E> sourceGraph, Map<V, Set<Integer>> restrictedColors) {
        this.sourceGraph = sourceGraph;
        this.restrictedColors = restrictedColors;
    }

    @Override
    public Coloring<V> getColoring() {
        List<Coloring<V>> validColorings = getListOfValidColorings();
        if (validColorings.isEmpty()) {
            return null;
        }
        return validColorings.get(0);
    }

    public List<Coloring<V>> getListOfValidColorings() {
        boolean coloringImpossible = restrictedColors.values().stream()
                .anyMatch(restrictedColors -> restrictedColors.size() == NUMBER_OF_COLORS);
        if (coloringImpossible){
            return List.of();
        }
        if (sourceGraph.vertexSet().isEmpty()) {
            return List.of(emptyThreeColoring());
        }
        List<Coloring<V>> validColorings = new ArrayList<>();
        Stack<VertexWithColor<V, Integer>> colorsToVerify = new Stack<>();
        LinkedList<V> verticesList = new LinkedList<>(sourceGraph.vertexSet());
        Map<V, Integer> coloredVertices = new HashMap<>();

        V firstVertex = verticesList.get(0);
        allowedColors(firstVertex).forEach(color -> colorsToVerify.push(new VertexWithColor<>(firstVertex, color)));
        var iterator = verticesList.listIterator();
        iterator.next();
        while (!colorsToVerify.isEmpty()) {
            VertexWithColor<V, Integer> currentVertexWithColor = colorsToVerify.pop();
            resetIteratorAfterElementBackwards(iterator, currentVertexWithColor.getVertex());
            coloredVertices.put(currentVertexWithColor.getVertex(), currentVertexWithColor.getColor());
            fillAllColorsTillTheEnd(colorsToVerify, coloredVertices, iterator);
            if (verifyColoring(coloredVertices)) {
                Coloring<V> validColoring = new ColoringImpl<>(toImmutableMap(coloredVertices), NUMBER_OF_COLORS);
                validColorings.add(validColoring);
            }
        }
        return validColorings;
    }

    private boolean verifyColoring(Map<V, Integer> coloredVertices) {
        return sourceGraph.edgeSet().stream()
                .noneMatch(e -> coloredVertices.get(sourceGraph.getEdgeSource(e))
                        .equals(coloredVertices.get(sourceGraph.getEdgeTarget(e))));
    }

    private static <V> void resetIteratorAfterElementBackwards(ListIterator<V> iterator, V elementResetTo) {
        while (iterator.hasPrevious()) {
            V currentElement = iterator.previous();
            if (currentElement.equals(elementResetTo)) {
                iterator.next();
                return;
            }
        }
        throw new IllegalStateException("Element: " + elementResetTo + " not found.");
    }

    private void fillAllColorsTillTheEnd(Stack<VertexWithColor<V, Integer>> colorsToVerify,
                                                    Map<V, Integer> coloredVertices, ListIterator<V> iterator) {
        while (iterator.hasNext()) {
            V currentVertex = iterator.next();
            coloredVertices.put(currentVertex, allowedColors(currentVertex).get(0));
            allowedColors(currentVertex).stream()
                    .skip(1)
                    .forEach(color -> colorsToVerify.push(new VertexWithColor<>(currentVertex, color)));
        }
    }

    private List<Integer> allowedColors(V vertex) {
        Set<Integer> disallowedColors = restrictedColors.getOrDefault(vertex, Set.of());
        return Stream.of(0, 1, 2).filter(color -> !disallowedColors.contains(color)).collect(toList());
    }

    private static class VertexWithColor<A, B> extends Pair<A, B> {

        /**
         * Create a new pair
         *
         * @param a the first element
         * @param b the second element
         */
        public VertexWithColor(A a, B b) {
            super(a, b);
        }

        public A getVertex() {
            return first;
        }

        public B getColor() {
            return second;
        }
    }
}
