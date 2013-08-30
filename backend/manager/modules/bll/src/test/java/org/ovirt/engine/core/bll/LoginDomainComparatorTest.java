package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoginDomainComparatorTest {

    @Test
    public void testCompareWithoutInternal() {
        List<String> list = new ArrayList<String>();
        list.add("a1");
        list.add("b1");
        list.add("c1");
        list.add("d1");
        list.add("a2");
        Collections.sort(list, new LoginDomainComparator("internal"));
        assertEquals("a1", list.get(0));
        assertEquals("a2", list.get(1));
        assertEquals("b1", list.get(2));
        assertEquals("c1", list.get(3));
        assertEquals("d1", list.get(4));
    }

    @Test
    public void testCompareWithInternal() {
        List<String> list = new ArrayList<String>();
        list.add("a1");
        list.add("b1");
        list.add("internal");
        list.add("c1");
        list.add("d1");
        list.add("a2");
        Collections.sort(list, new LoginDomainComparator("internal"));
        assertEquals("a1", list.get(0));
        assertEquals("a2", list.get(1));
        assertEquals("b1", list.get(2));
        assertEquals("c1", list.get(3));
        assertEquals("d1", list.get(4));
        assertEquals("internal", list.get(5));
    }

    @Test
    public void testCompareWithTwoInternal() {
        List<String> list = new ArrayList<String>();
        list.add("a1");
        list.add("b1");
        list.add("internal");
        list.add("c1");
        list.add("d1");
        list.add("internal");
        list.add("a2");
        Collections.sort(list, new LoginDomainComparator("internal"));
        assertEquals("a1", list.get(0));
        assertEquals("a2", list.get(1));
        assertEquals("b1", list.get(2));
        assertEquals("c1", list.get(3));
        assertEquals("d1", list.get(4));
        assertEquals("internal", list.get(5));
        assertEquals("internal", list.get(6));
    }

    @Test
    public void testCompareWithNulls() {
        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add(null);
        list.add("internal");
        list.add("c1");
        list.add("d1");
        list.add("internal");
        list.add("a2");
        Collections.sort(list, new LoginDomainComparator("internal"));
        assertEquals(null, list.get(0));
        assertEquals(null, list.get(1));
        assertEquals("a2", list.get(2));
        assertEquals("c1", list.get(3));
        assertEquals("d1", list.get(4));
        assertEquals("internal", list.get(5));
        assertEquals("internal", list.get(6));
    }
}
