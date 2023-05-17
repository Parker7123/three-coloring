package org.example.algorithms.planar;

import org.jgrapht.Graph;

import java.util.*;

public class PlanarTriangulationAlgorithm<V, E> {
    private DCEL<V, E> dcel;
    private final Graph<V, E> sourceGraph;

    public PlanarTriangulationAlgorithm(DCEL<V, E> dcel) {
        this.dcel = dcel;
        this.sourceGraph = dcel.getGraph();
    }

    private void makeEachFaceACycle() {
        List<DCEL.Face<V, E>> faces = dcel.getFaces();
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
        dcel = EmbeddingToDCELConverter.buildDcelFromGraph(sourceGraph);
    }

    public DCEL<V, E> triangulate() {

        return null;
    }
}
