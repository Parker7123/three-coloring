package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingToDCELConverterTest {

    @Test
    void shouldConvertK4() {
        Graph<Integer, DefaultEdge> completeGraph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(), false);
        CompleteGraphGenerator<Integer, DefaultEdge> completeGenerator = new CompleteGraphGenerator<>(4);
        completeGenerator.generateGraph(completeGraph);

        var embedding = new BoyerMyrvoldPlanarityInspector<>(completeGraph).getEmbedding();
        var dcel = EmbeddingToDCELConverter.convert(embedding);

        assert dcel.getFaces().size() == 4;
    }

    @Test
    void shouldConvertCycle() {
        Graph<Integer, DefaultEdge> cycle = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(), false);
        var cycleGenerator = new RingGraphGenerator<Integer, DefaultEdge>(10);
        cycleGenerator.generateGraph(cycle);

        var embedding = new BoyerMyrvoldPlanarityInspector<>(cycle).getEmbedding();
        var dcel = EmbeddingToDCELConverter.convert(embedding);

        assert dcel.getFaces().size() == 2;
        assert dcel.getFaces().get(0).edges().size() == 10;
        assert dcel.getFaces().get(1).edges().size() == 10;
    }
}