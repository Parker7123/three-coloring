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
        var cycleGenerator = new RingGraphGenerator<Integer, DefaultEdge>(10);
        cycleGenerator.generateGraph(cycle);

        var embedding = new BoyerMyrvoldPlanarityInspector<>(cycle).getEmbedding();
        var embeddingWithFaces = new PlanarTriangulationAlgorithm<>(embedding).triangulate();

        assert embeddingWithFaces.getFaces().size() == 8;
        for (var face : embeddingWithFaces.getFaces()) {
            assert face.edges().size() == 3;
        }
    }
}