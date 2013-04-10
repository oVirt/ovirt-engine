package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @see BitValueAutoCompleterTest
 */
public class BaseAutoCompleterTest {
    @Test
    public void testChangeCaseDisplay() {
        BaseAutoCompleter bac = new BaseAutoCompleter();
        assertEquals("normal", "jarjarbinks", bac.changeCaseDisplay("JarJarBinks"));
        assertEquals("dayOfWeek", "Monday", bac.changeCaseDisplay("Monday"));
        assertEquals("dayOfWeek", "monday", bac.changeCaseDisplay("monday"));
    }
}
