package org.example.algorithms.coloring;

import com.google.common.base.Suppliers;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.Coloring;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;
import static org.example.algorithms.coloring.ThreeColoringUtils.*;

/**
 * Brute force implementation of three coloring
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class ThreeColoringForGraphAndColoredNeighbors<V, E> implements VertexColoringAlgorithm<V>,
        Iterator<Coloring<V>>, Iterable<Coloring<V>> {

    private static final Set<Integer> possibleColors = Set.of(1, 2, 3);
    private final Graph<V, E> sourceGraph;
    private final Map<V, Set<Integer>> allowedColors;
    private final Stack<VertexWithColor<V, Integer>> colorsToVerify = new Stack<>();
    private final Map<V, Integer> coloredVertices = new HashMap<>();
    private final LinkedList<V> verticesList;
    private final ListIterator<V> iterator;
    private Optional<Coloring<V>> nextColoring = Optional.empty();
    private final Supplier<List<Coloring<V>>> coloringListSupplier = Suppliers.memoize(() ->
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.NONNULL), false)
                    .collect(toList()));
    final boolean coloringImpossible;

    public ThreeColoringForGraphAndColoredNeighbors(Graph<V, E> sourceGraph, Map<V, Set<Integer>> restrictedColors) {
        this.sourceGraph = sourceGraph;
        this.allowedColors = generateAllowedColors(sourceGraph.vertexSet(), restrictedColors);
        this.coloringImpossible = restrictedColors.values().stream()
                .anyMatch(colors -> colors.size() == NUMBER_OF_COLORS);
        this.verticesList = new LinkedList<>(sourceGraph.vertexSet());
        this.iterator = verticesList.listIterator();
        initIterator();
    }

    @Override
    public Coloring<V> getColoring() {
        return generateNextValidColoring().orElse(null);
    }

    private static <V> Map<V, Set<Integer>> generateAllowedColors(Set<V> vertices, Map<V, Set<Integer>> restrictedColors) {
        return vertices.stream()
                .collect(toMap(v -> v, v -> {
                    var set = new HashSet<>(possibleColors);
                    set.removeAll(restrictedColors.getOrDefault(v, Set.of()));
                    return set;
                }));
    }

    public List<Coloring<V>> getListOfValidColorings() {
        if (coloringImpossible) {
            return List.of();
        }
        return coloringListSupplier.get();
    }

    private void initIterator() {
        if (!coloringImpossible && !sourceGraph.vertexSet().isEmpty()) {
            V firstVertex = verticesList.getFirst();
            allowedColors.get(firstVertex).forEach(color -> colorsToVerify.push(new VertexWithColor<>(firstVertex, color)));
            iterator.next();
        }
    }

    private Optional<Coloring<V>> generateNextValidColoring() {
        while (!colorsToVerify.isEmpty()) {
            VertexWithColor<V, Integer> currentVertexWithColor = colorsToVerify.pop();
            resetIteratorAfterElementBackwards(iterator, currentVertexWithColor.getVertex());
            coloredVertices.put(currentVertexWithColor.getVertex(), currentVertexWithColor.getColor());
            fillAllColorsTillTheEnd(colorsToVerify, coloredVertices, iterator);
            if (verifyColoring(coloredVertices)) {
                Coloring<V> validColoring = new ColoringImpl<>(toImmutableMap(coloredVertices), NUMBER_OF_COLORS);
                return Optional.of(validColoring);
            }
        }
        if (sourceGraph.vertexSet().isEmpty()) {
            return Optional.of(emptyThreeColoring());
        }
        return Optional.empty();
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
            coloredVertices.put(currentVertex, allowedColors.get(currentVertex).stream().findFirst().get());
            allowedColors.get(currentVertex).stream()
                    .skip(1)
                    .forEach(color -> colorsToVerify.push(new VertexWithColor<>(currentVertex, color)));
        }
    }

    public Stream<Coloring<V>> asStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.NONNULL), false);
    }

    private void fetchNextColoring() {
        this.nextColoring = generateNextValidColoring();
    }

    @Override
    public boolean hasNext() {
        fetchNextColoring();
        return nextColoring.isPresent();
    }

    @Override
    public Coloring<V> next() {
        Coloring<V> coloring = nextColoring.orElseThrow();
        nextColoring = Optional.empty();
        return coloring;
    }

    @Override
    public Iterator<Coloring<V>> iterator() {
        return this;
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
