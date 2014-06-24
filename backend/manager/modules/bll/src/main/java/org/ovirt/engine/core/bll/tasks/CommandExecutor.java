package org.ovirt.engine.core.bll.tasks;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
        for (Iterator<Entry<Guid, CommandCallBack>> iterator = cmdCallBackMap.entrySet().iterator(); iterator.hasNext();) {
            Entry<Guid, CommandCallBack> entry = iterator.next();
            Guid cmdId = entry.getKey();
            CommandCallBack callBack = entry.getValue();
            CommandStatus status = coco.getCommandStatus(cmdId);

            switch (status) {
            case FAILED:
                callBack.onFailed(cmdId, coco.getChildCommandIds(cmdId));
                coco.updateCallBackNotified(cmdId);
                iterator.remove();
                break;
            case SUCCEEDED:
                callBack.onSucceeded(cmdId, coco.getChildCommandIds(cmdId));
                coco.updateCallBackNotified(cmdId);
                iterator.remove();
                break;
            case ACTIVE_SYNC:
                coco.retrieveCommand(cmdId).setCommandStatus(CommandStatus.FAILED_RESTARTED);
                break;
            case ACTIVE:
            case ACTIVE_ASYNC:
                callBack.doPolling(cmdId, coco.getChildCommandIds(cmdId));
                break;
            default:
                break;
            }
        }
    }

    private void initCommandExecutor() {
        if (!cmdExecutorInitialized) {
            synchronized(LOCK) {
                if (!cmdExecutorInitialized) {
                    for (CommandEntity cmdEntity : coco.getCommandsWithCallBackEnabled()) {
                        if (!cmdEntity.isCallBackNotified()) {
                            addToCallBackMap(cmdEntity);
                        }
                    }
                    cmdExecutorInitialized = true;
                }
            }
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
        command.persistCommand(command.getParameters().getParentCommand(), true);
        CommandCallBack callBack = command.getCallBack();
        if (callBack != null) {
            cmdCallBackMap.put(command.getCommandId(), callBack);
        }
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
        } else
        if (CommandStatus.ACTIVE_SYNC.equals(cmdEntity.getCommandStatus())) {
            cmdEntity.setCommandStatus(result.getSucceeded() ? CommandStatus.SUCCEEDED : CommandStatus.FAILED);
        }
        coco.persistCommand(cmdEntity);
    }

}
