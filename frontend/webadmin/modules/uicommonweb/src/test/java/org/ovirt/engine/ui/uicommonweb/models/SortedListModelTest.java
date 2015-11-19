package org.ovirt.engine.ui.uicommonweb.models;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetup;

public class SortedListModelTest {

    @ClassRule
    public static UiCommonSetup setup = new UiCommonSetup();

    static class TestItem {

        private final int value;
        private final int salt;

        TestItem(int value, int salt) {
            this.value = value;
            this.salt = salt;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    value,
                    salt
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TestItem)) {
                return false;
            }
            TestItem other = (TestItem) obj;
            return salt == other.salt
                    && value == other.value;
        }

    }

    static final TestItem ITEM_1_1 = new TestItem(1, 1);
    static final TestItem ITEM_1_2 = new TestItem(1, 2);
    static final TestItem ITEM_2_1 = new TestItem(2, 1);
    static final TestItem ITEM_2_2 = new TestItem(2, 2);

    SortedListModel<TestItem> testedModel;

    Comparator<TestItem> makeValueComparator() {
        return new Comparator<TestItem>() {
            @Override
            public int compare(TestItem a, TestItem b) {
                return a.value - b.value;
            }
        };
    }

    Comparator<TestItem> makeSaltComparator() {
        return new Comparator<TestItem>() {
            @Override
            public int compare(TestItem a, TestItem b) {
                return a.salt - b.salt;
            }
        };
    }

    @Before
    public void setUp() {
        testedModel = new SortedListModel<>();
    }

    @Test
    public void testSortItems_nullComparator() {
        List<TestItem> initial = Arrays.asList(ITEM_1_2, ITEM_1_1);

        testedModel.setComparator(null);
        Collection<TestItem> sorted = testedModel.sortItems(initial);
        assertEquals(sorted, initial);
    }

    @Test
    public void testSortItems_nullItems() {
        testedModel.setComparator(makeValueComparator());
        Collection<TestItem> sorted = testedModel.sortItems(null);
        assertNull(sorted);
    }

    @Test
    public void testSortItems_retainOrder() {
        List<TestItem> initial = Arrays.asList(ITEM_2_1, ITEM_2_2, ITEM_1_2, ITEM_1_1);
        List<TestItem> expected = Arrays.asList(ITEM_1_2, ITEM_1_1, ITEM_2_1, ITEM_2_2);

        testedModel.setComparator(makeValueComparator());
        Collection<TestItem> sorted = testedModel.sortItems(initial);
        assertArrayEquals(sorted.toArray(), expected.toArray());
    }

    @Test
    public void testSortItems_switchComparator() {
        List<TestItem> initial = Arrays.asList(ITEM_2_1, ITEM_2_2, ITEM_1_2, ITEM_1_1);
        List<TestItem> expected = Arrays.asList(ITEM_2_1, ITEM_1_1, ITEM_2_2, ITEM_1_2);

        testedModel.setComparator(makeSaltComparator());
        Collection<TestItem> sorted = testedModel.sortItems(initial);
        assertArrayEquals(sorted.toArray(), expected.toArray());

        expected = Arrays.asList(ITEM_1_1, ITEM_1_2, ITEM_2_1, ITEM_2_2);

        testedModel.setComparator(makeValueComparator());
        sorted = testedModel.sortItems(sorted);
        assertArrayEquals(sorted.toArray(), expected.toArray());
    }

}
