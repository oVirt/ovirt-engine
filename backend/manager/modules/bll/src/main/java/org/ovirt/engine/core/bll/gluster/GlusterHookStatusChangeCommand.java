package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * BLL command to enable Gluster hook
 */
@NonTransactiveCommandAttribute
public abstract class GlusterHookStatusChangeCommand<T extends GlusterHookParameters> extends GlusterHookCommandBase<T> {

    @Inject
    private GlusterHooksDao glusterHooksDao;

    @Inject
    private ClusterUtils clusterUtils;

    protected List<String> errors = new ArrayList<>();

    private List<VDS> upServers = null;

    public GlusterHookStatusChangeCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    private List<VDS> getAllUpServers() {
        if (upServers == null) {
            upServers = getAllUpServers(getGlusterHook().getClusterId());
        }
        return upServers;
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getAllUpServers() == null || getAllUpServers().isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        entity = getGlusterHook();
        addCustomValue(GlusterConstants.HOOK_NAME, entity.getName());

        if (getAllUpServers().size() < clusterUtils.getServerCount(getGlusterHook().getClusterId())) {
            errors.add(EngineMessage.CLUSTER_ALL_SERVERS_NOT_UP.toString());
        }

        List<Callable<Pair<VDS, VDSReturnValue>>> taskList = new ArrayList<>();
        for (final VDS upServer : getAllUpServers()) {
            taskList.add(() -> {
                VDSReturnValue returnValue =
                        runVdsCommand(
                                getStatusChangeVDSCommand(),
                                new GlusterHookVDSParameters(upServer.getId(),
                                        entity.getGlusterCommand(),
                                        entity.getStage(),
                                        entity.getName()));
                return new Pair<>(upServer, returnValue);
            });
        }
        boolean atLeastOneSuccess = false;
        List<Pair<VDS, VDSReturnValue>> pairResults = ThreadPoolUtil.invokeAll(taskList);
        for (Pair<VDS, VDSReturnValue> pairResult : pairResults) {

            VDSReturnValue retValue = pairResult.getSecond();
            if (retValue.getSucceeded() ) {
                atLeastOneSuccess = true;
                // update status in database
                updateServerHookStatusInDb(getGlusterHook().getId(), pairResult.getFirst().getId(), getNewStatus());
            } else {
                errors.add(retValue.getVdsError().getMessage());
             }
        }

        setSucceeded(atLeastOneSuccess);

        if (errors.size() > 0) {
            // conflict in status
            entity.addStatusConflict();
            handleVdsErrors(getAuditLogTypeValue(), errors);
            addCustomValue(GlusterConstants.FAILURE_MESSAGE , StringUtils.join(errors, System.lineSeparator()));
        }

        //The intention was to enable/disable hook. So we update the entity with new status if command succeeded
        if (getSucceeded()) {
            entity.setStatus(getNewStatus());
            //no longer conflicts as all hooks have same status
            entity.removeStatusConflict();
            updateHookInDb(entity);
            if (entity.getConflictStatus() == 0) {
                //all conflicts have been resolved, remove server hooks
                glusterHooksDao.removeGlusterServerHooks(entity.getId());
            }
        }

    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            if (getGlusterHook() != null) {
                 jobProperties.put(GlusterConstants.HOOK_NAME, getGlusterHook().getName());
            }
        }

        return jobProperties;
    }

    protected abstract VDSCommandType getStatusChangeVDSCommand();

    protected abstract GlusterHookStatus getNewStatus();

}
