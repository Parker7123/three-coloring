package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanarTriangulationAlgorithmTest {

    @Test
    void shouldTriangulateCycle() {
        Graph<Integer, DefaultEdge> cycle = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(), false);
        var cycleGenerator = new RingGraphGenerator<Integer, DefaultEdge>(5);
        cycleGenerator.generateGraph(cycle);

        var embedding = new BoyerMyrvoldPlanarityInspector<>(cycle).getEmbedding();
        var embeddingWithFaces = new PlanarTriangulationAlgorithm<>(embedding).triangulate();
        System.out.println(embeddingWithFaces.getGraph().edgeSet().size());
        assert embeddingWithFaces.getGraph().edgeSet().size() == 3 * 5 - 6;
        assert embeddingWithFaces.getFaces().size() == 6;
        for (var face : embeddingWithFaces.getFaces()) {
            assert face.edges().size() == 3;
        }
    }
}