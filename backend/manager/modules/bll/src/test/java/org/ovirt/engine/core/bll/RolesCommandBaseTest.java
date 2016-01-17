package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * A test case for the {@link RolesCommandBase} class.
 * This test uses an anonymous implementation of the class' abstract methods to allow testing the ones that aren't.
 */
public class RolesCommandBaseTest extends AbstractRolesCommandTestBase {

    @Override
    protected RolesCommandBase<RolesParameterBase> generateCommand() {
        return new RolesCommandBase<RolesParameterBase>(getParams(), null) {

            @Override
            protected void executeCommand() {
                // Do nothing!
            }
        };
    }

    @Test
    public void testCheckIfRoleIsReadOnlyTrue() {
        getRole().setReadonly(true);
        List<String> messages = new ArrayList<>(1);
        assertTrue("Role should be read only", getCommand().checkIfRoleIsReadOnly(messages));
        assertEquals("Wrong validate message",
                EngineMessage.ACTION_TYPE_FAILED_ROLE_IS_READ_ONLY.toString(),
                messages.get(0));
    }

    @Test
    public void testCheckIfRoleIsReadOnlyFalse() {
        getRole().setReadonly(false);
        List<String> messages = new ArrayList<>();
        assertFalse("Role should be read only", getCommand().checkIfRoleIsReadOnly(messages));
        assertTrue("Shouldn't be any validate messages", messages.isEmpty());
    }
}
