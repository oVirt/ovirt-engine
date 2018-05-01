package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class UpdateMacPoolCommandTest {


    @Test(expected = IllegalArgumentException.class)
    public void testFirstParameterIsNotNull() {
        UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(null, new MacPool());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSecondParameterIsNotNull() {
        UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(new MacPool(), null);
    }

    @Test
    public void testValidateDefaultFlagIsNotChangedWhenFlagChanged() {
        final MacPool macPool1 = new MacPool();
        final MacPool macPool2 = new MacPool();
        macPool2.setDefaultPool(!macPool1.isDefaultPool());

        assertThat(UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(macPool1, macPool2),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CHANGING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED));
    }

    @Test
    public void testValidateDefaultFlagIsNotChangedWhenFlagNotChanged() {
        final MacPool macPool1 = new MacPool();
        final MacPool macPool2 = new MacPool();
        assertThat(UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(macPool1, macPool2), isValid());
    }
}
