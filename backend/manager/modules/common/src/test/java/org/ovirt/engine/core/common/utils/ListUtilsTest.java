package org.ovirt.engine.core.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Test
    public void testListComparatorBasic() {
        List<List<Integer>> list = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(2, 2, 3),
                Arrays.asList(1, 1, 2),
                Arrays.asList(2, 2, 2),
                Arrays.asList(2, 3),
                Arrays.asList(2, 2)
        );

        list.sort(ListUtils.lexicographicListComparator());

        assertThat(list).containsExactly(
                Arrays.asList(1, 1, 2),
                Arrays.asList(1, 2, 3),
                Arrays.asList(2, 2),
                Arrays.asList(2, 2, 2),
                Arrays.asList(2, 2, 3),
                Arrays.asList(2, 3)
        );
    }

    @Test
    public void testListComparatorMixedTypes() {
        List<Integer> list1 = Arrays.asList(1, 1, 2);
        ArrayList<Integer> list2 = new ArrayList<>(Arrays.asList(2, 2, 2));

        int res = ListUtils.<Integer>lexicographicListComparator().compare(list1, list2);
        assertThat(res).isLessThan(0);
    }

    @Test
    public void testListComparatorWithInheritance() {
        List<Base> list1 = Stream.of(1, 2, 3).map(Base::new).collect(Collectors.toList());
        List<Derived1> list2 = Stream.of(2, 2, 2).map(Derived1::new).collect(Collectors.toList());
        List<Derived2> list3 = Stream.of(1, 1, 1).map(Derived2::new).collect(Collectors.toList());

        int test1 = ListUtils.lexicographicListComparator(Comparator.comparing(Base::getValue)).compare(list1, list2);
        int test2 = ListUtils.lexicographicListComparator(Comparator.comparing(Base::getValue)).compare(list2, list3);

        assertThat(test1).isLessThan(0);
        assertThat(test2).isGreaterThan(0);
    }

    private static class Base {
        private int value;

        public Base(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static class Derived1 extends Base {
        public Derived1(int value) {
            super(value);
        }
    }

    private static class Derived2 extends Base {
        public Derived2(int value) {
            super(value);
        }
    }
}
