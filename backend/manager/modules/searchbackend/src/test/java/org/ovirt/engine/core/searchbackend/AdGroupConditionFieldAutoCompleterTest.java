package org.ovirt.engine.core.searchbackend;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * this is the main test class for the BaseConditionFieldAutoCompleter class
 *
 */
public class AdGroupConditionFieldAutoCompleterTest extends TestCase {
    public void testEmpty() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        List<String> comps = Arrays.asList(comp.getCompletion(""));
        System.out.println(comps);
        assertTrue("name", comps.contains("name"));
        assertFalse("False", comps.contains("False"));
    }

    public void testValidateFieldValue() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertTrue("Jar Jar", comp.validateFieldValue("NAME", "Jar Jar"));
        assertTrue("1234", comp.validateFieldValue("NAME", "1234"));
        assertFalse("<>", comp.validateFieldValue("NAME", "<>"));
        assertTrue("Invalid Field", comp.validateFieldValue("NAME222", "Jar Jar"));
        assertTrue("Lowercase Field", comp.validateFieldValue("name", "<>"));
    }

    public void testGetDbFieldName() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertEquals("$CN", "$CN", comp.getDbFieldName("NAME"));
        assertNull("name", comp.getDbFieldType("name"));
        assertNull("22", comp.getDbFieldType("22"));
    }

    public void testGetDbFieldType() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertEquals("NAME", String.class, comp.getDbFieldType("NAME"));
        assertNull("name", comp.getDbFieldType("name"));
        assertNull("22", comp.getDbFieldType("22"));
    }

    public void testBuildFreeTextConditionSql() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        String sql = comp.buildFreeTextConditionSql("ATable", "=", "JarJar", false);
        assertEquals(" (  ATable.$CN LIKE '%JarJar%' ) ", " (  ATable.$CN LIKE '%JarJar%' ) ", sql);
        sql = comp.buildFreeTextConditionSql("ATable", "!=", "JarJar", false);
        assertEquals(" (  ATable.$CN NOT LIKE '%JarJar%' ) ", " (  ATable.$CN NOT LIKE '%JarJar%' ) ", sql);
    }
}
