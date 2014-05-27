package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.interfaces.BackendCommandObjectsHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class CommandExecutor {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Config.<Integer>getValue(ConfigValues.CommandCoordinatorThreadPoolSize));
    private static final String BACKEND_COMMAND_OBJECTS_HANDLER_JNDI_NAME =
            "java:global/engine/bll/Backend!org.ovirt.engine.core.bll.interfaces.BackendCommandObjectsHandler";
    private static final Log log = LogFactory.getLog(CommandExecutor.class);

    private final CommandCoordinator coco;

    CommandExecutor(CommandCoordinator coco) {
        this.coco = coco;
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
        VdcReturnValueBase result = getBackendCommandObjectsHandler().runAction(command, null);
        if (callBack != null) {
            callBack.executed(result);
        }
        return result;
    }

    private BackendCommandObjectsHandler getBackendCommandObjectsHandler() {
        try {
            InitialContext ctx = new InitialContext();
            return (BackendCommandObjectsHandler) ctx.lookup(BACKEND_COMMAND_OBJECTS_HANDLER_JNDI_NAME);
        } catch (NamingException e) {
            log.error("Getting backend command objects handler failed" + e.getMessage());
            log.debug("", e);
            return null;
        }
    }

}
