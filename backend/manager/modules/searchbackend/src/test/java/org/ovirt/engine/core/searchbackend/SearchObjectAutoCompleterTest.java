package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SearchObjectAutoCompleterTest {
    @Test
    public void testGetDefaultSortbyPhrase() {
        SearchObjectAutoCompleter comp = new SearchObjectAutoCompleter(true);
        assertTrue("HOST", comp.getDefaultSort("HOST").contains("vds_name"));
        assertEquals("Garbage", "", comp.getDefaultSort("kjfhkjdshkjfs"));
        assertEquals("Null", "", comp.getDefaultSort(null));
    }

    @Test
    public void testGetRelatedTableName() {
        SearchObjectAutoCompleter comp = new SearchObjectAutoCompleter(true);
        assertEquals("EVENTS", "audit_log", comp.getRelatedTableName("EVENTS"));
        assertNull("Garbage", comp.getRelatedTableName("kjfhkjdshkjfs"));
        assertNull("Null", comp.getRelatedTableName(null));
    }

    @Test
    public void testIsCrossReference() {
        SearchObjectAutoCompleter comp = new SearchObjectAutoCompleter(true);
        assertTrue("EVENTS", comp.isCrossReferece("EVENTS", "TEMPLATES"));
        assertFalse("Garbage Cross", comp.isCrossReferece("fsfsdf", "TEMPLATES"));
        assertFalse("Garbage Object", comp.isCrossReferece("EVENTS", "fsfds"));
        assertFalse("Null Object", comp.isCrossReferece("EVENTS", null));
        assertFalse("Null text", comp.isCrossReferece(null, "TEMPLATES"));
    }

    @Test
    public void testGetInnerJoin() {
        SearchObjectAutoCompleter comp = new SearchObjectAutoCompleter(true);
        assertNotNull("Sanity test", comp.getInnerJoin("EVENT", "USER"));
    }

    @Test
    public void testGetEntitySearchInfo() {
        assertNotNull(SearchObjectAutoCompleter.getEntitySearchInfo(SearchObjects.AUDIT_PLU_OBJ_NAME));
        assertEquals(SearchObjectAutoCompleter.getEntitySearchInfo(SearchObjects.AUDIT_PLU_OBJ_NAME),
                SearchObjectAutoCompleter.getEntitySearchInfo(SearchObjects.AUDIT_OBJ_NAME));
        assertNull(SearchObjectAutoCompleter.getEntitySearchInfo("RANDOM_NOTEXISTING_KEY"));
    }
}
