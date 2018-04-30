package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnumNameAutoCompleterTest {
    private EnumNameAutoCompleter comp;

    @BeforeEach
    public void setup() {
        comp = new EnumNameAutoCompleter(Jedi.class);
    }

    @Test
    public void testValues() {
        List<String> comps = Arrays.asList(comp.getCompletion("L"));
        assertTrue(comps.contains("luke"), "luke");
        assertTrue(comps.contains("leia"), "leia");

        assertEquals(0, comp.getCompletion("Z").length);
    }

    @Test
    public void testConvertFieldEnumValueToActualValue() {
        assertEquals("MACE", comp.convertFieldEnumValueToActualValue("MACE"), "MACE");
        assertEquals("MACE", comp.convertFieldEnumValueToActualValue("mace"), "mace");
    }
}
