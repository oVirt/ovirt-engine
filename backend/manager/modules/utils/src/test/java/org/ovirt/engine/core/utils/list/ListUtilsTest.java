package org.ovirt.engine.core.utils.list;

import java.util.ArrayList;
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

        Assert.assertEquals("only one element should match the criteria", 1l, filtered.size());
        Assert.assertEquals("and it should be 'bla'", "bla", filtered.get(0));
    }

    @Test
    public void nullSafeAdd() {
        ListUtils.nullSafeAdd(null, "foo");
        ListUtils.nullSafeAdd(null, new Object());
        ListUtils.nullSafeAdd(null, 1l);

        final ArrayList<String> list = new ArrayList<String>();
        ListUtils.nullSafeAdd(list, "foo");
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains("foo"));
    }

}
