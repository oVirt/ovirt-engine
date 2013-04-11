package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * this is the main test class for the BaseAutoCompleter class
 *
 */
public class BitValueAutoCompleterTest {
    private IAutoCompleter comp;

    @Before
    public void setUp() {
        comp = new BitValueAutoCompleter();
    }

    @Test
    public void testEmpty() {
        List<String> comps = Arrays.asList(comp.getCompletion(""));
        assertTrue("true", comps.contains("true"));
        assertTrue("false", comps.contains("false"));
        assertTrue("False", !comps.contains("False"));
    }

    @Test
    public void testSpace() {
        List<String> comps = Arrays.asList(comp.getCompletion(" "));
        assertTrue("true", comps.contains("true"));
        assertTrue("false", comps.contains("false"));
        assertTrue("False", !comps.contains("False"));
    }

    @Test
    public void testValue() {
        List<String> comps = Arrays.asList(comp.getCompletion("t"));
        assertTrue("true", comps.contains("true"));
        assertTrue("false", !comps.contains("false"));
    }

    @Test
    public void testValueCaps() {
        List<String> comps = Arrays.asList(comp.getCompletion("FA"));
        assertTrue("false", comps.contains("false"));
        assertTrue("true", !comps.contains("true"));
    }

    @Test
    public void testValidate() {
        assertTrue("true", comp.validate("true"));
        assertTrue("TRUE", comp.validate("TRUE"));
        assertFalse("JarJar", comp.validate("JarJar"));
    }

    @Test
    public void testValidateCompletion() {
        assertTrue("TRUE", comp.validateCompletion("TRUE"));
        assertTrue("TR", comp.validateCompletion("TR"));
        assertFalse("JarJar", comp.validateCompletion("JarJar"));
    }
}
