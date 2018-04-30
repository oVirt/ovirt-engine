package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateEnumValueAutoCompleterTest {
    private DateEnumValueAutoCompleter comp;

    @BeforeEach
    public void setUp() {
        comp = new DateEnumValueAutoCompleter(Jedi.class);
    }


    @Test
    public void testValues() {
        List<String> comps = Arrays.asList(comp.getCompletion(" "));
        assertTrue(comps.contains("Monday") || comps.contains("Tuesday"), "Monday");
        assertTrue(comps.contains("mace"), "mace");
    }

    @Test
    public void testConvertFieldEnumValueToActualValue() {
        // Dates should return dates
        String test = "01/20/1972";
        String expected = test;
        assertEquals(expected, comp.convertFieldEnumValueToActualValue(test), test);

        // Days of the week should not change the value
        test = "Monday";
        expected = test;
        assertEquals(expected, comp.convertFieldEnumValueToActualValue(test), test);

        // Check Caps
        test = "mOnday";
        expected = "Monday";
        assertEquals(expected, comp.convertFieldEnumValueToActualValue(test), test);

        // Check the other enum
        test = "MACE";
        expected = "4";
        assertEquals(expected, comp.convertFieldEnumValueToActualValue(test), test);
    }
}
