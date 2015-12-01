package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class LinqTest {
    @Test
    public void intersection() {
        Assert.assertNotNull(Linq.intersection(new ArrayList<ArrayList<String>>()));
        Assert.assertTrue(Linq.intersection(new ArrayList<ArrayList<String>>()).isEmpty());

        Assert.assertNotNull(Linq.intersection(null));
        Assert.assertTrue(Linq.intersection(null).isEmpty());

        ArrayList<ArrayList<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("A", "B", "C"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lists.add(new ArrayList<>(Arrays.asList("C", "D", "E"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertNotNull(Linq.intersection(lists));
        Assert.assertFalse(Linq.intersection(lists).isEmpty());
        Assert.assertTrue(Linq.intersection(lists).contains("C")); //$NON-NLS-1$
        Assert.assertFalse(Linq.intersection(lists).contains("A")); //$NON-NLS-1$

        lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("1", "2", "3"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lists.add(new ArrayList<>(Arrays.asList("A", "B", "C"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertNotNull(Linq.intersection(lists));
        Assert.assertTrue(Linq.intersection(null).isEmpty());

    }
}
