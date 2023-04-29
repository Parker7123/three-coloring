package org.example;

import org.jgrapht.Graph;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Graph<String, DefaultEdge> stringGraph = createStringGraph();
        System.out.println("-- toString output");
        System.out.println(stringGraph.toString());
        System.out.println();

        var spanningTreeAlgorithm = new KruskalMinimumSpanningTree<>(stringGraph);
        System.out.println(spanningTreeAlgorithm.getSpanningTree());

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