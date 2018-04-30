package org.ovirt.engine.core.searchbackend.gluster;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.searchbackend.EnumNameAutoCompleter;
import org.ovirt.engine.core.searchbackend.NumericConditionRelationAutoCompleter;
import org.ovirt.engine.core.searchbackend.StringConditionRelationAutoCompleter;

public class GlusterVolumeConditionFieldAutoCompleterTest {
    private final GlusterVolumeConditionFieldAutoCompleter comp = GlusterVolumeConditionFieldAutoCompleter.INSTANCE;

    @Test
    public void testGetFieldRelationshipAutoCompleter() {
        assertTrue(comp.getFieldRelationshipAutoCompleter("name") instanceof StringConditionRelationAutoCompleter);
        assertTrue(comp.getFieldRelationshipAutoCompleter("type") instanceof StringConditionRelationAutoCompleter);
        assertTrue(comp.getFieldRelationshipAutoCompleter("transport_type") instanceof StringConditionRelationAutoCompleter);
        assertTrue(comp.getFieldRelationshipAutoCompleter("status") instanceof StringConditionRelationAutoCompleter);

        assertTrue(comp.getFieldRelationshipAutoCompleter("replica_count") instanceof NumericConditionRelationAutoCompleter);
        assertTrue(comp.getFieldRelationshipAutoCompleter("stripe_count") instanceof NumericConditionRelationAutoCompleter);

        assertNull(comp.getFieldRelationshipAutoCompleter("invalid_field"));
    }

    @Test
    public void testGetFieldValueAutoCompleter() {
        assertTrue(comp.getFieldValueAutoCompleter("type") instanceof EnumNameAutoCompleter);
        assertTrue(comp.getFieldValueAutoCompleter("transport_type") instanceof EnumNameAutoCompleter);
        assertTrue(comp.getFieldValueAutoCompleter("status") instanceof EnumNameAutoCompleter);

        assertNull(comp.getFieldValueAutoCompleter("name"));
        assertNull(comp.getFieldValueAutoCompleter("replica_count"));
        assertNull(comp.getFieldValueAutoCompleter("stripe_count"));

        assertNull(comp.getFieldValueAutoCompleter("invalid_field"));
    }
}
