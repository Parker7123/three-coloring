package org.example.algorithms.planar;

import org.jgrapht.Graph;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingToDCELConverterTest {

    @Test
    void convert() {
        Graph<Integer, DefaultEdge> completeGraph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(), false);
        CompleteGraphGenerator<Integer, DefaultEdge> completeGenerator = new CompleteGraphGenerator<>(4);
        completeGenerator.generateGraph(completeGraph);

        var embedding = new BoyerMyrvoldPlanarityInspector<>(completeGraph).getEmbedding();
        var dcel = EmbeddingToDCELConverter.convert(embedding);

        assert dcel.getFaces().size() == 4;
    }
}