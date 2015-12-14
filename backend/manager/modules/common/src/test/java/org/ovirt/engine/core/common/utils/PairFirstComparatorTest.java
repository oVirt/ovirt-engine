package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class PairFirstComparatorTest {

    private Pair<Integer, Integer> pair1 = new Pair<>(200, null);
    private Pair<Integer, Integer> pair2 = new Pair<>(50, null);
    private Pair<Integer, Integer> pair3 = new Pair<>(100, null);

    @Test
    public void testComparator() {
        List<Pair<Integer, Integer>> orig = new LinkedList<>(Arrays.asList(pair1, pair2, pair3));
        List<Pair<Integer, Integer>> expected = new LinkedList<>(Arrays.asList(pair1, pair3, pair2));

        Collections.sort(orig, new PairFirstComparator<Integer, Integer>(Collections.<Integer> reverseOrder()));
        assertEquals(expected, orig);
    }
}

