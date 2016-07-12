package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDao;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

/** A test case for {@link CommandBase} */
public class CommandBaseTest extends BaseCommandTest {
    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.IsMultilevelAdministrationOn, false),
            mockConfig(ConfigValues.UserSessionTimeOutInterval, 60),
            mockConfig(ConfigValues.UserSessionHardLimit, 600));

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    protected String session = "someSession";

    @Before
    public void setupEnvironment() {
        CorrelationIdTracker.clean();
        DbUser user = mock(DbUser.class);

        when(engineSessionDao.remove(any(Long.class))).thenReturn(1);

        sessionDataContainer.setUser(session, user);
    }

    @After
    public void clearEnvironment() {
        CorrelationIdTracker.clean();
        sessionDataContainer.removeSessionOnLogout(session);
    }

    /** A dummy class for testing CommandBase's functionality */
    private class CommandBaseDummy extends CommandBase<VdcActionParametersBase> {

        /** A dummy constructor to pass parameters, since constructors aren't inherited in Java */
        protected CommandBaseDummy(VdcActionParametersBase params) {
            super(params, CommandContext.createContext(params.getSessionId()));
            setCompensationContext(NoOpCompensationContext.getInstance());
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
        protected BusinessEntitySnapshotDao getBusinessEntitySnapshotDao() {
            return mock(BusinessEntitySnapshotDao.class);
        }

        @Override
        protected boolean parentHasCallback() {
            return false;
        }

        @Override
        protected void logRollbackedTask() {
            return;
        }

        @Override
        public void setCommandStatus(CommandStatus status) {
        }

        @Override
        public void setCommandExecuted() {
        }

        @Override
        protected void updateCommandIfNeeded() {
        }
    }

    /** Testing the constructor, which adds the user id to the thread local container */
    @Test
    public void testConstructor() {
        DbUser user = mock(DbUser.class);
        when(user.getId()).thenReturn(Guid.EVERYONE);

        // Mock the parameters
        VdcActionParametersBase paramterMock = mock(VdcActionParametersBase.class);
        when(paramterMock.getSessionId()).thenReturn(session);
        sessionDataContainer.setUser(session, user);

        // Create a command
        CommandBase<VdcActionParametersBase> command = new CommandBaseDummy(paramterMock) {
            @Override
            protected SessionDataContainer getSessionDataContainer() {
                return sessionDataContainer;
            }
        };
        command.postConstruct();

        // Check the session
        assertEquals("wrong user id on command", user.getId(), command.getUserId());
    }

    /**
     * Tests that the default implementation of {@link CommandBase#executeAction()} calls
     * {@link CommandBase#executeCommand()}, and doesn't attempt to access any handlers
     */
    @Test
    public void testExecuteNoTaskHandlers() {
        VdcActionParametersBase parameterMock = mock(VdcActionParametersBase.class);
        when(parameterMock.getTransactionScopeOption()).thenReturn(TransactionScopeOption.Required);
        when(parameterMock.getLockProperties()).thenReturn(LockProperties.create(LockProperties.Scope.None));
        CommandBase<VdcActionParametersBase> command = spy(new CommandBaseDummy(parameterMock) {
            @Override
            protected SessionDataContainer getSessionDataContainer() {
                return sessionDataContainer;
            }
        });
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

    @Test
    public void logRenamedEntityNotRename() {
        CommandBase<?> command = mock(CommandBase.class);
        doCallRealMethod().when(command).logRenamedEntity();
        command.logRenamedEntity();
    }

    @Test
    public void logRenamedEntity() {
        abstract class RenameCommand extends CommandBaseDummy implements RenamedEntityInfoProvider {

            protected RenameCommand(VdcActionParametersBase params) {
                super(params);
            }

        }
        RenameCommand command = mock(RenameCommand.class);
        when(command.getEntityOldName()).thenReturn(null);
        when(command.getEntityNewName()).thenReturn(null);
        doCallRealMethod().when(command).logRenamedEntity();
        command.logRenamedEntity();
        when(command.getEntityOldName()).thenReturn("foo");
        when(command.getEntityNewName()).thenReturn("bar");
        when(command.getCurrentUser()).thenReturn(mock(DbUser.class));
        command.logRenamedEntity();
        when(command.getEntityOldName()).thenReturn(null);
        when(command.getEntityNewName()).thenReturn("bar");
        command.logRenamedEntity();
        when(command.getEntityOldName()).thenReturn("foo");
        when(command.getEntityNewName()).thenReturn(null);
        command.logRenamedEntity();
    }

    @Test
    public void testExtractVariableDeclarationsForStaticMsgs() {
        VdcActionParametersBase parameterMock = mock(VdcActionParametersBase.class);
        CommandBase<VdcActionParametersBase>command = new CommandBaseDummy(parameterMock);
        List<String> msgs = Arrays.asList(
                "ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM",
                "IRS_FAILED_RETRIEVING_SNAPSHOT_INFO");

        assertTrue("extractVariableDeclarations didn't return the same static messages",
                CollectionUtils.isEqualCollection(msgs, command.extractVariableDeclarations(msgs)));
    }

    @Test
    public void testExtractVariableDeclarationsForDynamicMsgs() {
        VdcActionParametersBase parameterMock = mock(VdcActionParametersBase.class);
        CommandBase<VdcActionParametersBase>command = new CommandBaseDummy(parameterMock);
        String msg1_1 = "ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM";
        String msg1_2 = "$VmName Vm1";
        String msg2   = "IRS_FAILED_CREATING_SNAPSHOT";
        String msg3_1 = "ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION";
        String msg3_2 = "$VmName Vm2";
        String msg3_3 = "$SnapshotName Snapshot";
        List<String> appendedMsgs = Arrays.asList(
                new StringBuilder().append(msg1_1).append(msg1_2).toString(),
                msg2,
                new StringBuilder().append(msg3_1).append(msg3_2).append(msg3_3).toString());
        List<String> extractedMsgs = Arrays.asList(msg1_1, msg1_2, msg2, msg3_1, msg3_2, msg3_3);

        assertTrue("extractVariableDeclarations didn't extract the variables as expected",
                CollectionUtils.isEqualCollection(extractedMsgs, command.extractVariableDeclarations(appendedMsgs)));
    }
}
