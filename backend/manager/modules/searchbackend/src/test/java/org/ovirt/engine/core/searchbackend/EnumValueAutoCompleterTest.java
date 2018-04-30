package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnumValueAutoCompleterTest {
    private EnumValueAutoCompleter comp;

    @BeforeEach
    public void setUp() {
        comp = new EnumValueAutoCompleter(Jedi.class);
    }


    @Test
    public void testValues() {
        List<String> comps = Arrays.asList(comp.getCompletion("L"));
        assertTrue(comps.contains("luke"), "luke");
        assertTrue(comps.contains("leia"), "leia");
    }

    @Test
    public void testConvertFieldEnumValueToActualValue() {
        assertEquals("4", comp.convertFieldEnumValueToActualValue("MACE"), "MACE");
        assertEquals("4", comp.convertFieldEnumValueToActualValue("mace"), "mace");
    }
}
