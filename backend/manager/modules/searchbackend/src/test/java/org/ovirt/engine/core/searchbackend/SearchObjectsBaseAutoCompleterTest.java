package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SearchObjectsBaseAutoCompleterTest {

    @Test
    public void testChangeCaseDisplay() {
        SearchObjectsBaseAutoCompleter comp = new SearchObjectsBaseAutoCompleter();
        assertEquals("hello", "Hello", comp.changeCaseDisplay("hello"));
        assertEquals("Hello", "Hello", comp.changeCaseDisplay("Hello"));
        assertEquals("HELLO", "Hello", comp.changeCaseDisplay("HELLO"));
        assertEquals("helLO", "Hello", comp.changeCaseDisplay("helLO"));
    }

}
