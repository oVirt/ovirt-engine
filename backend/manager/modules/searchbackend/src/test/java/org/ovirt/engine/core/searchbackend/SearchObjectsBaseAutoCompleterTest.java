package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

public class SearchObjectsBaseAutoCompleterTest extends TestCase {

    public void testChangeCaseDisplay() {
        SearchObjectsBaseAutoCompleter comp = new SearchObjectsBaseAutoCompleter();
        assertEquals("hello", "Hello", comp.changeCaseDisplay("hello"));
        assertEquals("Hello", "Hello", comp.changeCaseDisplay("Hello"));
        assertEquals("HELLO", "Hello", comp.changeCaseDisplay("HELLO"));
    }

}
