package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MultiLevelAdministrationHandler.class})
public class RemoveAdGroupCommandTest {


    /**
     * The command under test.
     */
    private RemoveAdGroupCommand<AdElementParametersBase> command;
    private Guid adElementId = Guid.NewGuid();

    private void initializeCommand() {
        AdElementParametersBase parameters = createParameters();
        command = spy(new RemoveAdGroupCommand<AdElementParametersBase>(parameters));
        mockStatic(MultiLevelAdministrationHandler.class);
    }

    /**
     * @return Valid parameters for the command.
     */
    private AdElementParametersBase createParameters() {
        AdElementParametersBase parameters = new AdElementParametersBase(adElementId);
        return parameters;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initializeCommand();
    }

    @Test
    public void canDoActionFailsOnRemoveLastAdGroupWithSuperUserPrivileges() throws Exception {
        when(MultiLevelAdministrationHandler.isLastSuperUserGroup(adElementId)).thenReturn(true);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ERROR_CANNOT_REMOVE_LAST_SUPER_USER_ROLE.toString()));
    }

    @Test
    public void canDoActionSucceedsOnRemoveNotLastAdGroupWithSuperUserPrivileges() throws Exception {
        when(MultiLevelAdministrationHandler.isLastSuperUserGroup(adElementId)).thenReturn(false);
        assertTrue(command.canDoAction());
    }
}
