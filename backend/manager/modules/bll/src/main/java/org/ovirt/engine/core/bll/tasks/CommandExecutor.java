package org.ovirt.engine.core.bll.tasks;

import static org.ovirt.engine.core.bll.tasks.CommandsRepository.CommandContainer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.BackendUtils;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.CommandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CommandExecutor {

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(Config.<Integer>getValue(ConfigValues.CommandCoordinatorThreadPoolSize));
    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
    private final CommandsRepository commandsRepository;
    private final int pollingRate = Config.<Integer>getValue(ConfigValues.AsyncCommandPollingLoopInSeconds);

    @Inject
    CommandExecutor(CommandsRepository commandsRepository) {
        this.commandsRepository = commandsRepository;
    }

    public Future<VdcReturnValueBase> executeAsyncCommand(final VdcActionType actionType,
                                                          final VdcActionParametersBase parameters,
                                                          final CommandContext cmdContext) {
        final CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, cmdContext);
        CommandCallback callBack = command.getCallback();
        command.persistCommand(command.getParameters().getParentCommand(), cmdContext, callBack != null);
        if (callBack != null) {
            commandsRepository.getCommandsCallback().put(command.getCommandId(), new CommandContainer(callBack, pollingRate));
        }

        Future<VdcReturnValueBase> retVal;
        try {
            retVal = executor.submit(() -> executeCommand(command, cmdContext));
        } catch (RejectedExecutionException ex) {
            command.setCommandStatus(CommandStatus.FAILED);
            log.error("Failed to submit command to executor service, command '{}' status has been set to FAILED",
                    command.getCommandId());
            retVal = new RejectedExecutionFuture();
        }
        return retVal;
    }

    private VdcReturnValueBase executeCommand(final CommandBase<?> command, final CommandContext cmdContext) {
        VdcReturnValueBase result = BackendUtils.getBackendCommandObjectsHandler(log).runAction(command,
                cmdContext != null ? cmdContext.getExecutionContext() : null);
        updateCommand(command, result);
        return result;
    }

    private void updateCommand(final CommandBase<?> command,
                               final VdcReturnValueBase result) {
        CommandEntity cmdEntity = commandsRepository.getCommandEntity(command.getCommandId());
        cmdEntity.setReturnValue(result);
        if (!result.isValid()) {
            cmdEntity.setCommandStatus(CommandStatus.FAILED);
        }
        commandsRepository.persistCommand(cmdEntity);
    }

    static class RejectedExecutionFuture implements Future<VdcReturnValueBase> {

        VdcReturnValueBase retValue;

        RejectedExecutionFuture() {
            retValue = new VdcReturnValueBase();
            retValue.setSucceeded(false);
            EngineFault fault = new EngineFault();
            fault.setError(EngineError.ResourceException);
            fault.setMessage(Backend.getInstance()
                    .getVdsErrorsTranslator()
                    .translateErrorTextSingle(fault.getError().toString()));
            retValue.setFault(fault);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public VdcReturnValueBase get() throws InterruptedException, ExecutionException {
            return retValue;
        }

        @Override
        public VdcReturnValueBase get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return retValue;
        }
    }

}
