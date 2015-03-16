package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ListUtilsTest {
    @Test
    public void filter() {
        Assert.assertNull("null list must result in null", ListUtils.filter(null, null));
        Assert.assertNull("null list must result in null, even if the filter is not null", ListUtils.filter(null, null));
        Assert.assertTrue("empty list must be an empty list", ListUtils.filter(new ArrayList<String>(), null).isEmpty());
        Assert.assertTrue("empty list must be an empty list",
                ListUtils.filter(new ArrayList<String>(), new ListUtils.Filter<String>() {

                    @Override
                    public List<String> filter(List<String> data) {
                        return data;
                    }
                }).isEmpty());

        List<String> testList = new ArrayList<String>();
        testList.add("bla");
        testList.add("bli");
        testList.add("blu");
        testList.add("blo");
        testList.add("foo");
        testList.add("bar");
        List<String> filtered =
                ListUtils.filter(testList, new ListUtils.PredicateFilter<String>(new ListUtils.Predicate<String>() {
                    @Override
                    public boolean evaluate(String obj) {
                        return obj.endsWith("a");
                    }
                }));

        Assert.assertEquals("only one element should match the criteria", 1, filtered.size());
        Assert.assertEquals("and it should be 'bla'", "bla", filtered.get(0));
    }

    @Test
    public void nullSafeAdd() {
        ListUtils.nullSafeAdd(null, "foo");
        ListUtils.nullSafeAdd(null, new Object());
        ListUtils.nullSafeAdd(null, 1L);

        final ArrayList<String> list = new ArrayList<String>();
        ListUtils.nullSafeAdd(list, "foo");
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains("foo"));
    }

    @Test
    public void testListsEqual() {
        List<String> lst1 = Arrays.asList("a", "b", "c");

        Assert.assertFalse(ListUtils.listsEqual(lst1, Arrays.asList("a")));
        Assert.assertFalse(ListUtils.listsEqual(lst1, Arrays.asList("d", "e", "f")));
        Assert.assertFalse(ListUtils.listsEqual(lst1, Arrays.asList("b", "c", "d")));
        Assert.assertTrue(ListUtils.listsEqual(lst1, Arrays.asList("c", "b", "a")));
    }

    @Test
    public void testGetAddedElements() {
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();

        list1.add("string1");
        list2.add("string1");

        assertTrue(ListUtils.getAddedElements(list1, list2).isEmpty());

        list2.add("string2");
        list2.add("string3");
        list1.add("string4");

        Collection<String> addedElements = ListUtils.getAddedElements(list1, list2);
        assertTrue(addedElements.size() == 2);
        assertTrue(addedElements.contains("string2"));
        assertTrue(addedElements.contains("string3"));

        addedElements = ListUtils.getAddedElements(list2, list1);
        assertTrue(addedElements.size() == 1);
        assertTrue(addedElements.contains("string4"));
    }

    @Test
    public void testFirstMatch() {
        List<String> source = Arrays.asList("zero", "one", "two ", "three");

        Assert.assertEquals("one", ListUtils.firstMatch(source, "one", "two"));
        Assert.assertEquals("one", ListUtils.firstMatch(source, "two", "one"));
        Assert.assertEquals(null, ListUtils.firstMatch(source, (String[]) null));
    }
}
