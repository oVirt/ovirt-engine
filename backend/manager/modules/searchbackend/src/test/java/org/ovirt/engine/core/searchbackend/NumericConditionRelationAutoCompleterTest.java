package org.ovirt.engine.core.searchbackend;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class NumericConditionRelationAutoCompleterTest extends TestCase {

    public void testValue() {
        List<String> comps = Arrays.asList(NumericConditionRelationAutoCompleter.INSTANCE.getCompletion("<"));
        assertTrue("<", comps.contains("<"));
        assertTrue("<=", comps.contains("<="));
        assertFalse("=", comps.contains("="));
    }
}
