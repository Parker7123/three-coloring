package org.example;

import org.checkerframework.checker.units.qual.A;
import org.example.algorithms.coloring.PlanarThreeColoring;
import org.example.algorithms.separator.PlanarConnectedSeparatorFindingAlgorithm;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.Coloring;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.graph6.Graph6Sparse6Importer;
import org.jgrapht.util.SupplierUtil;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.StringReader;
import java.util.Optional;
import java.util.concurrent.Callable;

public class Main {

    @Command(name = "threeColoring", mixinStandardHelpOptions = true, version = "1.0",
            description = "Checks if given planar graph can be three colored and returns the coloring if possible")
    private static class ThreeColoringCommand implements Callable<File> {

        @ArgGroup(exclusive = true)
        private Args args;

        static class Args {
            @Option(names = {"-f"}, paramLabel = "File", description = "File with graph in graph6 format")
            private File file;

            @Parameters(arity = "0..1", description = "graph6 string")
            private String graphCode;
        }

        @Override
        public File call() throws Exception {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.createDefaultEdgeSupplier(), false);
            Graph6Sparse6Importer<Integer, DefaultEdge> importer = new Graph6Sparse6Importer<>();
            if (args.file != null) {
                importer.importGraph(graph, args.file);
            } else {
                importer.importGraph(graph, new StringReader(args.graphCode));
            }
            Coloring<Integer> threeColoring = new PlanarThreeColoring<>(graph).getColoring();
            if (threeColoring == null) {
                System.err.println("Three coloring is not possible on a given graph");
            } else {
                System.out.println(threeColoring);
            }
            return null;
        }
    }

    public static void main(String[] args) {
        new CommandLine(new ThreeColoringCommand()).execute(args);
    }
}