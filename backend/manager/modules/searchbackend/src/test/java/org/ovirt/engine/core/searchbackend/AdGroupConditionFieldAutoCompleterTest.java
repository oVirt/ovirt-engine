package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * this is the main test class for the BaseConditionFieldAutoCompleter class
 */
public class AdGroupConditionFieldAutoCompleterTest {

    @Test
    public void testEmpty() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        List<String> comps = Arrays.asList(comp.getCompletion(""));
        assertTrue(comps.contains("name"), "name");
        assertFalse(comps.contains("False"), "False");
    }

    @Test
    public void testValidateFieldValue() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("NAME", "Jar Jar"), "Jar Jar");
        assertTrue(comp.validateFieldValue("NAME", "1234"), "1234");
        assertFalse(comp.validateFieldValue("NAME", "<>"), "<>");
        assertTrue(comp.validateFieldValue("NAME222", "Jar Jar"), "Invalid Field");
        assertTrue(comp.validateFieldValue("name", "<>"), "Lowercase Field");
    }

    @Test
    public void testGetDbFieldName() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertEquals("$CN", comp.getDbFieldName("NAME"), "$CN");
        assertNull(comp.getDbFieldType("name"), "name");
        assertNull(comp.getDbFieldType("22"), "22");
    }

    @Test
    public void testGetDbFieldType() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertEquals(String.class, comp.getDbFieldType("NAME"), "NAME");
        assertNull(comp.getDbFieldType("name"), "name");
        assertNull(comp.getDbFieldType("22"), "22");
    }

    @Test
    @Disabled
    public void testBuildFreeTextConditionSql() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        String sql = comp.buildFreeTextConditionSql("ATable", "=", "JarJar", false);
        assertEquals(" (  ATable.cn LIKE '%JarJar%' ) ", sql, " (  ATable.cn LIKE '%JarJar%' ) ");
        sql = comp.buildFreeTextConditionSql("ATable", "!=", "JarJar", false);
        assertEquals(" (  ATable.cn NOT LIKE '%JarJar%' ) ", sql, " (  ATable.cn NOT LIKE '%JarJar%' ) ");
    }
}
