package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * BLL command to enable Gluster hook
 */
@NonTransactiveCommandAttribute
@LockIdNameAttribute(isWait = true)
public abstract class GlusterHookStatusChangeCommand extends GlusterHookCommandBase<GlusterHookParameters> {
    private GlusterHookEntity entity;
    protected List<String> errors = new ArrayList<String>();

    public GlusterHookStatusChangeCommand(GlusterHookParameters params) {
        super(params);
        setVdsGroupId(params.getClusterId());
    }

    protected GlusterHookEntity getGlusterHook() {
        if (entity == null) {
            entity = getGlusterHooksDao().getById(getParameters().getHookId(),true);
        }
        return entity;
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }
        if (getParameters().getClusterId() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID);
            return false;
        }

        if (Guid.isNullOrEmpty(getParameters().getHookId())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED);
            return false;
        }

        if (getGlusterHooksDao().getById(getParameters().getHookId()) == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST);
            return false;
        }

        List <VDS> servers = getAllUpServers(getParameters().getClusterId());
        if (servers == null || servers.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        entity = getGlusterHook();
        addCustomValue(GlusterConstants.HOOK_NAME, entity.getName());
        List <VDS> servers = getAllUpServers(getParameters().getClusterId());
        List<GlusterServerHook> serverHooks = entity.getServerHooks();

        if (servers.size() < getClusterUtils().getServerCount(getParameters().getClusterId())) {
            errors.add(VdcBllMessages.CLUSTER_ALL_SERVERS_NOT_UP.toString());
        }

        List<Callable<Pair<VDS, VDSReturnValue>>> taskList = new ArrayList<Callable<Pair<VDS, VDSReturnValue>>>();
        for (final VDS upServer : servers) {
            taskList.add(new Callable<Pair<VDS, VDSReturnValue>>() {
                @Override
                public Pair<VDS, VDSReturnValue> call() throws Exception {
                    VDSReturnValue returnValue =
                            runVdsCommand(
                                    getStatusChangeVDSCommand(),
                                    new GlusterHookVDSParameters(upServer.getId(),
                                            entity.getGlusterCommand(),
                                            entity.getStage(),
                                            entity.getName()));
                    return new Pair<VDS, VDSReturnValue>(upServer, returnValue);
                }
            });
        }
        boolean atLeastOneSuccess = false;
        List<Pair<VDS, VDSReturnValue>> pairResults = ThreadPoolUtil.invokeAll(taskList);
        for (Pair<VDS, VDSReturnValue> pairResult : pairResults) {

            VDSReturnValue retValue = pairResult.getSecond();
            if (retValue.getSucceeded() ) {
                atLeastOneSuccess = true;
                // update status in database
                addOrUpdateServerHook(serverHooks, pairResult);
            } else {
                errors.add(retValue.getVdsError().getMessage());
             }
        }

        setSucceeded(atLeastOneSuccess);

        if (errors.size() > 0) {
            // conflict in status
            entity.addStatusConflict();
            handleVdsErrors(getAuditLogTypeValue(), errors);
            addCustomValue(GlusterConstants.FAILURE_MESSAGE , StringUtils.join(errors, SystemUtils.LINE_SEPARATOR));
        }

        //The intention was to enable/disable hook. So we update the entity with new status if command succeeded
        if (getSucceeded()) {
            entity.setStatus(getNewStatus());
            updateHookInDb(entity);
        }

    }

    private void addOrUpdateServerHook(List<GlusterServerHook> serverHooks, Pair<VDS, VDSReturnValue> pairResult) {
        if (getServerHookFromList(serverHooks, pairResult.getFirst().getId()) == null) {
           // if a new server has been detected, the hook entry needs to be added
            addServerHook(pairResult.getFirst().getId());
        } else {
            updateServerHookStatusInDb(getGlusterHook().getId(), pairResult.getFirst().getId(), getNewStatus());
        }
    }

    private void addServerHook(Guid serverId) {
        GlusterServerHook newServerHook = new GlusterServerHook();
        newServerHook.setHookId(getGlusterHook().getId());
        newServerHook.setStatus(getNewStatus());
        newServerHook.setServerId(serverId);
        addServerHookInDb(newServerHook);
    }

    private GlusterServerHook getServerHookFromList(List<GlusterServerHook> serverHooks, Guid serverId) {
        for (GlusterServerHook serverHook: serverHooks) {
            if (serverHook.getServerId().equals(serverId)) {
                return serverHook;
            }
        }
        return null;
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
