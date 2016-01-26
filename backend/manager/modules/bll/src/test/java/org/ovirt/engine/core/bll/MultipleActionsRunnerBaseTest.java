package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.ClassRule;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

public class MultipleActionsRunnerBaseTest extends BaseCommandTest {

    private static CommandContext commandContext = CommandContext.createContext(new
            DiskProfileParameters().getSessionId()).withCompensationContext(NoOpCompensationContext.getInstance());

    @Mock
    private NestedCommandFactory commandFactory;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DefaultMinThreadPoolSize, 6),
            mockConfig(ConfigValues.DefaultMaxThreadPoolSize, 6),
            mockConfig(ConfigValues.DefaultMaxThreadWaitQueueSize, 10));

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    protected static class TestCommand extends CommandBase {

        private VdcReturnValueBase validationResult;

        public TestCommand(VdcReturnValueBase validationResult) {
            super(new DiskProfileParameters(), commandContext);
            this.validationResult = validationResult;
        }

        @Override
        public VdcReturnValueBase executeAction() {
            setReturnValue(validationResult);
            return validationResult;
        }

        @Override
        protected void executeCommand() {
            // needs to be implemented but is not called
        }

        @Override
        public List<PermissionSubject> getPermissionCheckSubjects() {
            // needs to be implemented but is not called
            return Lists.newArrayList();
        }

        @Override
        public VdcReturnValueBase validateOnly() {
            setReturnValue(validationResult);
            return validationResult;
        }
    }

    protected TestCommand successfulCommand() {
        return createCommand(true, true);
    }

    protected TestCommand failingExecutionCommand() {
        return createCommand(true, false);
    }

    protected TestCommand failingValidationCommand() {
        return createCommand(false, false);
    }

    protected TestCommand createCommand(boolean validationSucceeds, boolean executionSucceeds) {
        VdcReturnValueBase returnValueBase = new VdcReturnValueBase();
        returnValueBase.setValid(validationSucceeds);
        returnValueBase.setSucceeded(executionSucceeds);
        TestCommand command = new TestCommand(returnValueBase);
        command.setAuditLogDirector(mock(AuditLogDirector.class));
        return spy(command);
    }

    protected void setUpFactory(TestCommand... commands) {
        OngoingStubbing stubbing =
                when(commandFactory.createWrappedCommand(any(CommandContext.class),
                        any(VdcActionType.class),
                        any(VdcActionParametersBase.class),
                        anyBoolean()));
        for (TestCommand command : commands) {
            stubbing = stubbing.thenReturn(command);
        }
    }

}
