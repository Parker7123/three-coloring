package org.example.algorithms.coloring;

import org.jgrapht.generate.GridGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class PlanarThreeColoringTest {

    @Test
    void bigGridGraphTest() {
        GridGraphGenerator<Integer, DefaultEdge> gridGraphGenerator = new GridGraphGenerator<>(30, 30);
        var graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(), false);
        gridGraphGenerator.generateGraph(graph, null);

        var planarThreeColoring = new PlanarThreeColoring<>(graph, false);

        long start = Instant.now().toEpochMilli();
        var coloring = planarThreeColoring.getColoring();
        long end = Instant.now().toEpochMilli();
        System.out.println(end - start);
        System.out.println(coloring);
    }

}