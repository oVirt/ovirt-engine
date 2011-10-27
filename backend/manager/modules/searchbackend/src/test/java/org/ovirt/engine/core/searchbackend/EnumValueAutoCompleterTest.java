package org.ovirt.engine.core.searchbackend;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class EnumValueAutoCompleterTest extends TestCase {

    public void testValues() {
        IAutoCompleter comp = new EnumValueAutoCompleter(Jedi.class);
        List<String> comps = Arrays.asList(comp.getCompletion("L"));
        System.out.println(comps);
        assertTrue("luke", comps.contains("luke"));
        assertTrue("leia", comps.contains("leia"));
    }

    public void testConvertFieldEnumValueToActualValue() {
        EnumValueAutoCompleter comp = new EnumValueAutoCompleter(Jedi.class);
        assertEquals("MACE", "4", comp.convertFieldEnumValueToActualValue("MACE"));
        assertEquals("mace", "4", comp.convertFieldEnumValueToActualValue("mace"));
    }
}
