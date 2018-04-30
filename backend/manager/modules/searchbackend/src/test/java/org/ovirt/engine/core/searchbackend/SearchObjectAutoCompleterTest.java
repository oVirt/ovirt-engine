package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SearchObjectAutoCompleterTest {
    private SearchObjectAutoCompleter comp;

    @BeforeEach
    public void setUp() {
        comp = new SearchObjectAutoCompleter();
    }

    @Test
    public void testGetDefaultSortbyPhrase() {
        assertTrue(comp.getDefaultSort("HOST").contains("vds_name"), "HOST");
        assertEquals("", comp.getDefaultSort("kjfhkjdshkjfs"), "Garbage");
        assertEquals("", comp.getDefaultSort(null), "Null");
    }

    @Test
    public void testGetRelatedTableName() {
        assertEquals("audit_log", comp.getRelatedTableName("EVENTS", true), "EVENTS");
        assertNull(comp.getRelatedTableName("kjfhkjdshkjfs", true), "Garbage");
        assertNull(comp.getRelatedTableName(null, true), "Null");
    }

    @Test
    public void testIsCrossReference() {
        assertTrue(comp.isCrossReference("EVENTS", "TEMPLATES"), "EVENTS");
        assertFalse(comp.isCrossReference("fsfsdf", "TEMPLATES"), "Garbage Cross");
        assertFalse(comp.isCrossReference("EVENTS", "fsfds"), "Garbage Object");
        assertFalse(comp.isCrossReference("EVENTS", null), "Null Object");
        assertFalse(comp.isCrossReference(null, "TEMPLATES"), "Null text");
    }

    @Test
    public void testGetInnerJoin() {
        assertNotNull(comp.getInnerJoin("EVENT", "USER", true), "Sanity test");
    }

    @Test
    public void testGetEntitySearchInfo() {
        assertNotNull(SearchObjectAutoCompleter.getEntitySearchInfo(SearchObjects.AUDIT_PLU_OBJ_NAME));
        assertEquals(SearchObjectAutoCompleter.getEntitySearchInfo(SearchObjects.AUDIT_PLU_OBJ_NAME),
                SearchObjectAutoCompleter.getEntitySearchInfo(SearchObjects.AUDIT_OBJ_NAME));
        assertNull(SearchObjectAutoCompleter.getEntitySearchInfo("RANDOM_NOTEXISTING_KEY"));
    }
}
