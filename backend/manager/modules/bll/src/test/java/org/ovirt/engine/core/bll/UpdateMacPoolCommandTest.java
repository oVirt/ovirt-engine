package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class UpdateMacPoolCommandTest {


    @Test
    public void testFirstParameterIsNotNull() {
        assertThrows(IllegalArgumentException.class,
                () -> UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(null, new MacPool()));
    }

    @Test
    public void testSecondParameterIsNotNull() {
        assertThrows(IllegalArgumentException.class,
                () -> UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(new MacPool(), null));
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
