package org.example;

import org.example.algorithms.coloring.PlanarThreeColoring;
import org.jgrapht.Graph;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;

public class Main {
    private static int SIZE = 4;

    public static void main(String[] args) {
        // TODO: Use https://picocli.info to create console app that loads graph from file
        System.out.println("Hello world!");
        Graph<String, DefaultEdge> stringGraph = createStringGraph();
        System.out.println("-- toString output");
        System.out.println(stringGraph);
        System.out.println();

        // example cicle
        var planarThreeColoring = new PlanarThreeColoring<>(stringGraph).getColoring();
        System.out.println(planarThreeColoring);

        Graph<Integer, DefaultEdge> completeGraph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(), false);
        CompleteGraphGenerator<Integer, DefaultEdge> completeGenerator = new CompleteGraphGenerator<>(SIZE);
        completeGenerator.generateGraph(completeGraph);

        // example k4
        var integerDefaultEdgePlanarThreeColoring = new PlanarThreeColoring<>(completeGraph).getColoring();
        System.out.println(integerDefaultEdgePlanarThreeColoring);
    }

    private static Graph<String, DefaultEdge> createStringGraph()
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        String v1 = "v1";
        String v2 = "v2";
        String v3 = "v3";
        String v4 = "v4";

        // add the vertices
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        // add edges to create a circuit
        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v3, v4);
        g.addEdge(v4, v1);

        return g;
    }
}