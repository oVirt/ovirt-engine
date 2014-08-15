package org.ovirt.engine.core.bll.tasks;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.bll.utils.BackendUtils;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class CommandExecutor {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Config.<Integer>getValue(ConfigValues.CommandCoordinatorThreadPoolSize));
    private static final Log log = LogFactory.getLog(CommandExecutor.class);

    private final CommandCoordinatorImpl coco;
    private final Map<Guid, CommandCallBack> cmdCallBackMap = new ConcurrentHashMap<>();
    private boolean cmdExecutorInitialized;

    CommandExecutor(CommandCoordinatorImpl coco) {
        this.coco = coco;
        SchedulerUtil scheduler = SchedulerUtilQuartzImpl.getInstance();
        scheduler.scheduleAFixedDelayJob(this, "invokeCallbackMethods", new Class[]{},
                new Object[]{}, Config.<Integer>getValue(ConfigValues.AsyncCommandPollingRateInSeconds),
                Config.<Integer>getValue(ConfigValues.AsyncCommandPollingRateInSeconds), TimeUnit.SECONDS);
    }

    @OnTimerMethodAnnotation("invokeCallbackMethods")
    public void invokeCallbackMethods() {
        initCommandExecutor();
        for (Iterator<Entry<Guid, CommandCallBack>> iterator = cmdCallBackMap.entrySet().iterator(); iterator.hasNext();) {
            Entry<Guid, CommandCallBack> entry = iterator.next();
            Guid cmdId = entry.getKey();
            CommandCallBack callBack = entry.getValue();
            CommandStatus status = coco.getCommandStatus(cmdId);
            boolean errorInCallback = false;
            try {
                switch (status) {
                    case FAILED:
                        callBack.onFailed(cmdId, coco.getChildCommandIds(cmdId));
                        break;
                    case SUCCEEDED:
                        callBack.onSucceeded(cmdId, coco.getChildCommandIds(cmdId));
                        break;
                    case ACTIVE:
                        if (coco.getCommandEntity(cmdId).isExecuted()) {
                            callBack.doPolling(cmdId, coco.getChildCommandIds(cmdId));
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                errorInCallback = true;
                handleError(ex, status, cmdId);
            } finally {
                if (CommandStatus.FAILED.equals(status) || (CommandStatus.SUCCEEDED.equals(status) && !errorInCallback)) {
                    coco.updateCallBackNotified(cmdId);
                    iterator.remove();
                }
            }
        }
    }

    private void handleError(Exception ex, CommandStatus status, Guid cmdId) {
        log.errorFormat("Error invoking callback method {0} for {1} command {2}",
                getCallBackMethod(status),
                status.toString(),
                cmdId.toString());
        log.error(ex);
        if (!CommandStatus.FAILED.equals(status)) {
            coco.updateCommandStatus(cmdId, CommandStatus.FAILED);
        }
    }

    private String getCallBackMethod(CommandStatus status) {
        switch (status) {
            case FAILED:
            case FAILED_RESTARTED:
                return "onFailed";
            case SUCCEEDED:
                return "onSucceeded";
            case ACTIVE:
                return "doPolling";
            default:
                return "Unknown";
        }
    }

    private void initCommandExecutor() {
        if (!cmdExecutorInitialized) {
            for (CommandEntity cmdEntity : coco.getCommandsWithCallBackEnabled()) {
                if (!cmdEntity.isExecuted() &&
                        cmdEntity.getCommandStatus() != CommandStatus.FAILED &&
                        cmdEntity.getCommandStatus() != CommandStatus.FAILED_RESTARTED) {
                    coco.retrieveCommand(cmdEntity.getId()).setCommandStatus(CommandStatus.FAILED_RESTARTED);
                }
                if (!cmdEntity.isCallBackNotified()) {
                    addToCallBackMap(cmdEntity);
                }
            }
            cmdExecutorInitialized = true;
        }
    }

    public void addToCallBackMap(CommandEntity cmdEntity) {
        if (!cmdCallBackMap.containsKey(cmdEntity.getId())) {
            CommandBase<?> cmd = coco.retrieveCommand(cmdEntity.getId());
            if (cmd != null && cmd.getCallBack() != null) {
                cmdCallBackMap.put(cmdEntity.getId(), cmd.getCallBack());
            }
        }
    }

    public Future<VdcReturnValueBase> executeAsyncCommand(final VdcActionType actionType,
                                    final VdcActionParametersBase parameters,
                                    final CommandContext cmdContext) {
        final CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, cmdContext);
        command.persistCommand(command.getParameters().getParentCommand(), cmdContext, true);
        CommandCallBack callBack = command.getCallBack();
        if (callBack != null) {
            cmdCallBackMap.put(command.getCommandId(), callBack);
        }
        Future<VdcReturnValueBase> retVal;
        try {
            retVal = executor.submit(new Callable<VdcReturnValueBase>() {

                @Override
                public VdcReturnValueBase call() throws Exception {
                    return executeCommand(command, cmdContext);
                }
            });
        } catch(RejectedExecutionException ex) {
            command.setCommandStatus(CommandStatus.FAILED);
            log.errorFormat("Failed to submit command to executor service, command {0} status has been set to FAILED",
                    command.getCommandId().toString());
            retVal = new RejectedExecutionFuture();
        }
        return retVal;
    }

    private VdcReturnValueBase executeCommand(final CommandBase<?> command, final CommandContext cmdContext) {
        CommandCallBack callBack = command.getCallBack();
        VdcReturnValueBase result = BackendUtils.getBackendCommandObjectsHandler(log).runAction(command, null);
        updateCommand(command, result);
        if (callBack != null) {
            callBack.executed(result);
        }
        return result;
    }

    private void updateCommand(final CommandBase<?> command,
                               final VdcReturnValueBase result) {
        CommandEntity cmdEntity = coco.getCommandEntity(command.getCommandId());
        cmdEntity.setReturnValue(result);
        if (!result.getCanDoAction()) {
            cmdEntity.setCommandStatus(CommandStatus.FAILED);
        }
        coco.persistCommand(cmdEntity);
    }

    static class RejectedExecutionFuture implements Future<VdcReturnValueBase> {

        VdcReturnValueBase retValue;

        RejectedExecutionFuture() {
            retValue = new VdcReturnValueBase();
            retValue.setSucceeded(false);
            VdcFault fault = new VdcFault();
            fault.setError(VdcBllErrors.ResourceException);
            fault.setMessage(Backend.getInstance()
                    .getVdsErrorsTranslator()
                    .TranslateErrorTextSingle(fault.getError().toString()));
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
