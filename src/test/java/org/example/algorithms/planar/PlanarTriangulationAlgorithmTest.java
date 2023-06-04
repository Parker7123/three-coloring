package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(embeddingWithFaces.getGraph().edgeSet()).hasSize(3 * 5 - 6);
        assertThat(embeddingWithFaces.getFaces()).hasSize(6);
        assertThat(embeddingWithFaces.getFaces())
                .allSatisfy(face -> assertThat(face.edges().size()).isEqualTo(3));
    }
}