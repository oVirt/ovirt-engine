package org.ovirt.engine.core.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;

public class SequentialMultipleActionsRunnerTest extends MultipleActionsRunnerBaseTest{

    private static CommandContext commandContext = CommandContext.createContext(new
            DiskProfileParameters().getSessionId()).withCompensationContext(NoOpCompensationContext.getInstance());

    @InjectMocks
    private MultipleActionsRunner runner = new SequentialMultipleActionsRunner(ActionType
            .RemoveDiskProfile,
            Arrays.asList(new DiskProfileParameters(), new DiskProfileParameters(), new DiskProfileParameters()),
            commandContext,
            true);


    @BeforeEach
    public void setUp() {
        runner.setIsWaitForResult(true);
    }

    @Test
    public void shouldAlwaysRunExecuteActionForEveryCommand() {
        TestCommand failingValidationCommand = failingValidationCommand();
        TestCommand successfulCommand = successfulCommand();
        TestCommand failingExecutionCommand = failingExecutionCommand();
        setUpFactory(failingValidationCommand, successfulCommand, failingExecutionCommand);

        runner.setIsRunOnlyIfAllValidatePass(false);
        runner.execute();

        verify(successfulCommand, times(1)).executeAction();
        verify(failingValidationCommand, times(1)).executeAction();
        verify(failingExecutionCommand, times(1)).executeAction();
    }

    @Test
    public void shouldCollectReturnValuesOfCommands() {
        setUpFactory(successfulCommand(), failingValidationCommand(), failingExecutionCommand());
        runner.setIsRunOnlyIfAllValidatePass(false);
        List<ActionReturnValue> returnValues = runner.execute();
        assertThat(returnValues).hasSize(3);
        // Command succeeds
        assertThat(returnValues.get(0).isValid()).isTrue();
        assertThat(returnValues.get(0).getSucceeded()).isTrue();
        // Validation fails
        assertThat(returnValues.get(1).isValid()).isFalse();
        assertThat(returnValues.get(1).getSucceeded()).isFalse();
        // Execution fails
        assertThat(returnValues.get(2).isValid()).isTrue();
        assertThat(returnValues.get(2).getSucceeded()).isFalse();

    }

}
