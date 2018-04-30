package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ProviderType;

public class ProviderConditionFieldAutoCompleterTest {

    private IConditionFieldAutoCompleter comp =  new ProviderConditionFieldAutoCompleter();

    @Test
    public void testExistentType() {
        assertTrue(comp.validateFieldValue(ProviderConditionFieldAutoCompleter.TYPE,
                ProviderType.OPENSTACK_NETWORK.name()));
    }

    @Test
    public void testNonExistentType() {
        assertFalse(comp.validateFieldValue(ProviderConditionFieldAutoCompleter.TYPE, "foo"));
    }

}
