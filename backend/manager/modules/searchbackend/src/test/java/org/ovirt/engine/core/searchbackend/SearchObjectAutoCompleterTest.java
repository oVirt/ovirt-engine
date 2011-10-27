package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

public class SearchObjectAutoCompleterTest extends TestCase {
    public void testGetDefaultSortbyPhrase() {
        SearchObjectAutoCompleter comp = new SearchObjectAutoCompleter(true);
        assertTrue("HOST", comp.getDefaultSort("HOST").contains("vds_name"));
        assertEquals("Garbage", "", comp.getDefaultSort("kjfhkjdshkjfs"));
        assertEquals("Null", "", comp.getDefaultSort(null));
    }

    public void testGetRelatedTableName() {
        SearchObjectAutoCompleter comp = new SearchObjectAutoCompleter(true);
        assertEquals("EVENTS", "audit_log", comp.getRelatedTableName("EVENTS"));
        assertNull("Garbage", comp.getRelatedTableName("kjfhkjdshkjfs"));
        assertNull("Null", comp.getRelatedTableName(null));
    }

    public void testIsCrossReference() {
        SearchObjectAutoCompleter comp = new SearchObjectAutoCompleter(true);
        assertTrue("EVENTS", comp.isCrossReferece("EVENTS", "TEMPLATES"));
        assertFalse("Garbage Cross", comp.isCrossReferece("fsfsdf", "TEMPLATES"));
        assertFalse("Garbage Object", comp.isCrossReferece("EVENTS", "fsfds"));
        assertFalse("Null Object", comp.isCrossReferece("EVENTS", null));
        assertFalse("Null text", comp.isCrossReferece(null, "TEMPLATES"));
    }

    public void testGetInnerJoin() {
        SearchObjectAutoCompleter comp = new SearchObjectAutoCompleter(true);
        assertNotNull("Sanity test", comp.getInnerJoin("EVENT", "USER"));
    }
}
