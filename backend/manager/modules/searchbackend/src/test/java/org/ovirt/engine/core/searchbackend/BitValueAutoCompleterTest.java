package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * this is the main test class for the BaseAutoCompleter class
 *
 */
public class BitValueAutoCompleterTest {
    private IAutoCompleter comp;

    @BeforeEach
    public void setUp() {
        comp = new BitValueAutoCompleter();
    }

    @Test
    public void testEmpty() {
        List<String> comps = Arrays.asList(comp.getCompletion(""));
        assertTrue(comps.contains("true"), "true");
        assertTrue(comps.contains("false"), "false");
        assertTrue(!comps.contains("False"), "False");
    }

    @Test
    public void testSpace() {
        List<String> comps = Arrays.asList(comp.getCompletion(" "));
        assertTrue(comps.contains("true"), "true");
        assertTrue(comps.contains("false"), "false");
        assertTrue(!comps.contains("False"), "False");
    }

    @Test
    public void testValue() {
        List<String> comps = Arrays.asList(comp.getCompletion("t"));
        assertTrue(comps.contains("true"), "true");
        assertTrue(!comps.contains("false"), "false");
    }

    @Test
    public void testValueCaps() {
        List<String> comps = Arrays.asList(comp.getCompletion("FA"));
        assertTrue(comps.contains("false"), "false");
        assertTrue(!comps.contains("true"), "true");
    }

    @Test
    public void testValidate() {
        assertTrue(comp.validate("true"), "true");
        assertTrue(comp.validate("TRUE"), "TRUE");
        assertFalse(comp.validate("JarJar"), "JarJar");
    }

    @Test
    public void testValidateCompletion() {
        assertTrue(comp.validateCompletion("TRUE"), "TRUE");
        assertTrue(comp.validateCompletion("TR"), "TR");
        assertFalse(comp.validateCompletion("JarJar"), "JarJar");
    }
}
