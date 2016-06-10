package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@NonTransactiveCommandAttribute
public class ManageGlusterServiceCommand extends GlusterCommandBase<GlusterServiceParameters> {
    private Guid clusterId;
    private Guid serverId;
    private ServiceType serviceType;
    private String actionType;
    private final List<String> errors = new ArrayList<>();

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
        this.clusterId = params.getClusterId();
        this.serverId = params.getServerId();
        this.serviceType = params.getServiceType();
        this.actionType = params.getActionType();
        if (serverId != null) {
            setVdsId(serverId);
        }
        if (clusterId != null) {
            setClusterId(clusterId);
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(manageActionDetailsMap.get(getParameters().getActionType()).getValidateMsg());
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_SERVICE);
    }

    @Override
    protected boolean validate() {
        clusterId = getParameters().getClusterId();
        serverId = getParameters().getServerId();
        serviceType = getParameters().getServiceType();
        actionType = getParameters().getActionType();

        if (!manageActionDetailsMap.keySet().contains(actionType)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_ACTION_TYPE);
        }

        if (Guid.isNullOrEmpty(clusterId) && Guid.isNullOrEmpty(serverId)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTERID_AND_SERVERID_BOTH_NULL);
        }

        if (!Guid.isNullOrEmpty(clusterId) && getGlusterUtils().getAllUpServers(clusterId).size() == 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_SERVERS_FOR_CLUSTER);
        }

        return true;
    }

    public GlusterServerServiceDao getGlusterServerServiceDao() {
        return DbFacade.getInstance().getGlusterServerServiceDao();
    }

    public GlusterServiceDao getGlusterServiceDao() {
        return DbFacade.getInstance().getGlusterServiceDao();
    }

    @Override
    protected void executeCommand() {
        if (!Guid.isNullOrEmpty(serverId)) {
            performActionForServicesOfServer();
        } else if (!Guid.isNullOrEmpty(clusterId)) {
            performActionForServicesOfCluster();
        }
        addCustomValue(GlusterConstants.SERVICE_TYPE, getParameters().getServiceType().name());
    }

    private List<String> getServiceList() {
        List<GlusterService> serviceList = getGlusterServiceDao().getByServiceType(serviceType);
        List<String> serviceListStr = new ArrayList<>();
        for (GlusterService srvc : serviceList) {
            serviceListStr.add(srvc.getServiceName());
        }

        return serviceListStr;
    }

    private List<Callable<Pair<VDS, VDSReturnValue>>> getCallableVdsCmdList() {
        List<VDS> servers = getGlusterUtils().getAllUpServers(clusterId);
        final List<String> serviceList = getServiceList();
        List<Callable<Pair<VDS, VDSReturnValue>>> commandList = new ArrayList<>();
        for (final VDS upServer : servers) {
            commandList.add(() -> {
                VDSReturnValue returnValue =
                        runVdsCommand(VDSCommandType.ManageGlusterService,
                                new GlusterServiceVDSParameters(upServer.getId(), serviceList, actionType));
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
                        new GlusterServiceVDSParameters(serverId, serviceList, actionType));

        setSucceeded(returnValue.getSucceeded());

        if (!getSucceeded()) {
            handleVdsError(getAuditLogTypeValue(), returnValue.getVdsError().getMessage());
        } else {
            updateService(serverId, (List<GlusterServerService>) returnValue.getReturnValue());
        }
    }

    private void updateService(Guid serverId, List<GlusterServerService> fetchedServerServices) {
        // form the list of service ids
        List<Guid> serviceIds = new ArrayList<>();
        for (GlusterService srvc : getGlusterServiceDao().getByServiceType(serviceType)) {
            serviceIds.add(srvc.getId());
        }

        for (GlusterServerService serverService : fetchedServerServices) {
            if (serviceIds.contains(serverService.getServiceId())) {
                serverService.setStatus(manageActionDetailsMap.get(actionType).getStatus());
                getGlusterServerServiceDao().updateByServerIdAndServiceType(serverService);
            } else {
                getGlusterServerServiceDao().save(serverService);
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
