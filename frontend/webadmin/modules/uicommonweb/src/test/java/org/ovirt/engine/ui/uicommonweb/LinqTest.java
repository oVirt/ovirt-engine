package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class LinqTest {
    @Test
    public void intersection() {
        Assert.assertNotNull(Linq.Intersection(new ArrayList<ArrayList<String>>()));
        Assert.assertTrue(Linq.Intersection(new ArrayList<ArrayList<String>>()).isEmpty());

        Assert.assertNotNull(Linq.Intersection(null));
        Assert.assertTrue(Linq.Intersection(null).isEmpty());

        ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
        lists.add(new ArrayList<String>(Arrays.asList("A", "B", "C"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lists.add(new ArrayList<String>(Arrays.asList("C", "D", "E"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertNotNull(Linq.Intersection(lists));
        Assert.assertFalse(Linq.Intersection(lists).isEmpty());
        Assert.assertTrue(Linq.Intersection(lists).contains("C")); //$NON-NLS-1$
        Assert.assertFalse(Linq.Intersection(lists).contains("A")); //$NON-NLS-1$

        lists = new ArrayList<ArrayList<String>>();
        lists.add(new ArrayList<String>(Arrays.asList("1", "2", "3"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lists.add(new ArrayList<String>(Arrays.asList("A", "B", "C"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertNotNull(Linq.Intersection(lists));
        Assert.assertTrue(Linq.Intersection(null).isEmpty());

    }
}
