package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EnumValueAutoCompleterTest {
    private EnumValueAutoCompleter comp;

    @Before
    public void setUp() {
        comp = new EnumValueAutoCompleter(Jedi.class);
    }


    @Test
    public void testValues() {
        List<String> comps = Arrays.asList(comp.getCompletion("L"));
        assertTrue("luke", comps.contains("luke"));
        assertTrue("leia", comps.contains("leia"));
    }

    @Test
    public void testConvertFieldEnumValueToActualValue() {
        assertEquals("MACE", "4", comp.convertFieldEnumValueToActualValue("MACE"));
        assertEquals("mace", "4", comp.convertFieldEnumValueToActualValue("mace"));
    }
}
