package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterHookManageParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * BLL command to remove gluster hook from all servers in the cluster.
 */
@NonTransactiveCommandAttribute
public class RemoveGlusterHookCommand extends GlusterHookCommandBase<GlusterHookManageParameters> {

    protected List<String> errors = new ArrayList<String>();
    private List<VDS> serversInCluster = null;

    public RemoveGlusterHookCommand(GlusterHookManageParameters params) {
        super(params);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_HOOK);
    }

    private List<VDS> getServersInCluster() {
        if (serversInCluster == null) {
            serversInCluster = getVdsDAO().getAllForVdsGroup(getGlusterHook().getClusterId());
        }
        return serversInCluster;
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

       for (VDS vds: getServersInCluster()) {
           if (vds.getStatus() != VDSStatus.Up) {
                setVdsName(vds.getName());
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {

        entity = getGlusterHook();
        addCustomValue(GlusterConstants.HOOK_NAME, entity.getName());

        List<Callable<Pair<VDS, VDSReturnValue>>> taskList = new ArrayList<Callable<Pair<VDS, VDSReturnValue>>>();
        for (final VDS server : getServersInCluster()) {
            taskList.add(new Callable<Pair<VDS, VDSReturnValue>>() {
                @Override
                public Pair<VDS, VDSReturnValue> call() throws Exception {
                    VDSReturnValue returnValue;
                        returnValue =
                               runVdsCommand(
                                       VDSCommandType.RemoveGlusterHook,
                                       new GlusterHookVDSParameters(server.getId(),
                                               entity.getGlusterCommand(),
                                               entity.getStage(),
                                               entity.getName()
                                               ));
                     return new Pair<VDS, VDSReturnValue>(server, returnValue);

                }
            });
        }

        if (!taskList.isEmpty()) {
            List<Pair<VDS, VDSReturnValue>> pairResults = ThreadPoolUtil.invokeAll(taskList);
            for (Pair<VDS, VDSReturnValue> pairResult : pairResults) {

                VDSReturnValue retValue = pairResult.getSecond();
                if (!retValue.getSucceeded() ) {
                    errors.add(retValue.getVdsError().getMessage());
                }
            }
        }

        if (errors.size() > 0) {
            setSucceeded(false);
            errorType =  AuditLogType.GLUSTER_HOOK_REMOVE_FAILED;
            handleVdsErrors(getAuditLogTypeValue(), errors);
            addCustomValue(GlusterConstants.FAILURE_MESSAGE , StringUtils.join(errors, SystemUtils.LINE_SEPARATOR));
        } else {
            setSucceeded(true);
        }

        if (getSucceeded()) {
            entity.removeMissingConflict();
            getGlusterHooksDao().remove(entity.getId());
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

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.GLUSTER_HOOK_REMOVED : errorType;
    }

}
