package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class RemoveAdGroupCommandTest {

    /**
     * The command under test.
     */
    private RemoveAdGroupCommand<AdElementParametersBase> command;
    private Guid adElementId = Guid.NewGuid();

    @Before
    public void initializeCommand() {
        AdElementParametersBase parameters = createParameters();
        command = spy(new RemoveAdGroupCommand<AdElementParametersBase>(parameters));
    }

    /**
     * @return Valid parameters for the command.
     */
    private AdElementParametersBase createParameters() {
        return new AdElementParametersBase(adElementId);
    }

    @Test
    public void canDoActionFailsOnRemoveLastAdGroupWithSuperUserPrivileges() throws Exception {
        mockIsLastSuperUserGroup(true);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ERROR_CANNOT_REMOVE_LAST_SUPER_USER_ROLE.toString()));
    }

    @Test
    public void canDoActionSucceedsOnRemoveNotLastAdGroupWithSuperUserPrivileges() throws Exception {
        mockIsLastSuperUserGroup(false);
        assertTrue(command.canDoAction());
    }

    private void mockIsLastSuperUserGroup(boolean isLast) {
        doReturn(isLast).when(command).isLastSuperUserGroup(adElementId);
    }
}
