package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

/**
 * this is the main test class for the BaseConditionFieldAutoCompleter class
 */
public class AdGroupConditionFieldAutoCompleterTest {

    @Test
    public void testEmpty() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        List<String> comps = Arrays.asList(comp.getCompletion(""));
        assertTrue("name", comps.contains("name"));
        assertFalse("False", comps.contains("False"));
    }

    @Test
    public void testValidateFieldValue() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertTrue("Jar Jar", comp.validateFieldValue("NAME", "Jar Jar"));
        assertTrue("1234", comp.validateFieldValue("NAME", "1234"));
        assertFalse("<>", comp.validateFieldValue("NAME", "<>"));
        assertTrue("Invalid Field", comp.validateFieldValue("NAME222", "Jar Jar"));
        assertTrue("Lowercase Field", comp.validateFieldValue("name", "<>"));
    }

    @Test
    public void testGetDbFieldName() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertEquals("$CN", "$CN", comp.getDbFieldName("NAME"));
        assertNull("name", comp.getDbFieldType("name"));
        assertNull("22", comp.getDbFieldType("22"));
    }

    @Test
    public void testGetDbFieldType() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        assertEquals("NAME", String.class, comp.getDbFieldType("NAME"));
        assertNull("name", comp.getDbFieldType("name"));
        assertNull("22", comp.getDbFieldType("22"));
    }

    @Test
    @Ignore
    public void testBuildFreeTextConditionSql() {
        IConditionFieldAutoCompleter comp = new AdGroupConditionFieldAutoCompleter();
        String sql = comp.buildFreeTextConditionSql("ATable", "=", "JarJar", false);
        assertEquals(" (  ATable.cn LIKE '%JarJar%' ) ", " (  ATable.cn LIKE '%JarJar%' ) ", sql);
        sql = comp.buildFreeTextConditionSql("ATable", "!=", "JarJar", false);
        assertEquals(" (  ATable.cn NOT LIKE '%JarJar%' ) ", " (  ATable.cn NOT LIKE '%JarJar%' ) ", sql);
    }
}
