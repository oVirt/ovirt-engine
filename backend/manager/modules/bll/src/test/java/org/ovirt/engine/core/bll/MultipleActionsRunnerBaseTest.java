package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.assertj.core.util.Lists;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.DiskProfileParameters;

public class MultipleActionsRunnerBaseTest extends BaseCommandTest {
    private static CommandContext commandContext = CommandContext.createContext(new
            DiskProfileParameters().getSessionId()).withCompensationContext(NoOpCompensationContext.getInstance());

    @Mock
    private NestedCommandFactory commandFactory;

    protected static class TestCommand extends CommandBase {

        private ActionReturnValue validationResult;

        public TestCommand(ActionReturnValue validationResult) {
            super(new DiskProfileParameters(), commandContext);
            this.validationResult = validationResult;
        }

        @Override
        public ActionReturnValue executeAction() {
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
        public ActionReturnValue validateOnly() {
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
        ActionReturnValue returnValueBase = new ActionReturnValue();
        returnValueBase.setValid(validationSucceeds);
        returnValueBase.setSucceeded(executionSucceeds);
        TestCommand command = new TestCommand(returnValueBase);
        return spy(command);
    }

    protected void setUpFactory(TestCommand... commands) {
        OngoingStubbing stubbing = when(commandFactory.createWrappedCommand(any(), any(), any(), anyBoolean()));
        for (TestCommand command : commands) {
            stubbing = stubbing.thenReturn(command);
        }
    }

}
