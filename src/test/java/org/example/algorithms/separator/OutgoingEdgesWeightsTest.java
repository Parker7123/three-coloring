package org.example.algorithms.separator;

import org.jgrapht.Graph;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class OutgoingEdgesWeightsTest {

    @Test
    void shouldFindCycle() {
        var graph = createTreeGraph();
        var algorithm = new PlanarConnectedSeparatorFindingAlgorithm<>(graph);
        var embedding = new BoyerMyrvoldPlanarityInspector<>(graph).getEmbedding();
        var spanningTree = algorithm.createSpanningTreeUsingBFS(8);
        graph.addEdge(3, 4);
        int v1 = 3;
        int v2 = 4;

        int commonAncestor = algorithm.getLowestCommonAncestor(3, 4);
        assertThat(commonAncestor).isEqualTo(5);

        List<Integer> cycle = algorithm.getCycle(v1,v2,commonAncestor);
        assertThat(cycle).containsExactly(3, 2, 1, 0, 5, 4);
    }

    @Test
    void shouldComputeOutgoingEdgesWeights() {
        var graph = createTreeGraph();
        var algorithm = new PlanarConnectedSeparatorFindingAlgorithm<>(graph);
        var embedding = new BoyerMyrvoldPlanarityInspector<>(graph).getEmbedding();
        var spanningTree = algorithm.createSpanningTreeUsingBFS(8);
        graph.addEdge(3, 4);
        int v1 = 3;
        int v2 = 4;

        int commonAncestor = algorithm.getLowestCommonAncestor(3, 4);
        List<Integer> cycle = algorithm.getCycle(v1,v2,commonAncestor);

        var weights = algorithm.computeOutgoingEdgeWeights(graph, v1, v2, commonAncestor, cycle);
        System.out.println(weights);
    }


    @Test
    void shouldCreateSpanningTree() {
        var expectedParentNodes = Map.of(0, 5,
                1 ,0,
                2 ,1,
                3 ,2,
                4 , 5,
                5 , 6,
                6 , 8,
                7 , 6,
                9 , 1,
                10 , 2);
        var graph = createTreeGraph();
        var algorithm = new PlanarConnectedSeparatorFindingAlgorithm<>(graph);
        var spanningTree = algorithm.createSpanningTreeUsingBFS(8);
        var parentNodes = algorithm.getSpanningTreeParentNodes();

        assertThat(parentNodes).hasSize(graph.vertexSet().size());
        assertThat(parentNodes).containsAllEntriesOf(expectedParentNodes);
        assertThat(parentNodes.get(8)).isNull();
    }

    /**
     * resources/testGraph.png without 3 - 4 edge
     * @return
     */
    private static Graph<Integer, DefaultEdge> createTreeGraph() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        IntStream.range(0, 11).forEach(graph::addVertex);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
//        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(5, 0);

        // 8 is root
        graph.addEdge(6, 5);
        graph.addEdge(8, 6);
        graph.addEdge(6, 7);

        graph.addEdge(1, 9);
        graph.addEdge(2, 10);

        return graph;
    }


}
