package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.utils.BackendUtils;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CommandExecutor {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Config.<Integer>getValue(ConfigValues.CommandCoordinatorThreadPoolSize));
    private static final Log log = LogFactory.getLog(CommandExecutor.class);

    private final CommandCoordinator coco;
    private final Map<Guid, CommandCallBack> cmdCallBackMap = new ConcurrentHashMap<>();
    private Object LOCK = new Object();
    private volatile boolean cmdExecutorInitialized;

    CommandExecutor(CommandCoordinator coco) {
        this.coco = coco;
        SchedulerUtil scheduler = SchedulerUtilQuartzImpl.getInstance();
        scheduler.scheduleAFixedDelayJob(this, "invokeCallbackMethods", new Class[]{},
                new Object[]{}, Config.<Integer>getValue(ConfigValues.AsyncCommandPollingRateInSeconds),
                Config.<Integer>getValue(ConfigValues.AsyncCommandPollingRateInSeconds), TimeUnit.SECONDS);
    }

    @OnTimerMethodAnnotation("invokeCallbackMethods")
    public synchronized void invokeCallbackMethods() {
        initCommandExecutor();
        for (Guid cmdId : cmdCallBackMap.keySet()) {
            CommandCallBack callBack = cmdCallBackMap.get(cmdId);
            CommandStatus status = coco.getCommandStatus(cmdId);
            switch(status) {
                case FAILED:
                    callBack.onFailed(cmdId, coco.getChildCommandIds(cmdId));
                    coco.updateCallBackNotified(cmdId);
                    cmdCallBackMap.remove(cmdId);
                    break;
                case SUCCEEDED:
                    callBack.onSucceeded(cmdId, coco.getChildCommandIds(cmdId));
                    coco.updateCallBackNotified(cmdId);
                    cmdCallBackMap.remove(cmdId);
                    break;
                case ACTIVE_SYNC:
                    coco.retrieveCommand(cmdId).setCommandStatus(CommandStatus.FAILED_RESTARTED);
                    break;
                case ACTIVE:
                case ACTIVE_ASYNC:
                    callBack.doPolling(cmdId, coco.getChildCommandIds(cmdId));
                    break;
            }
        }
    }

    private void initCommandExecutor() {
        if (!cmdExecutorInitialized) {
            synchronized(LOCK) {
                if (!cmdExecutorInitialized) {
                    CommandBase<?> cmd;
                    for (CommandEntity cmdEntity : coco.getCommandsWithCallBackEnabled()) {
                        if (!cmdEntity.isCallBackNotified()) {
                            cmd = coco.retrieveCommand(cmdEntity.getId());
                            cmdCallBackMap.put(cmdEntity.getId(), cmd.getCallBack());
                        }
                    }
                    cmdExecutorInitialized = true;
                }
            }
        }
    }

    public Future<VdcReturnValueBase> executeAsyncCommand(final VdcActionType actionType,
                                                          final VdcActionParametersBase parameters) {
        final CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters);
        return executor.submit(new Callable<VdcReturnValueBase>() {

            @Override
            public VdcReturnValueBase call() throws Exception {
                return executeCommand(command);
            }
        });
    }

    private VdcReturnValueBase executeCommand(final CommandBase<?> command) {
        CommandCallBack callBack = command.getCallBack();
        if (callBack != null) {
            command.persistCommand(command.getParameters().getParentCommand(), true);
            cmdCallBackMap.put(command.getCommandId(), callBack);
        }
        VdcReturnValueBase result = BackendUtils.getBackendCommandObjectsHandler(log).runAction(command, null);
        if (callBack != null) {
            updateCommandStatus(command, result);
            callBack.executed(result);
        }
        return result;
    }

    private void clearCompletedCommands() {
        for (Guid cmdId : cmdCallBackMap.keySet()) {
            if (CommandStatus.SUCCEEDED.equals(coco.getCommandStatus(cmdId)) ||
                    CommandStatus.FAILED.equals(coco.getCommandStatus(cmdId))) {
                cmdCallBackMap.remove(cmdId);
            }
        }
    }

    private void updateCommandStatus(final CommandBase<?> command,
                                     final VdcReturnValueBase result) {
        if (!result.getCanDoAction()) {
            command.setCommandStatus(CommandStatus.FAILED);
            return;
        }
        if (CommandStatus.ACTIVE_SYNC.equals(coco.getCommandStatus(command.getCommandId()))) {
            command.setCommandStatus(result.getSucceeded() ? CommandStatus.SUCCEEDED : CommandStatus.FAILED);
        }
    }

}
