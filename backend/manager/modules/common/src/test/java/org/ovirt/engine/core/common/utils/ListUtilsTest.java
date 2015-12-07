package org.ovirt.engine.core.common.utils;

import java.util.Arrays;
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
}
