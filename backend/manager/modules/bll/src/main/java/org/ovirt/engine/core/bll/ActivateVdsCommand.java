package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class ActivateVdsCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    @Inject
    private NetworkClusterHelper networkClusterHelper;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private GlusterUtil glusterUtil;
    public ActivateVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected ActivateVdsCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {

        final VDS vds = getVds();
        try (EngineLock monitoringLock = acquireMonitorLock("Activate host")) {
            executionHandler.updateSpecificActionJobCompleted(vds.getId(), ActionType.MaintenanceVds, false);
            setSucceeded(setVdsStatus(VDSStatus.Unassigned).getSucceeded());

            if (getSucceeded()) {
                TransactionSupport.executeInNewTransaction(() -> {
                    // set network to operational / non-operational
                    List<Network> networks = networkDao.getAllForCluster(vds.getClusterId());
                    networkClusterHelper.setStatus(vds.getClusterId(), networks);
                    return null;
                });

                // Start glusterd service on the node, which would haven been stopped due to maintenance
                if (vds.getClusterSupportsGlusterService()) {
                    // Check gluster service running status
                    GlusterStatus isGlusterRunning = glusterUtil.isGlusterRunning(vds.getId());
                    switch(isGlusterRunning) {
                    case DOWN:
                        log.info("Gluster service on host '{}' is down, starting it",
                                vds.getHostName());
                        runVdsCommand(VDSCommandType.ManageGlusterService,
                                new GlusterServiceVDSParameters(vds.getId(), Arrays.asList("glusterd"), "start"));
                        break;
                    case UP:
                        log.debug("Gluster service on host '{}' is up, continuing",
                                vds.getHostName());
                           break;
                    case UNKNOWN:
                        log.warn("Gluster service on host '{}' has some issues, trying to restart it",
                                vds.getHostName());
                        runVdsCommand(VDSCommandType.ManageGlusterService,
                                new GlusterServiceVDSParameters(vds.getId(), Arrays.asList("glusterd"), "restart"));
                        break;
                    }

                    // starting vdo service
                    GlusterStatus isRunning = glusterUtil.isVDORunning(vds.getId());
                    switch (isRunning) {
                    case DOWN:
                        log.info("VDO service is down in host : '{}' with id '{}', starting VDO service",
                                vds.getHostName(),
                                vds.getId());
                        startVDOService(vds);
                        break;
                    case UP:
                        log.info("VDO service is up in host : '{}' with id '{}', skipping starting of VDO service",
                                vds.getHostName(),
                                vds.getId());
                           break;
                    case UNKNOWN:
                        log.info("VDO service is not installed host : '{}' with id '{}', ignoring to start VDO service",
                                vds.getHostName(),
                                vds.getId());
                        break;
                    }

                }
            }
        }
    }

    @Override
    protected boolean validate() {
        HostValidator validator = HostValidator.createInstance(getVds());
        return validate(validator.hostExists()) &&
                validate(validator.validateStatusForActivation()) &&
                validate(validator.validateUniqueId());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ACTIVATE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().isRunSilent()) {
            return getSucceeded() ? AuditLogType.VDS_ACTIVATE_ASYNC : AuditLogType.VDS_ACTIVATE_FAILED_ASYNC;
        } else {
            return getSucceeded() ? AuditLogType.VDS_ACTIVATE : AuditLogType.VDS_ACTIVATE_FAILED;
        }
    }
    public void startVDOService(VDS vds) {
        // starting VDO service
        boolean succeeded = runVdsCommand(VDSCommandType.ManageGlusterService,
                new GlusterServiceVDSParameters(vds.getId(), Arrays.asList("vdo"), "restart")).getSucceeded();
        if (!succeeded) {
            log.error("Failed to start VDO service while activating the host '{}' with id '{}'",
                    vds.getHostName(),
                    vds.getId());
        }
    }
}
