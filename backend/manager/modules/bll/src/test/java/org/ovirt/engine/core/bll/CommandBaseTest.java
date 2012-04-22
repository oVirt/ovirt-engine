package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

/** A test case for {@link CommandBase} */
public class CommandBaseTest {

    /** A dummy class for testing CommandBase's functionality */
    @SuppressWarnings("serial")
    private class CommandBaseDummy extends CommandBase<VdcActionParametersBase> {

        /** A dummy constructor to pass parameters, since constructors aren't inherited in Java */
        protected CommandBaseDummy(VdcActionParametersBase params) {
            super(params);
        }

        @Override
        protected void executeCommand() {
            // Do nothing
        }

        @Override
        public List<PermissionSubject> getPermissionCheckSubjects() {
            return Collections.emptyList();
        }
    }

    @Before
    @After
    public void clearEnvironment() {
        ThreadLocalParamsContainer.clean();
        SessionDataContainer.getInstance().removeSession();
    }

    /** Testing the constructor, which adds the user id to the thread local container */
    @Test
    public void testConstructor() {
        String session = RandomStringUtils.random(10);

        VdcUser user = mock(VdcUser.class);
        when(user.getUserId()).thenReturn(Guid.EVERYONE);

        // Mock the parameters
        VdcActionParametersBase paramterMock = mock(VdcActionParametersBase.class);
        when(paramterMock.getSessionId()).thenReturn(session);

        SessionDataContainer.getInstance().setUser(session, user);

        // Create a command
        CommandBase<VdcActionParametersBase> command = new CommandBaseDummy(paramterMock);

        // Check the session
        assertEquals("wrong user id on command", user.getUserId(), command.getUserId());
        assertEquals("wrong user id on threadlocal", user, ThreadLocalParamsContainer.getVdcUser());
    }
}
