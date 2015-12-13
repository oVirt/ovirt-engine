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
    public void testListsEqual() {
        List<String> lst1 = Arrays.asList("a", "b", "c");

        Assert.assertFalse(ListUtils.listsEqual(lst1, Arrays.asList("a")));
        Assert.assertFalse(ListUtils.listsEqual(lst1, Arrays.asList("d", "e", "f")));
        Assert.assertFalse(ListUtils.listsEqual(lst1, Arrays.asList("b", "c", "d")));
        Assert.assertTrue(ListUtils.listsEqual(lst1, Arrays.asList("c", "b", "a")));
    }

    @Test
    public void testGetAddedElements() {
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();

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
}
