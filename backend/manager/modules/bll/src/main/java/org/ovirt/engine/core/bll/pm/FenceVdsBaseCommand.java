package org.ovirt.engine.core.bll.pm;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.PreviousHostedEngineHost;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDao;

public abstract class FenceVdsBaseCommand<T extends FenceVdsActionParameters> extends VdsCommand<T> {
    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private PreviousHostedEngineHost previousHostedEngineHost;
    @Inject
    private AuditLogDao auditLogDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private HostLocking hostLocking;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected FenceVdsBaseCommand(Guid commandId) {
        super(commandId);
    }

    public FenceVdsBaseCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        List<String> messages = getReturnValue().getValidationMessages();
        VDS host = getVds();
        boolean valid =
                fenceValidator.isHostExists(host, messages)
                        && fenceValidator.isPowerManagementEnabledAndLegal(host, getCluster(), messages)
                        && (previousHostedEngineHost.isPreviousHostId(host.getId())
                                || fenceValidator.isStartupTimeoutPassed(messages)
                                || host.isInFenceFlow())
                        && isQuietTimeFromLastActionPassed()
                        && fenceValidator.isProxyHostAvailable(host, messages);
        if (!valid) {
            handleError();
        }
        getReturnValue().setSucceeded(valid);
        return valid;
    }

    private boolean isQuietTimeFromLastActionPassed() {
        // Check Quiet time between PM operations, this is done only if command is not internal and parent
        // command is not <Restart>
        int secondsLeftToNextPmOp =
                (isInternalExecution() || (getParameters().getParentCommand() == ActionType.RestartVds))
                        ?
                        0
                        :
                        auditLogDao.getTimeToWaitForNextPmOp(getVds().getName(), getRequestedAuditEvent());
        if (secondsLeftToNextPmOp > 0) {
            addValidationMessage(EngineMessage.VDS_FENCE_DISABLED_AT_QUIET_TIME);
            addValidationMessageVariable("seconds", secondsLeftToNextPmOp);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void executeCommand() {
        log.info("Power-Management: {} of host '{}' initiated.", getAction(), getVdsName());
        audit(AuditLogType.FENCE_OPERATION_STARTED);
        VDSStatus lastStatus = getVds().getStatus();
        FenceOperationResult result = null;
        try {
            setup();
            result = createHostFenceActionExecutor(
                    getVds(),
                    getParameters().getFencingPolicy()
            ).fence(getAction());
            handleResult(result);
            if (getSucceeded()) {
                log.info("Power-Management: {} host '{}' succeeded.", getAction(), getVdsName());
                audit(AuditLogType.FENCE_OPERATION_SUCCEEDED);
            } else {
                log.info("Power-Management: {} host '{}' failed.", getAction(), getVdsName());
                audit(AuditLogType.FENCE_OPERATION_FAILED);
            }
        } finally {
            if (!getSucceeded()) {
                setStatus(lastStatus);
                if (result != null && result.getStatus() != Status.SKIPPED_DUE_TO_POLICY) {
                    // show alert only if command was not skipped due to fencing policy
                    alertIfPowerManagementOperationFailed();
                }
                throw new EngineException(EngineError.VDS_FENCE_OPERATION_FAILED);
            } else {
                teardown();
            }
        }
    }

    protected HostFenceActionExecutor createHostFenceActionExecutor(VDS fencedHost, FencingPolicy fencingPolicy) {
        return new HostFenceActionExecutor(fencedHost, fencingPolicy);
    }

    private void audit(AuditLogType auditMessage) {
        addCustomValue("Action", getAction().name().toLowerCase());
        addCustomValue("VdsName", getVds().getName());
        auditLogDirector.log(this, auditMessage);
    }

    private void handleResult(FenceOperationResult result) {
        switch (result.getStatus()) {
            case SKIPPED_DUE_TO_POLICY:
                // when fencing is skipped due to policy we want to suppress command result logging, because
                // we fire an alert in VdsNotRespondingTreatment
                setCommandShouldBeLogged(false);
                setSucceeded(false);
                break;

            case SUCCESS:
                handleSpecificCommandActions();
                setSucceeded(true);
                break;

            default:
                setSucceeded(false);
        }
        setActionReturnValue(result);
    }

    protected void setStatus() {
        setVdsStatus(VDSStatus.Reboot);
        runSleepOnReboot();
    }

    protected void setStatus(VDSStatus status) {
        // we need to load current status from db
        VdsDynamic currentHost = vdsDynamicDao.get(getVds().getId());
        if (currentHost != null && currentHost.getStatus() != status) {
            setVdsStatus(status);
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return hostLocking.getPowerManagementLock(getVdsId());
    }

    protected List<VM> getVmList() {
        return vmDao.getAllRunningForVds(getVdsId());
    }

    /**
     * get the event to look for in validate() , if we requested to start Host then we should look when we stopped it
     * and vice
     */
    protected abstract String getRequestedAuditEvent();

    protected abstract void handleError();

    protected abstract void setup();

    protected abstract void teardown();

    protected abstract void handleSpecificCommandActions();

    /**
     * Get the fence action
     */
    protected abstract FenceActionType getAction();

    @Override
    public Map<String, String> getJobMessageProperties() {
        Map<String, String> map = super.getJobMessageProperties();
        map.put(VdcObjectType.Cluster.name().toLowerCase(), getClusterName());
        return map;
    }
}
