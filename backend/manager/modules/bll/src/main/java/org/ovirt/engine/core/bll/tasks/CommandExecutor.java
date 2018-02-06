package org.ovirt.engine.core.bll.tasks;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendCommandObjectsHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CommandExecutor {

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.CoCo)
    private ManagedExecutorService executor;

    @Inject
    private BackendCommandObjectsHandler actionRunner;
    @Inject
    private BackendInternal backend;

    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
    private final CommandsRepository commandsRepository;

    @Inject
    CommandExecutor(CommandsRepository commandsRepository) {
        this.commandsRepository = commandsRepository;
    }

    public Future<ActionReturnValue> executeAsyncCommand(final CommandBase<?> command,
                                                          final CommandContext cmdContext) {
        Future<ActionReturnValue> retVal;
        try {
            retVal = executor.submit(() -> executeCommand(command, cmdContext));
        } catch (RejectedExecutionException ex) {
            command.setCommandStatus(CommandStatus.FAILED);
            log.error("Failed to submit command to executor service, command '{}' status has been set to FAILED",
                    command.getCommandId());
            retVal = new RejectedExecutionFuture(createRejectedReturnValue());
        }
        return retVal;
    }

    private ActionReturnValue executeCommand(final CommandBase<?> command, final CommandContext cmdContext) {
        ActionReturnValue result = actionRunner.runAction(command,
                cmdContext != null ? cmdContext.getExecutionContext() : null);
        updateCommandResult(command.getCommandId(), result);
        return result;
    }

    private void updateCommandResult(final Guid commandId, final ActionReturnValue result) {
        CommandEntity cmdEntity = commandsRepository.getCommandEntity(commandId);
        cmdEntity.setReturnValue(result);
        if (!result.isValid()) {
            cmdEntity.setCommandStatus(CommandStatus.FAILED);
        }
        commandsRepository.persistCommand(cmdEntity);
    }

    private ActionReturnValue createRejectedReturnValue() {
        ActionReturnValue retValue = new ActionReturnValue();
        retValue.setSucceeded(false);
        EngineFault fault = new EngineFault();
        fault.setError(EngineError.ResourceException);
        fault.setMessage(backend.getVdsErrorsTranslator().translateErrorTextSingle(fault.getError().toString()));
        retValue.setFault(fault);
        return retValue;
    }

    static class RejectedExecutionFuture implements Future<ActionReturnValue> {

        ActionReturnValue retValue;

        RejectedExecutionFuture(ActionReturnValue retValue) {
            this.retValue = retValue;
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
        public ActionReturnValue get() {
            return retValue;
        }

        @Override
        public ActionReturnValue get(long timeout, TimeUnit unit) {
            return retValue;
        }
    }

}
