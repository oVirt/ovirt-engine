package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EnumNameAutoCompleterTest {
    private EnumNameAutoCompleter comp;

    @Before
    public void setup() {
        comp = new EnumNameAutoCompleter(Jedi.class);
    }

    @Test
    public void testValues() {
        List<String> comps = Arrays.asList(comp.getCompletion("L"));
        assertTrue("luke", comps.contains("luke"));
        assertTrue("leia", comps.contains("leia"));

        assertTrue(comp.getCompletion("Z").length == 0);
    }

    @Test
    public void testConvertFieldEnumValueToActualValue() {
        assertEquals("MACE", "MACE", comp.convertFieldEnumValueToActualValue("MACE"));
        assertEquals("mace", "MACE", comp.convertFieldEnumValueToActualValue("mace"));
    }
}
