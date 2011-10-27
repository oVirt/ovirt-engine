package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

/**
 * @see BitValueAutoCompleterTest
 */
public class BaseAutoCompleterTest extends TestCase {
    public void testChangeCaseDisplay() {
        BaseAutoCompleter bac = new BaseAutoCompleter();
        assertEquals("normal", "jarjarbinks", bac.changeCaseDisplay("JarJarBinks"));
        assertEquals("dayOfWeek", "Monday", bac.changeCaseDisplay("Monday"));
        assertEquals("dayOfWeek", "monday", bac.changeCaseDisplay("monday"));
    }
}
