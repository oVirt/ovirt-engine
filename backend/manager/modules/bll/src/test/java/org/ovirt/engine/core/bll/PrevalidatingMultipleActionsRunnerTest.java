package org.ovirt.engine.core.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
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

public class PrevalidatingMultipleActionsRunnerTest extends MultipleActionsRunnerBaseTest{

    private static CommandContext commandContext = CommandContext.createContext(new
            DiskProfileParameters().getSessionId()).withCompensationContext(NoOpCompensationContext.getInstance());

    @InjectMocks
    private MultipleActionsRunner runner = new PrevalidatingMultipleActionsRunner(ActionType
            .RemoveDiskProfile,
            Arrays.asList(new DiskProfileParameters(), new DiskProfileParameters(), new DiskProfileParameters()),
            commandContext,
            true);


    @BeforeEach
    public void setUp() {
        runner.setIsWaitForResult(true);
    }

    @Test
    public void shouldExecuteValidCommands() {
        TestCommand failingCommand = failingValidationCommand();
        TestCommand successfulCommand = successfulCommand();
        setUpFactory(failingCommand, successfulCommand, successfulCommand);

        runner.setIsRunOnlyIfAllValidatePass(false);
        runner.execute();

        verify(successfulCommand, times(2)).validateOnly();
        verify(failingCommand, times(1)).validateOnly();
        verify(successfulCommand, times(2)).executeAction();
        verify(failingCommand, never()).executeAction();
    }

    @Test
    public void shouldExecuteNoCommandsBecauseOneFails() {
        TestCommand failingCommand = failingValidationCommand();
        TestCommand successfulCommand = successfulCommand();
        setUpFactory(failingCommand, successfulCommand, successfulCommand);

        runner.setIsRunOnlyIfAllValidatePass(true);
        runner.execute();

        verify(successfulCommand, times(2)).validateOnly();
        verify(failingCommand, times(1)).validateOnly();
        verify(successfulCommand, never()).executeAction();
        verify(failingCommand, never()).executeAction();
    }

    @Test
    public void shouldExecuteMoreThanOneValidCommands() {
        TestCommand successfulCommand = successfulCommand();
        setUpFactory(successfulCommand, successfulCommand, successfulCommand);

        runner.setIsRunOnlyIfAllValidatePass(true);
        runner.execute();

        verify(successfulCommand, times(3)).validateOnly();
        verify(successfulCommand, times(3)).executeAction();
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
