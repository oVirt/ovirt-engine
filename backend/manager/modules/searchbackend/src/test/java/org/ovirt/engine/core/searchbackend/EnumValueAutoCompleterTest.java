package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class EnumValueAutoCompleterTest {

    @Test
    public void testValues() {
        IAutoCompleter comp = new EnumValueAutoCompleter(Jedi.class);
        List<String> comps = Arrays.asList(comp.getCompletion("L"));
        assertTrue("luke", comps.contains("luke"));
        assertTrue("leia", comps.contains("leia"));
    }

    @Test
    public void testConvertFieldEnumValueToActualValue() {
        EnumValueAutoCompleter comp = new EnumValueAutoCompleter(Jedi.class);
        assertEquals("MACE", "4", comp.convertFieldEnumValueToActualValue("MACE"));
        assertEquals("mace", "4", comp.convertFieldEnumValueToActualValue("mace"));
    }
}
