package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SearchObjectsBaseAutoCompleterTest {

    @Test
    public void testChangeCaseDisplay() {
        SearchObjectsBaseAutoCompleter comp = new SearchObjectsBaseAutoCompleter();
        assertEquals("Hello", comp.changeCaseDisplay("hello"), "hello");
        assertEquals("Hello", comp.changeCaseDisplay("Hello"), "Hello");
        assertEquals("Hello", comp.changeCaseDisplay("HELLO"), "HELLO");
        assertEquals("Hello", comp.changeCaseDisplay("helLO"), "helLO");
    }

}
