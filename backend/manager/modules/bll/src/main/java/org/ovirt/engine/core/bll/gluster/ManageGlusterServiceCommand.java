package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@NonTransactiveCommandAttribute
public class ManageGlusterServiceCommand extends GlusterCommandBase<GlusterServiceParameters> {
    private final List<String> errors = new ArrayList<>();

    @Inject
    private GlusterServerServiceDao glusterServerServiceDao;
    @Inject
    private GlusterServiceDao glusterServiceDao;
    @Inject
    private GlusterServerDao glusterServerDao;
    @Inject
    private GlusterUtil glusterUtil;


    private static final Map<String, ManageActionDetail> manageActionDetailsMap = new HashMap<>();

    static {
        manageActionDetailsMap.put(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START,
                new ManageActionDetail(EngineMessage.VAR__ACTION__START,
                        GlusterServiceStatus.RUNNING,
                        AuditLogType.GLUSTER_SERVICE_STARTED,
                        AuditLogType.GLUSTER_SERVICE_START_FAILED));
        manageActionDetailsMap.put(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP,
                new ManageActionDetail(EngineMessage.VAR__ACTION__STOP,
                        GlusterServiceStatus.STOPPED,
                        AuditLogType.GLUSTER_SERVICE_STOPPED,
                        AuditLogType.GLUSTER_SERVICE_STOP_FAILED));
        manageActionDetailsMap.put(GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART,
                new ManageActionDetail(EngineMessage.VAR__ACTION__RESTART,
                        GlusterServiceStatus.RUNNING,
                        AuditLogType.GLUSTER_SERVICE_RESTARTED,
                        AuditLogType.GLUSTER_SERVICE_RESTART_FAILED));
    }

    public ManageGlusterServiceCommand(GlusterServiceParameters params, CommandContext commandContext) {
        super(params, commandContext);
        setVdsId(params.getServerId());
        setClusterId(params.getClusterId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(manageActionDetailsMap.get(getParameters().getActionType()).getValidateMsg());
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_SERVICE);
    }

    @Override
    protected boolean validate() {
        if (!manageActionDetailsMap.keySet().contains(getParameters().getActionType())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_ACTION_TYPE);
        }

        if (Guid.isNullOrEmpty(getClusterId()) && Guid.isNullOrEmpty(getParameters().getServerId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTERID_AND_SERVERID_BOTH_NULL);
        }

        /*
         * only check for upserver if this is a cluster wide service change.
         * On a server level change, this validation leads to chicken-egg if glusterd is not running
         */
        if (!Guid.isNullOrEmpty(getClusterId()) && Guid.isNullOrEmpty(getParameters().getServerId())
                && glusterUtil.getAllUpServers(getClusterId()).size() == 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_SERVERS_FOR_CLUSTER);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (!Guid.isNullOrEmpty(getParameters().getServerId())) {
            performActionForServicesOfServer();
        } else if (!Guid.isNullOrEmpty(getClusterId())) {
            performActionForServicesOfCluster();
        }
        addCustomValue(GlusterConstants.SERVICE_TYPE, getParameters().getServiceType().name());
    }

    private List<String> getServiceList() {
        List<GlusterService> serviceList = glusterServiceDao.getByServiceType(getParameters().getServiceType());
        List<String> serviceListStr = new ArrayList<>();
        for (GlusterService srvc : serviceList) {
            serviceListStr.add(srvc.getServiceName());
        }

        return serviceListStr;
    }

    private List<Callable<Pair<VDS, VDSReturnValue>>> getCallableVdsCmdList() {
        List<VDS> servers = glusterUtil.getAllUpServers(getClusterId());
        final List<String> serviceList = getServiceList();
        List<Callable<Pair<VDS, VDSReturnValue>>> commandList = new ArrayList<>();
        for (final VDS upServer : servers) {
            commandList.add(() -> {
                VDSReturnValue returnValue =
                        runVdsCommand(VDSCommandType.ManageGlusterService,
                                new GlusterServiceVDSParameters(
                                        upServer.getId(), serviceList, getParameters().getActionType()));
                Pair<VDS, VDSReturnValue> pairRetVal = new Pair<>(upServer, returnValue);
                if (returnValue.getSucceeded()) {
                    updateService(upServer.getId(), (List<GlusterServerService>) returnValue.getReturnValue());
                } else {
                    errors.add(returnValue.getVdsError().getMessage());
                }
                return pairRetVal;
            });
        }

        return commandList;
    }

    private void invokeManageGlusterService(List<Callable<Pair<VDS, VDSReturnValue>>> commandList) {
        if (commandList.isEmpty()) {
            return;
        }
        ThreadPoolUtil.invokeAll(commandList);
        if (errors.size() > 0) {
            setSucceeded(false);
            handleVdsErrors(getAuditLogTypeValue(), errors);
            addCustomValue(GlusterConstants.FAILURE_MESSAGE, StringUtils.join(errors, System.lineSeparator()));
        } else {
            setSucceeded(true);
        }
    }

    private void performActionForServicesOfCluster() {
        List<Callable<Pair<VDS, VDSReturnValue>>> commandList = getCallableVdsCmdList();

        if (!commandList.isEmpty()) {
            invokeManageGlusterService(commandList);
        } else {
            setSucceeded(false);
        }
    }

    private void performActionForServicesOfServer() {
        List<String> serviceList = getServiceList();

        VDSReturnValue returnValue = null;
        returnValue =
                runVdsCommand(VDSCommandType.ManageGlusterService,
                        new GlusterServiceVDSParameters(
                                getParameters().getServerId(), serviceList, getParameters().getActionType()));

        setSucceeded(returnValue.getSucceeded());

        if (!getSucceeded()) {
            handleVdsError(getAuditLogTypeValue(), returnValue.getVdsError().getMessage());
        } else {
            updateService(getParameters().getServerId(), (List<GlusterServerService>) returnValue.getReturnValue());
            //if glusterd was restarted, update peer status and host status
            if (getParameters().getServiceType() == ServiceType.GLUSTER
                    && (GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART
                            .equals(getParameters().getActionType())
                    || GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START
                            .equals(getParameters().getActionType()))) {
                glusterServerDao.updatePeerStatus(getParameters().getServerId(), PeerStatus.CONNECTED);
                //only if cluster supports only gluster service
                if (!getCluster().supportsVirtService()) {
                    runVdsCommand(VDSCommandType.SetVdsStatus,  new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Initializing));
                }
            }
        }
    }

    private void updateService(Guid serverId, List<GlusterServerService> fetchedServerServices) {
        // form the list of service ids
        List<Guid> serviceIds = new ArrayList<>();
        for (GlusterService srvc : glusterServiceDao.getByServiceType(getParameters().getServiceType())) {
            serviceIds.add(srvc.getId());
        }

        for (GlusterServerService serverService : fetchedServerServices) {
            if (serviceIds.contains(serverService.getServiceId())) {
                serverService.setStatus(manageActionDetailsMap.get(getParameters().getActionType()).getStatus());
                glusterServerServiceDao.updateByServerIdAndServiceType(serverService);
            } else {
                glusterServerServiceDao.save(serverService);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return manageActionDetailsMap.get(getParameters().getActionType()).getActionPerformedActionLog();
        } else {
            return manageActionDetailsMap.get(getParameters().getActionType()).getActionFailedActionLog();
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!Guid.isNullOrEmpty(getParameters().getServerId())) {
            return Collections.singletonMap(getParameters().getServerId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        } else if (!Guid.isNullOrEmpty(getParameters().getClusterId())) {
            return Collections.singletonMap(getParameters().getClusterId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }

        return null;
    }

    /**
     * An instance of this class holds various details about the managed actions
     */
    static class ManageActionDetail {
        private EngineMessage validateMsg;
        private GlusterServiceStatus status;
        private AuditLogType actionPerformedActionLog;
        private AuditLogType actionFailedActionLog;

        public ManageActionDetail(EngineMessage validateMsg,
                GlusterServiceStatus status,
                AuditLogType actionPerformedActionLog,
                AuditLogType actionFailedActionLog) {
            this.validateMsg = validateMsg;
            this.status = status;
            this.actionPerformedActionLog = actionPerformedActionLog;
            this.actionFailedActionLog = actionFailedActionLog;
        }

        public EngineMessage getValidateMsg() {
            return validateMsg;
        }

        public void setValidateMsg(EngineMessage validateMsg) {
            this.validateMsg = validateMsg;
        }

        public GlusterServiceStatus getStatus() {
            return status;
        }

        public void setStatus(GlusterServiceStatus status) {
            this.status = status;
        }

        public AuditLogType getActionPerformedActionLog() {
            return actionPerformedActionLog;
        }

        public void setActionPerformedActionLog(AuditLogType actionPerformedActionLog) {
            this.actionPerformedActionLog = actionPerformedActionLog;
        }

        public AuditLogType getActionFailedActionLog() {
            return actionFailedActionLog;
        }

        public void setActionFailedActionLog(AuditLogType actionFailedActionLog) {
            this.actionFailedActionLog = actionFailedActionLog;
        }
    }

}
