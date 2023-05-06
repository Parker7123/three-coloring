package org.example.algorithms.separator;

import java.util.Set;

public interface SeparatorFindingAlgorithm <V> {
    Set<V> getSparator();
    Set<V> getSubsetA();
    Set<V> getSubsetB();
}
