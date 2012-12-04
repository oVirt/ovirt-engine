package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

/** A test case for {@link CommandBase} */
public class CommandBaseTest {
    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.IsMultilevelAdministrationOn, false));

    @Rule
    public MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    /** The session to use */
    private String session = "";

    /** A dummy class for testing CommandBase's functionality */
    @SuppressWarnings("serial")
    private class CommandBaseDummy extends CommandBase<VdcActionParametersBase> {

        /** A dummy constructor to pass parameters, since constructors aren't inherited in Java */
        protected CommandBaseDummy(VdcActionParametersBase params) {
            super(params);
        }

        @Override
        protected void executeCommand() {
            setSucceeded(true);
        }

        @Override
        public List<PermissionSubject> getPermissionCheckSubjects() {
            return Collections.emptyList();
        }

        @Override
        protected BusinessEntitySnapshotDAO getBusinessEntitySnapshotDAO() {
            return mock(BusinessEntitySnapshotDAO.class);
        }

        @Override
        protected boolean isBackwardsCompatible() {
            return true;
        }
    }

    @Before
    @After
    public void clearEnvironment() {
        ThreadLocalParamsContainer.clean();
        SessionDataContainer.getInstance().removeSession();
        SessionDataContainer.getInstance().removeSession(session);
    }

    /** Testing the constructor, which adds the user id to the thread local container */
    @Test
    public void testConstructor() {
        session = RandomStringUtils.random(10);

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

    /**
     * Tests that the default implementation of {@link CommandBase#executeAction()} calls
     * {@link CommandBase#executeCommand()}, and doesn't attempt to access any handlers
     */
    @Test
    public void testExecuteNoTaskHandlers() {
        VdcActionParametersBase parameterMock = mock(VdcActionParametersBase.class);
        when(parameterMock.getTransactionScopeOption()).thenReturn(TransactionScopeOption.Required);
        CommandBase<VdcActionParametersBase> command = spy(new CommandBaseDummy(parameterMock));

        command.executeAction();
        verify(command).executeCommand();
    }

    @Test
    public void testHandlersInEndSuccessful() {
        SPMAsyncTaskHandler handler1 = mock(SPMAsyncTaskHandler.class);
        SPMAsyncTaskHandler handler2 = mock(SPMAsyncTaskHandler.class);
        CommandBase<VdcActionParametersBase> command = spy(new CommandBaseDummy(new VdcActionParametersBase()));
        when(command.getTaskHandlers()).thenReturn(Arrays.<SPMAsyncTaskHandler> asList(handler1, handler2));

        command.getReturnValue().setSucceeded(true);
        command.endActionInTransactionScope();
        verify(handler1).endSuccessfully();
        verify(handler2).execute();
        verifyNoMoreInteractions(handler1, handler2);
    }

    @Test
    public void testNoHandlersInEndSuccessful() {
        VdcActionParametersBase parameterMock = mock(VdcActionParametersBase.class);
        when(parameterMock.getTransactionScopeOption()).thenReturn(TransactionScopeOption.Required);
        CommandBase<VdcActionParametersBase> command = spy(new CommandBaseDummy(parameterMock));
        doReturn(true).when(command).isEndSuccessfully();
        command.endActionInTransactionScope();
        verify(command).endSuccessfully();
    }

    @Test
    public void testHandlersInEndWithFailure() {
        SPMAsyncTaskHandler handler1 = mock(SPMAsyncTaskHandler.class);
        SPMAsyncTaskHandler handler2 = mock(SPMAsyncTaskHandler.class);
        CommandBase<VdcActionParametersBase> command = spy(new CommandBaseDummy(new VdcActionParametersBase()));
        when(command.getTaskHandlers()).thenReturn(Arrays.<SPMAsyncTaskHandler> asList(handler1, handler2));

        command.getParameters().setTaskGroupSuccess(false);
        command.getParameters().setExecutionIndex(1);
        command.getReturnValue().setSucceeded(false);
        command.endActionInTransactionScope();
        verify(handler2).endWithFailure();
        verify(handler1).compensate();
        verify(handler1).getRevertTaskType();
        verifyNoMoreInteractions(handler2, handler1);
    }

    @Test
    public void testNoHandlersInEndWithFailure() {
        VdcActionParametersBase parameterMock = mock(VdcActionParametersBase.class);
        when(parameterMock.getTransactionScopeOption()).thenReturn(TransactionScopeOption.Required);
        CommandBase<VdcActionParametersBase> command = spy(new CommandBaseDummy(parameterMock));
        doReturn(false).when(command).isEndSuccessfully();
        command.endActionInTransactionScope();
        verify(command).endWithFailure();
    }
}
