package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.pm.PmHealthCheckManager;
import org.ovirt.engine.core.bll.provider.NetworkProviderValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.FenceValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.AddVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class VdsCommand<T extends VdsActionParameters> extends CommandBase<T> {

    protected String _failureMessage = null;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected VdsCommand(Guid commandId) {
        super(commandId);
    }

    public VdsCommand(T parameters) {
        this(parameters, null);
    }

    public VdsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(parameters.getVdsId());
    }

    protected void initializeVds() {
        initializeVds(false);
    }

    protected void initializeVds(boolean newHost) {
        runVdsCommand(VDSCommandType.RemoveVds,
                        new RemoveVdsVDSCommandParameters(getVdsId(), newHost));
        runVdsCommand(VDSCommandType.AddVds, new AddVdsVDSCommandParameters(getVdsId()));
    }

    @Override
    protected String getDescription() {
        return getVdsName();
    }

    protected void runSleepOnReboot() {
        runSleepOnReboot(VDSStatus.NonResponsive);
    }

    protected void runSleepOnReboot(final VDSStatus status) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                sleepOnReboot(status);
            }
        });
    }

    private void sleepOnReboot(final VDSStatus status) {
        int sleepTimeInSec = Config.<Integer> getValue(ConfigValues.ServerRebootTimeout);
        log.info("Waiting {} seconds, for server to finish reboot process.",
                sleepTimeInSec);
        ThreadUtils.sleep(sleepTimeInSec * 1000);
        runVdsCommand(VDSCommandType.SetVdsStatus,
                        new SetVdsStatusVDSCommandParameters(getVdsId(), status));
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     */
    private void Alert(AuditLogType logType) {
        Alert(logType, null);
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     * @param operation
     *            Operation name.
     */
    private void Alert(AuditLogType logType, String operation) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getVds().getId());
        String op = (operation == null) ? getActionType().name(): operation;
        alert.addCustomValue("Operation", op);
        AlertDirector.Alert(alert, logType);
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     * @param operation
     *            Operation name.
     * @param throwable
     *            Throwable object with exception details.
     */
    private void Alert(AuditLogType logType, String operation, Throwable throwable) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getVds().getId());
        String op = (operation == null) ? getActionType().name(): operation;
        alert.addCustomValue("Operation", op);
        alert.updateCallStackFromThrowable(throwable);
        AlertDirector.Alert(alert, logType);
    }

    /**
     * Alerts if power management not configured.
     *
     * @param vdsStatic
     *            The VDS static.
     */
    protected void alertIfPowerManagementNotConfigured(VdsStatic vdsStatic) {
        if (getVdsGroup() != null && !getVdsGroup().supportsVirtService()) {
            return;
        }

        if (!vdsStatic.isPmEnabled()) {
            Alert(AuditLogType.VDS_ALERT_FENCE_IS_NOT_CONFIGURED);
            // remove any test failure alerts
            AlertDirector.RemoveVdsAlert(vdsStatic.getId(),
                    AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
        } else {
            AlertDirector.RemoveVdsAlert(vdsStatic.getId(),
                    AuditLogType.VDS_ALERT_FENCE_IS_NOT_CONFIGURED);
        }
    }

    /**
     * Alerts if power management status failed.
     *
     * @param vdsStatic
     *            The VDS static.
     */
    protected void testVdsPowerManagementStatus(VdsStatic vdsStatic) {
        if (vdsStatic.isPmEnabled()) {
            PmHealthCheckManager.getInstance().pmHealthCheck(vdsStatic.getId());
        }
    }

    /**
     * Alerts if power management operation failed.
     */
    protected void alertIfPowerManagementOperationFailed() {
        Alert(AuditLogType.VDS_ALERT_FENCE_OPERATION_FAILED);
    }

    /**
     * Alerts if power management operation skipped.
     * @param operation The operation name.
     */
    protected void alertIfPowerManagementOperationSkipped(String operation, Throwable throwable) {
        Alert(AuditLogType.VDS_ALERT_FENCE_OPERATION_SKIPPED, operation, throwable);
    }

    protected void logSettingVmToDown(Guid vdsId, Guid vmId) {
        AuditLogableBase logable = new AuditLogableBase(vdsId, vmId);
        AuditLogDirector.log(logable,
                AuditLogType.VM_WAS_SET_DOWN_DUE_TO_HOST_REBOOT_OR_MANUAL_FENCE);
    }

    /**
     * Check if given agent is valid.
     * The check that is made is that 'user' and 'password' values are not empty.
     */
    protected boolean isFenceAgentValid(FenceAgent agent) {
        if (StringUtils.isEmpty(agent.getUser()) ||
                StringUtils.isEmpty(agent.getPassword())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT_CREDENTIALS);
            return false;
        } else {
            return true;
        }
    }

    protected boolean isPowerManagementLegal(boolean pmEnabled,
            List<FenceAgent> fenceAgents,
            String clusterCompatibilityVersion) {
        if (pmEnabled) {
            if (fenceAgents == null || fenceAgents.isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT);
                return false;
            }
            FenceValidator fenceValidator = new FenceValidator();
            for (FenceAgent agent : fenceAgents) {
                if (!fenceValidator.isFenceAgentVersionCompatible(agent,
                        clusterCompatibilityVersion,
                        getReturnValue().getCanDoActionMessages())
                        || !isFenceAgentValid(agent)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getVdsId(), VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }

    public void handleVdsError(VDSReturnValue returnValue) {
        getReturnValue().getFault().setError(returnValue.getVdsError().getCode());
        getReturnValue().getFault().setMessage(returnValue.getVdsError().getMessage());
        getReturnValue().getExecuteFailedMessages().add(returnValue.getVdsError().getMessage());
    }

    protected EngineLock acquireMonitorLock() {
        final VDS vds = getVds();
        EngineLock monitoringLock =
                new EngineLock(Collections.singletonMap(getParameters().getVdsId().toString(),
                        new Pair<String, String>(LockingGroup.VDS_INIT.name(), "")), null);
        log.info("Before acquiring lock in order to prevent monitoring for host '{}' from data-center '{}'",
                vds.getName(),
                vds.getStoragePoolName());
        getLockManager().acquireLockWait(monitoringLock);
        log.info("Lock acquired, from now a monitoring of host will be skipped for host '{}' from data-center '{}'",
                vds.getName(),
                vds.getStoragePoolName());

        return monitoringLock;
    }

    protected void logMonitorLockReleased(String commandName) {
        final VDS vds = getVds();
        log.info("{} finished. Lock released. Monitoring can run now for host '{}' from data-center '{}'",
                commandName,
                vds.getName(),
                vds.getStoragePoolName());
    }

    protected void handleError(Exception e, VDSStatus status) {
        log.error(
                "Host installation failed for host '{}', '{}': {}",
                getVds().getId(),
                getVds().getName(),
                e.getMessage());
        log.debug("Exception", e);
        setVdsStatus(status);
        setSucceeded(false);
        _failureMessage = e.getMessage();
    }

    /**
     * Set vds object status.
     *
     * @param status
     *            new status.
     */
    protected void setVdsStatus(VDSStatus status) {
        runVdsCommand(
                VDSCommandType.SetVdsStatus,
                new SetVdsStatusVDSCommandParameters(getVdsId(), status)
        );
    }

    protected String getErrorMessage(String msg) {
        return !StringUtils.isEmpty(msg) ? msg : String.format(
            "Please refer to %1$s/engine.log and log logs under %1$s/host-deploy/ for further details.",
            EngineLocalConfig.getInstance().getLogDir()
        );
    }

    @SuppressWarnings("serial")
    protected static class VdsInstallException extends RuntimeException {
        private VDSStatus status;

        VdsInstallException(VDSStatus status, String message) {
            super(message);
            this.status = status;
        }

        VdsInstallException(VDSStatus status, String message, Exception cause) {
            super(message, cause);
            this.status = status;
        }

        public VDSStatus getStatus() {
            return status;
        }
    }

    protected boolean validateNetworkProviderProperties(Guid providerId, String networkMappings) {
        NetworkProviderValidator validator = new NetworkProviderValidator(getProviderDao().get(providerId));
        return validate(validator.providerIsSet())
                && validate(validator.providerTypeValid())
                && validate(validator.networkMappingsProvided(networkMappings))
                && validate(validator.messagingBrokerProvided());
    }
}
