package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingToEmbeddingWithFacesConverterTest {

    @Test
    void shouldConvertK4() {
        Graph<Integer, DefaultEdge> completeGraph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(), false);
        CompleteGraphGenerator<Integer, DefaultEdge> completeGenerator = new CompleteGraphGenerator<>(4);
        completeGenerator.generateGraph(completeGraph);

        var embedding = new BoyerMyrvoldPlanarityInspector<>(completeGraph).getEmbedding();
        var dcel = EmbeddingToEmbeddingWithFacesConverter.convert(embedding);

        assertThat(dcel.getFaces().size()).isEqualTo(4);
    }

    @Test
    void shouldConvertCycle() {
        Graph<Integer, DefaultEdge> cycle = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(), false);
        var cycleGenerator = new RingGraphGenerator<Integer, DefaultEdge>(10);
        cycleGenerator.generateGraph(cycle);

        var embedding = new BoyerMyrvoldPlanarityInspector<>(cycle).getEmbedding();
        var embeddingWithFaces = EmbeddingToEmbeddingWithFacesConverter.convert(embedding);

        assertThat(embeddingWithFaces.getFaces().size()).isEqualTo(2);
        assertThat(embeddingWithFaces.getFaces().get(0).edges().size()).isEqualTo(10);
        assertThat(embeddingWithFaces.getFaces().get(1).edges().size()).isEqualTo(10);
    }
}