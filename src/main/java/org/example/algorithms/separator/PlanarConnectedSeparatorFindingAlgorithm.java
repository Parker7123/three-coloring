package org.example.algorithms.separator;

import org.example.algorithms.planar.EmbeddingWithFaces;
import org.example.algorithms.planar.PlanarTriangulationAlgorithm;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.PlanarityTestingAlgorithm;
import org.jgrapht.alg.planar.BoyerMyrvoldPlanarityInspector;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.SimpleGraph;
import org.jheaps.annotations.VisibleForTesting;

import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.algorithms.coloring.ThreeColoringUtils.subgraph;

public class PlanarConnectedSeparatorFindingAlgorithm<V, E> implements SeparatorFindingAlgorithm<V> {
    private final Graph<V, E> sourceGraph;
    private final int n;
    private Set<V> separator;
    private Set<V> subsetA;
    private Set<V> subsetB;
    private List<List<V>> spanningTreeLevels;
    private int level0;
    private int level2;

    private Map<V,V> spanningTreeParentNodes;

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

    public Map<V, V> getSpanningTreeParentNodes() {
        return spanningTreeParentNodes;
    }

    public void runAlgorithm() {

        // 1 - Simple Stage
        Graph<V,E> modifiedGraph = simpleStage();
        if(modifiedGraph==null) return;
        Graph<V,E> spanningTree = subgraph(modifiedGraph,modifiedGraph.vertexSet());


        // 2
        var embedding = new BoyerMyrvoldPlanarityInspector<>(modifiedGraph).getEmbedding();
        var triangulatedFaces = new PlanarTriangulationAlgorithm<>(embedding).triangulate();

        // 3
        // całe szukanie cyklu
        complexStage(modifiedGraph, spanningTree, embedding, triangulatedFaces);
    }

    private void complexStage(Graph<V,E> G, Graph<V,E> spanningTree,
                              PlanarityTestingAlgorithm.Embedding embedding, EmbeddingWithFaces triangulatedFaces){
        E cycleEdge = pickNontreeEdge(spanningTree,G);
        V v1 = sourceGraph.getEdgeSource(cycleEdge);
        V v2 = sourceGraph.getEdgeTarget(cycleEdge);
        V commonAncestor = getLowestCommonAncestor(v1,v2);
        Map<E,Integer>  outgoingEdgesWeights = new HashMap<>();
        getEdgesWeights(spanningTree,v1,commonAncestor, outgoingEdgesWeights);
        getEdgesWeights(spanningTree,v2,commonAncestor, outgoingEdgesWeights);

        List<V> cycle = getCycle(v1,v2,commonAncestor);
        getEdgesWeightsForCommonAncestor(spanningTree,commonAncestor,cycle,outgoingEdgesWeights);

        Pair<Integer,Integer> cycleValues = SumCycleSides(spanningTree,cycle,embedding,outgoingEdgesWeights);
        int area, cycleValue;
        if(cycleValues.getFirst() > cycleValues.getSecond()){
            area = 1;
            cycleValue = cycleValues.getFirst();
        }
        else{
            area = 2;
            cycleValue = cycleValues.getSecond();
        }

        try {
            findSufficientCycle(G, spanningTree, cycle, area, cycleValue, cycleEdge,
                    embedding, triangulatedFaces, outgoingEdgesWeights);
        }
        catch (Exception ex){

        }
        initializeSets();
        CountSeparatorAndSubsets(cycle,spanningTree,embedding,area);

    }

    private void CountSeparatorAndSubsets(List<V> cycle,Graph<V,E> spanningTree,PlanarityTestingAlgorithm.Embedding embedding, int area){

        for(int i =0;i<cycle.size();i++){
            V vertex = cycle.get(i);

            //int nextIndex = Math.floorMod(i+1,cycle.size());
            //int prevIndex = Math.floorMod(i-1,cycle.size());

            for(E e : spanningTree.outgoingEdgesOf(vertex)){

                V v2 = Graphs.getOppositeVertex(spanningTree,e,vertex);
                //if(v2.equals(cycle.get(nextIndex))||v2.equals(cycle.get(prevIndex)))continue;

                if(checkIfEdgeInArea(spanningTree,cycle,area,embedding,e)){
                    addToSetBSubtreeVerticesRecursive(spanningTree,v2,vertex);
                }
            }
        }

        if(cycle.contains(spanningTreeLevels.get(0).get(0)))
            cycle.remove(spanningTreeLevels.get(0).get(0));
        separator.addAll(cycle);
        separator.addAll(spanningTreeLevels.get(level0));
        separator.addAll(spanningTreeLevels.get(level2));

//        for(int i=0;i<this.level0;i++){
//            subsetA.addAll(spanningTreeLevels.get(i));
//        }
//        for(int i=this.level2+1;i<this.spanningTreeLevels.size();i++){
//            subsetA.addAll(spanningTreeLevels.get(i));
//        }
        for(int i=0;i<spanningTreeLevels.size();i++){
            subsetA.addAll(spanningTreeLevels.get(i));
        }
        for (V v:this.subsetB) {
            subsetA.remove(v);
        }
        for (V v:this.separator) {
            subsetA.remove(v);
        }
    }

    private List<V> findSufficientCycle(Graph<V,E> G, Graph<V,E> spanningTree, List<V> cycle,
                                     int area, int cycleValue, E cycleEdge,
                                     PlanarityTestingAlgorithm.Embedding embedding,
                                     EmbeddingWithFaces<V,E> triangulatedFaces,
                                     Map<E,Integer> outgoingEdgesWeights) throws Exception {
        int currentCycleValue = cycleValue;
        E currentCycleEdge = cycleEdge;
        while(currentCycleValue > 2/3*n){
            List<E> edgesInArea = findTriangleEdgesInArea(G,cycle,area,currentCycleEdge,embedding,triangulatedFaces);
            V vertexY = commonVertex(G, edgesInArea.get(0), edgesInArea.get(1));

            //TODO Find a faster way to check if edge is in the spanning tree
            var edgesInAreaFromSpanningTree = edgesInArea.stream().
                    filter(e-> spanningTree.edgeSet().contains(e)).collect(Collectors.toList());

            switch (edgesInAreaFromSpanningTree.size()){
                case 0:
                    findClosestVertexOnCycleAndUpdateWeights(spanningTree, cycle, vertexY);
                    V vertexX = cycle.get(0);
                    V vertexZ = cycle.get(cycle.size()-1);
                    V commonAncestorXY = getLowestCommonAncestor(vertexY, vertexX);
                    V commonAncestorYZ = getLowestCommonAncestor(vertexY, vertexZ);

                    var cycle1 = getCycle(vertexX, vertexY, commonAncestorXY);
                    var cycle2 = getCycle(vertexY, vertexZ, commonAncestorYZ);
                    int cycle1Value, cycle2Value;
                    var cycle1Values = SumCycleSides(spanningTree, cycle1, embedding, outgoingEdgesWeights);
                    // TODO You don't have to count cycle2Values manually
                    var cycle2Values = SumCycleSides(spanningTree, cycle2, embedding, outgoingEdgesWeights);
                    if(area==1){
                        cycle1Value = cycle1Values.getFirst();
                        cycle2Value = cycle2Values.getFirst();
                    }
                    else{
                        cycle1Value = cycle1Values.getSecond();
                        cycle2Value = cycle2Values.getSecond();
                    }
                    if(cycle1Value>cycle2Value){
                        cycle = cycle1;
                        currentCycleValue = cycle1Value;
                        currentCycleEdge = G.getEdge(vertexX,vertexY);
                    }
                    else{
                        cycle = cycle2;
                        currentCycleValue = cycle2Value;
                        currentCycleEdge = G.getEdge(vertexY, vertexZ);
                    }
                    break;
                case 1:
                    //TODO (?) Count subtrees for y (Figure 4.3b))
                    V vertex1InArea = G.getEdgeSource(edgesInAreaFromSpanningTree.get(0));
                    V vertex2InArea = G.getEdgeTarget(edgesInAreaFromSpanningTree.get(0));
                    V vertexInArea = vertex1InArea; // default assignment
                    for(int i=0; i<cycle.size(); i++){
                        V vertexFromCycle = cycle.get(i);
                        if(vertexFromCycle.equals(vertex1InArea)){
                            cycle.add(i+1,vertex2InArea);
                            vertexInArea = vertex2InArea;
                            break;
                        }
                        else if(vertexFromCycle.equals(vertex2InArea)){
                            cycle.add(i+1,vertex1InArea);
                            vertexInArea = vertex1InArea;
                            break;
                        }
                    }
                    currentCycleValue--;
                    getEdgesWeights(spanningTree, vertexInArea,
                            spanningTreeParentNodes.get(vertexInArea), outgoingEdgesWeights);
                    break;
                default:
                    throw new Exception("Error in findSufficientCycle: More than one edge lies inside the cycle");
            }
        }
        return cycle;
    }

    private void findClosestVertexOnCycleAndUpdateWeights(Graph<V,E> spanningTree, List<V> cycle, V vertex){
        V parent = spanningTreeParentNodes.get(vertex);

        //TODO Better way to check if vertex is on cycle
        do {
            for(E edge: spanningTree.outgoingEdgesOf(vertex)) {
                countSubtreeVerticesRecursive(spanningTree,
                        Graphs.getOppositeVertex(spanningTree, edge, vertex), vertex);
            }
            vertex = parent;
            parent = spanningTreeParentNodes.get(parent);
        }while(!cycle.contains(vertex));
    }
    private V commonVertex(Graph<V,E> G, E e1, E e2){
        V v1 = G.getEdgeSource(e1);
        V v2 = G.getEdgeTarget(e1);
        if(v1.equals(G.getEdgeSource(e2)) || v1.equals(G.getEdgeTarget(e2))) {
            return v1;
        }
        else if (v2.equals(G.getEdgeSource(e2)) || v2.equals(G.getEdgeTarget(e2))) {
            return v2;
        }
        else {
            return null;
        }
    }

    private List<E> findTriangleEdgesInArea(Graph<V,E> G, List<V> cycle, int area, E cycleEdge,
                               PlanarityTestingAlgorithm.Embedding embedding, EmbeddingWithFaces<V,E> triangulatedFaces){
        var triangles = triangulatedFaces.getFaces().stream()
                .filter(face -> face.edges().contains(cycleEdge)).collect(Collectors.toList());
        V cycleEdgeSource = G.getEdgeSource(cycleEdge);
        V cycleEdgeTarget = G.getEdgeTarget(cycleEdge);

        List<E> edgesInArea = new ArrayList<>();
        for(var face:triangles){
            for(var edge:face.edges()){
                if(checkIfEdgeInArea(G,cycle,area,embedding,edge.edge())){
                    edgesInArea.add(edge.edge());
                }
            }
        }
        return edgesInArea;
    }

    private boolean checkIfEdgeInArea(Graph<V,E> G, List<V> cycle, int area,
                                      PlanarityTestingAlgorithm.Embedding embedding, E edge){
        V vertexOnCycle, vertexNotOnCycle;
        if(cycle.contains(G.getEdgeSource(edge)) && cycle.contains(G.getEdgeTarget(edge))) {
            return false;
        }
        else if(cycle.contains(G.getEdgeSource(edge))){
            vertexOnCycle = G.getEdgeSource(edge);
            vertexNotOnCycle = G.getEdgeTarget(edge);
        }
        else if (cycle.contains(G.getEdgeTarget(edge))){
            vertexNotOnCycle = G.getEdgeSource(edge);
            vertexOnCycle = G.getEdgeTarget(edge);
        }
        else{
            return false; // TODO What if cycle has no common vertex with edge
        }

        int vertexOnCycleIndex = cycle.indexOf(vertexOnCycle);
        int nextIndex = Math.floorMod(vertexOnCycleIndex+1,cycle.size());
        V nextV  = cycle.get(nextIndex);
        int prevIndex = Math.floorMod(vertexOnCycleIndex-1,cycle.size());
        V prevV  = cycle.get(prevIndex);

        List<E> outEdges = embedding.getEdgesAround(vertexOnCycle);
        int prevEdgeIndex=0, nextEdgeIndex=0;
        for (int j=0;j<outEdges.size();j++){
            V v2 = G.getEdgeTarget(outEdges.get(vertexOnCycleIndex));
            if(v2.equals(prevV))prevEdgeIndex=j;
            if(v2.equals(nextV))nextEdgeIndex=j;
        }

        int currentArea = 0;
        for(int j=0;j<outEdges.size();j++) {
            if(j==prevEdgeIndex) {
                currentArea = 1;
                continue;
            }
            else if(j==nextEdgeIndex) {
                currentArea = 2;
                continue;
            }
            if(outEdges.get(j).equals(edge)) {
                return currentArea == area;
            }
        }
        for(int j=0;j<outEdges.size();j++){
            if(outEdges.get(j).equals(edge)) {
                return currentArea == area;
            }
        }
        return false;
    }

    @VisibleForTesting
    Pair<Integer, Integer> SumCycleSides(Graph<V,E> G, List<V> cycle,
                               PlanarityTestingAlgorithm.Embedding embedding, Map<E,Integer> outgoingEdgesWeights){
        int count1=0,count2=0;

        for(int i=0;i<cycle.size();i++){
            int nextIndex = Math.floorMod(i+1,cycle.size());
            V nextV  = cycle.get(nextIndex);
            int prevIndex = Math.floorMod(i-1,cycle.size());
            V prevV  = cycle.get(prevIndex);
            V v = cycle.get(i);

            List<E> outEdges = embedding.getEdgesAround(cycle.get(i));
            int prevEdgeIndex=0, nextEdgeIndex=0;
            for (int j=0;j<outEdges.size();j++){
                V v2 = Graphs.getOppositeVertex(G, outEdges.get(j), v);
                if(v2.equals(prevV))prevEdgeIndex=j;
                if(v2.equals(nextV))nextEdgeIndex=j;
            }

            int currentArea = 0;

            for(int j=0;j<outEdges.size();j++) {

                if(j==prevEdgeIndex) {
                    currentArea = 1;
                    continue;
                }
                else if(j==nextEdgeIndex) {
                    currentArea = 2;
                    continue;
                }

                if(currentArea ==1) {
                    count1 += outgoingEdgesWeights.get(outEdges.get(j));
                }
                else if(currentArea ==2) {
                    count2 += outgoingEdgesWeights.get(outEdges.get(j));
                }
            }

            for(int j=0;j<outEdges.size();j++){
                if(j==prevEdgeIndex || j==nextEdgeIndex) {
                    break;
                }
                if(currentArea ==1) {
                    count1 += outgoingEdgesWeights.get(outEdges.get(j));
                }
                else if(currentArea ==2) {
                    count2 += outgoingEdgesWeights.get(outEdges.get(j));
                }
            }
        }

        return new Pair<>(count1, count2);
    }

    @VisibleForTesting
    Map<E, Integer> computeOutgoingEdgeWeights(Graph<V,E> G, V v1, V v2, V commonAncestor, List<V> cycle) {
        Map<E,Integer>  outgoingEdgesWeights = new HashMap<>();
        getEdgesWeights(G,v1,commonAncestor, outgoingEdgesWeights);
        getEdgesWeights(G,v2,commonAncestor, outgoingEdgesWeights);
        getEdgesWeightsForCommonAncestor(G,commonAncestor,cycle,outgoingEdgesWeights);

        return outgoingEdgesWeights;
    }

    private void getEdgesWeightsForCommonAncestor(Graph<V,E> G, V v1, List<V> cycle,Map<E,Integer> edgesWeights){

        int v1Index = cycle.indexOf(v1);

        int vPrevIndex = v1Index-1;
        if(vPrevIndex<0)vPrevIndex = cycle.size()-1;

        int vNextIndex = v1Index+1;
        if(vNextIndex>cycle.size()-1)vNextIndex = 0;

        V vNext = cycle.get(vNextIndex);
        V vPrev = cycle.get(vPrevIndex);

        for(E e : G.outgoingEdgesOf(v1)){
            V v2 = Graphs.getOppositeVertex(G, e, v1);
            if(v2.equals(vNext)||v2.equals(vPrev)) continue;
            int count = countSubtreeVerticesRecursive(G,v2,v1);
            edgesWeights.put(e, count);
        }
    }

    private void getEdgesWeights(Graph<V,E> G,V v1, V commonAncestor,Map<E,Integer> weights){
        V prev = null;

        while (!v1.equals(commonAncestor)) {
            V parent = spanningTreeParentNodes.get(v1);

            for (E e : G.outgoingEdgesOf(v1)) {

                V outgoingVertex = Graphs.getOppositeVertex(G, e, v1);
                if (outgoingVertex.equals(parent) || outgoingVertex.equals(prev)) continue;

                int count = countSubtreeVerticesRecursive(G, outgoingVertex, v1);
                weights.put(e, count);
            }

            prev = v1;
            v1 = parent;
        }

    }

    private void addToSetBSubtreeVerticesRecursive(Graph<V, E> spanningTree, V vertex, V parent) {
        this.subsetB.add(vertex);
        for (E edge : spanningTree.outgoingEdgesOf(vertex)) {
            V v = spanningTree.getEdgeTarget(edge);
            if(v.equals(parent))break;
            addToSetBSubtreeVerticesRecursive(spanningTree, v,vertex);
        }
    }

    private int countSubtreeVerticesRecursive(Graph<V, E> G, V vertex, V parent) {
        int count = 1;
        for (E edge : G.outgoingEdgesOf(vertex)) {
            V v = Graphs.getOppositeVertex(G,edge, vertex);
            if(v.equals(parent)) {
                continue;
            }
            count += countSubtreeVerticesRecursive(G, v,vertex);
        }

        return count;
    }

    @VisibleForTesting
    List<V> getCycle(V v1, V v2, V commonAncestor){
        List<V> path1 = getUpPath(v1,commonAncestor);
        path1.add(commonAncestor);

        List<V> path2 = getUpPath(v2,commonAncestor);
        Collections.reverse(path2);
        path1.addAll(path2);

        return path1;
    }
    private List<V> getUpPath(V v1, V ancestor){
        List<V> path = new ArrayList<>();
        while(v1!=ancestor){
            path.add(v1);
            v1 = spanningTreeParentNodes.get(v1);
        }
        return path;
    }

    @VisibleForTesting
    V getLowestCommonAncestor(V v1, V v2){
        List<V> path1 = getPathToRoot(v1);
        do{
            if(path1.contains(v2))return v2;
            v2 = spanningTreeParentNodes.get(v2);
        }while(v2!=null);
        return null;
    }

    private List<V> getPathToRoot(V vertex){
        List<V> path= new ArrayList<>();
        path.add(vertex);
        while(true){
            vertex = spanningTreeParentNodes.get(vertex);
            if(vertex==null)break;
            path.add(vertex);
        }
        return path;
    }

    private E pickNontreeEdge(Graph<V,E> spanningTree,Graph<V,E> G){
        for (E edge : G.edgeSet()) { //problem, dać triangulację a nie sourceGraph
            if (!spanningTree.containsEdge(edge)) {
                return edge;
            }
        }
        return null;
    }

    private Graph<V,E> simpleStage()    {
        V startVertex = sourceGraph.vertexSet().stream().findFirst().orElse(null);
        List<List<V>> treeLevels = spanningTreeLevels = createSpanningTreeLevelsUsingBFS(startVertex);
        Graph<V,E> spanningTree = createSpanningTreeUsingBFS(sourceGraph.vertexSet().stream().findFirst().orElse(null));
        int centerLevel = findTreeCenterOfGravityLevel(treeLevels);

        if(checkSingleLevelSeparatorSize(treeLevels.get(centerLevel))) {
//            submitSingleLevelSeparator(treeLevels,centerLevel);
//            return null;
            //Todo
        }

        int levelBelow = findLevelBelowCenter(treeLevels,centerLevel);
        int levelAbove = findLevelAboveCenter(treeLevels,centerLevel);

        if(checkTwoLevelsSeparatorSize(treeLevels,levelBelow,levelAbove)) {
//            submitTwoLevelsSeparator(treeLevels,levelBelow,levelAbove);
//            return null;
            //Todo
        }

        modifyGraphForComplexStage(spanningTree,treeLevels,levelBelow,levelAbove);
        getAncestors(startVertex,spanningTree);
        this.level0 = levelBelow;
        this.level2 = levelAbove;
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
                        V neighbor = Graphs.getOppositeVertex(sourceGraph,edge,vertex);
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

    @VisibleForTesting
    Graph<V,E> createSpanningTreeUsingBFS(V startVertex) {

        Graph<V,E> spanningTree = new SimpleGraph<V,E>(sourceGraph.getVertexSupplier(),sourceGraph.getEdgeSupplier(),false);
        Queue<V> queue = new LinkedList<>();
        Set<V> visited = new HashSet<>();
        Map<V,V> parentNodes = new HashMap<>();

        parentNodes.put(startVertex,null);
        spanningTree.addVertex(startVertex);
        queue.add(startVertex);
        visited.add(startVertex);

        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                V vertex = queue.poll();

                for (E edge : sourceGraph.outgoingEdgesOf(vertex)) {
                    V neighbor = Graphs.getOppositeVertex(sourceGraph, edge, vertex);
                    if (!visited.contains(neighbor)) {
                        parentNodes.put(neighbor,vertex);
                        queue.add(neighbor);
                        visited.add(neighbor);
                        spanningTree.addVertex(neighbor);
                        spanningTree.addEdge(sourceGraph.getEdgeSource(edge), sourceGraph.getEdgeTarget(edge), edge);
                    }
                }
                visited.add(vertex);
            }
        }
        spanningTreeParentNodes = parentNodes;
        return spanningTree;
    }
    private void getAncestors(V startVertex, Graph<V,E> spanningTree) {

        Queue<V> queue = new LinkedList<>();
        Set<V> visited = new HashSet<>();
        Map<V,V> parentNodes = new HashMap<>();

        parentNodes.put(startVertex,null);
        queue.add(startVertex);
        visited.add(startVertex);

        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                V vertex = queue.poll();

                for (E edge : spanningTree.outgoingEdgesOf(vertex)) {
                    V neighbor = Graphs.getOppositeVertex(spanningTree, edge, vertex);
                    if (!visited.contains(neighbor)) {
                        parentNodes.put(neighbor,vertex);
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }
        spanningTreeParentNodes = parentNodes;
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
    private void modifyGraphForComplexStage(Graph<V, E> spanningTree,List<List<V>> treeLevels, int l0,int l2){

        for(int i=l2;i<treeLevels.size();i++){
            for (V v:treeLevels.get(i)) {
                spanningTree.removeVertex(v);
            }
        }
        V topVertex = treeLevels.get(0).get(0);

        for(int i=1;i<=l0;i++){
            for (V v:treeLevels.get(i)) {
                spanningTree.removeVertex(v);
            }
        }

        for (V v:treeLevels.get(l0+1)) {
            spanningTree.addEdge(topVertex, v);
        }
    }
    private void initializeSets(){
        subsetA = new HashSet<>();
        subsetB = new HashSet<>();
        separator = new HashSet<>();
    }
}
