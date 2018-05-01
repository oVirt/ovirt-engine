package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
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
