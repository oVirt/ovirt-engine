package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @see BitValueAutoCompleterTest
 */
public class BaseAutoCompleterTest {
    @Test
    public void testChangeCaseDisplay() {
        BaseAutoCompleter bac = new BaseAutoCompleter();
        assertEquals("jarjarbinks", bac.changeCaseDisplay("JarJarBinks"), "normal");
        assertEquals("Monday", bac.changeCaseDisplay("Monday"), "dayOfWeek");
        assertEquals("monday", bac.changeCaseDisplay("monday"), "dayOfWeek");
    }
}
