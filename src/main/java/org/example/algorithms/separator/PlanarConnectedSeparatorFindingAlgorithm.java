package org.example.algorithms.separator;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;

import java.util.Set;
import java.util.*;

public class PlanarConnectedSeparatorFindingAlgorithm<V, E> implements SeparatorFindingAlgorithm<V> {
    private final Graph<V, E> sourceGraph;
    private final int n;
    private Set<V> separator;
    private Set<V> subsetA;
    private Set<V> subsetB;

    public PlanarConnectedSeparatorFindingAlgorithm(Graph<V, E> sourceGraph) {
        this.sourceGraph = sourceGraph;
        this.n = sourceGraph.vertexSet().size();
    }
    @Override
    public Set<V> getSparator() {
        return separator;
    }

    @Override
    public Set<V> getSubsetA() {
        return subsetA;
    }

    @Override
    public Set<V> getSubsetB() {
        return subsetB;
    }

    public void runAlgorithm() {

        // 1 - Simple Stage
        Graph<V,E> ModifiedGraph = simpleStage();
        if(ModifiedGraph==null)return;
        
        // 2
        // triangulacja triangulate()

        // 3
        // całe szukanie cyklu
    }

    private Graph<V,E> simpleStage(){
        List<List<V>> treeLevels = createSpanningTreeLevelsUsingBFS(sourceGraph.vertexSet().stream().findFirst().orElse(null));
        Graph<V,E> spanningTree = createSpanningTreeUsingBFS(sourceGraph.vertexSet().stream().findFirst().orElse(null));
        int centerLevel = findTreeCenterOfGravityLevel(treeLevels);

        if(checkSingleLevelSeparatorSize(treeLevels.get(centerLevel))) {
            submitSingleLevelSeparator(treeLevels,centerLevel);
            return null;
        }

        int levelBelow = findLevelBelowCenter(treeLevels,centerLevel);
        int levelAbove = findLevelAboveCenter(treeLevels,centerLevel);

        if(checkTwoLevelsSeparatorSize(treeLevels,levelBelow,levelAbove)) {
            submitTwoLevelsSeparator(treeLevels,levelBelow,levelAbove);
            return null;
        }

        getModifiedGraphForComplexStage(spanningTree,treeLevels,levelBelow,levelAbove);
        return spanningTree;
    }

    private List<List<V>> createSpanningTreeLevelsUsingBFS(V startVertex) {

            List<List<V>> levels = new ArrayList<>();
            Queue<V> queue = new LinkedList<>();
            Set<V> visited = new HashSet<>();
    
            queue.add(startVertex);
            visited.add(startVertex);
    
            while (!queue.isEmpty()) {
                int size = queue.size();
                List<V> levelVertices = new ArrayList<>();
    
                for (int i = 0; i < size; i++) {
                    V vertex = queue.poll();
                    levelVertices.add(vertex);
    
                    for (E edge : sourceGraph.outgoingEdgesOf(vertex)) {
                        V neighbor = sourceGraph.getEdgeTarget(edge);
                        if (!visited.contains(neighbor)) {
                            queue.add(neighbor);
                            visited.add(neighbor);
                        }
                    }
                }
    
                levels.add(levelVertices);
            }
    
            return levels;
    }
    private Graph<V,E> createSpanningTreeUsingBFS(V startVertex) {

        Graph<V,E> spanningTree = new SimpleGraph<V,E>(sourceGraph.getVertexSupplier(),sourceGraph.getEdgeSupplier(),false);
        Queue<V> queue = new LinkedList<>();
        Set<V> visited = new HashSet<>();

        spanningTree.addVertex(startVertex);
        queue.add(startVertex);
        visited.add(startVertex);

        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                V vertex = queue.poll();

                for (E edge : sourceGraph.outgoingEdgesOf(vertex)) {
                    V neighbor = sourceGraph.getEdgeTarget(edge);
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                        spanningTree.addVertex(neighbor);
                        spanningTree.addEdge(vertex, neighbor);
                    }
                }
            }
        }

        return spanningTree;
    }
    private int findTreeCenterOfGravityLevel(List<List<V>> tree) {
        int maxLevel = tree.size() - 1;
        int cumulativeCount = 0;

        for (int i = 0; i <= maxLevel; i++) {
            List<V> levelVertices = tree.get(i);
            cumulativeCount += levelVertices.size();
            
            if (cumulativeCount >= n / 2) {
                return i;
            }
        }

        return -1; // Jeśli nie znaleziono odpowiedniego poziomu
    }
    private boolean checkSingleLevelSeparatorSize(List<V> separator){
        int separatorSize = separator.size();

        return false; // Do ogarnięcia wielkość separatora
    }
    private void submitSingleLevelSeparator(List<List<V>> treeLevels, int center){
        initializeSets();
        for(int i=0;i<center;i++) subsetA.addAll(treeLevels.get(i));
        separator.addAll(treeLevels.get(center));
        for(int i=center+1;i<treeLevels.size();i++) subsetB.addAll(treeLevels.get(i));
    }
    private void submitTwoLevelsSeparator(List<List<V>> treeLevels,int belowLevel,int aboveLevel){
        initializeSets();
        int sizeA = getVerticesCountBetweenLevels(treeLevels,0,belowLevel-1);
        int sizeB = getVerticesCountBetweenLevels(treeLevels,belowLevel+1,aboveLevel-1);
        int sizeC = getVerticesCountBetweenLevels(treeLevels,aboveLevel+1,treeLevels.size()-1);

        int maxSize = Math.max(Math.max(sizeA,sizeB),sizeC);
        Set<V> currentSet;

        currentSet = maxSize==sizeA? subsetA:subsetB;
        addVerticesBetweenLevelsToSubset(treeLevels,0,belowLevel-1,currentSet);

        currentSet = maxSize==sizeB? subsetA:subsetB;
        addVerticesBetweenLevelsToSubset(treeLevels,belowLevel+1,aboveLevel-1,currentSet);

        currentSet = maxSize==sizeC? subsetA:subsetB;
        addVerticesBetweenLevelsToSubset(treeLevels,aboveLevel+1,treeLevels.size()-1,currentSet);

        separator.addAll(treeLevels.get(belowLevel));
        separator.addAll(treeLevels.get(aboveLevel));
    }
    private void addVerticesBetweenLevelsToSubset(List<List<V>> treeLevels,int from,int to, Set<V> S){
        for(int i =from;i<=to;i++){
            S.addAll(treeLevels.get(i));
        }
    }
    private boolean checkTwoLevelsSeparatorSize(List<List<V>> tree,int belowLevel,int aboveLevel){
        int sizeA = getVerticesCountBetweenLevels(tree,0,belowLevel-1);
        int sizeB = getVerticesCountBetweenLevels(tree,belowLevel+1,aboveLevel-1);
        int sizeC = getVerticesCountBetweenLevels(tree,aboveLevel+1,tree.size()-1);

        int maxSize = Math.max(Math.max(sizeA,sizeB),sizeC);

        return maxSize<n*2/3;

    }
//    private int getMaxSubsetSize(List<List<V>> tree,int belowLevel,int aboveLevel){
//        int sizeA = getVerticesCountBetweenLevels(tree,0,belowLevel-1);
//        int sizeB = getVerticesCountBetweenLevels(tree,belowLevel+1,aboveLevel-1);
//        int sizeC = getVerticesCountBetweenLevels(tree,aboveLevel+1,tree.size()-1);
//
//        return Math.max(Math.max(sizeA,sizeB),sizeC);
//    }
    private int getVerticesCountBetweenLevels(List<List<V>> tree,int bottom,int top){
        int count = 0;
        for(int i=bottom;i<=top;i++){
            count += tree.get(i).size();
        }
        return count;
    }
    private int findLevelBelowCenter(List<List<V>> tree, int middle){
        int sqrtN = (int) Math.sqrt(n);

        for(int i = middle-1;i>=0;i--) {
            int levelSize = tree.get(i).size();
            int D = middle-i;
            if(2*(sqrtN-D)>=levelSize)return i;
        }
        return -1;
    }
    private int findLevelAboveCenter(List<List<V>> tree, int middle){
        int sqrtN = (int) Math.sqrt(n);

        for(int i = middle+1;i<tree.size();i++) {
            int levelSize = tree.get(i).size();
            int D = i-middle;
            if(2*(sqrtN-D)>=levelSize)return i;
        }
        return -1;
    }
    private void getModifiedGraphForComplexStage(Graph<V, E> tree,List<List<V>> treeLevels, int belowLevel,int aboveLevel){

        for(int i=aboveLevel;i<treeLevels.size();i++){
            for (V v:treeLevels.get(i)) {
                tree.removeVertex(v);
            }
        }
        V topVertex = treeLevels.get(0).get(0);

        for(int i=0;i<=treeLevels.size();i++){
            for (V v:treeLevels.get(i)) {
                tree.removeVertex(v);
            }
        }

        tree.addVertex(topVertex);

        for (V v:treeLevels.get(belowLevel+1)) {
            tree.addEdge(topVertex, v);
        }
    }
    private void initializeSets(){
        subsetA = new HashSet<>();
        subsetB = new HashSet<>();
        separator = new HashSet<>();
    }
    private Graph<V, E> triangulate() {
        // tutaj dodać algorytm triangulacji, najlepiej w nowej klasie
        return null;
    }
}
