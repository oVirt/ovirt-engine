package org.ovirt.engine.core.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

class ListUtilsTest {
    @Test
    public void testRankSimple() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<Integer> ranks = ListUtils.rankSorted(list, Comparator.naturalOrder());

        assertThat(ranks).containsExactly(0, 1, 2, 3, 4, 5);
    }

    @Test
    public void testRankWithDuplicates() {
        List<Integer> list = Arrays.asList(6, 6, 5, 5, 2, 2, 2);
        List<Integer> ranks = ListUtils.rankSorted(list, Comparator.reverseOrder());

        assertThat(ranks).containsExactly(0, 0, 2, 2, 4, 4, 4);
    }
}
