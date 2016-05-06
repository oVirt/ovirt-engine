package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.pm.PmHealthCheckManager;
import org.ovirt.engine.core.bll.provider.NetworkProviderValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.FenceValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.AddVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public abstract class VdsCommand<T extends VdsActionParameters> extends CommandBase<T> {

    protected String _failureMessage = null;

    @Inject
    private PmHealthCheckManager pmHealthCheckManager;

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private HostLocking hostLocking;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected VdsCommand(Guid commandId) {
        super(commandId);
    }

    public VdsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(parameters.getVdsId());
    }

    protected CpuFlagsManagerHandler getCpuFlagsManagerHandler() {
        return cpuFlagsManagerHandler;
    }

    protected void initializeVds() {
        initializeVds(false);
    }

    protected void initializeVds(boolean newHost) {
        runVdsCommand(VDSCommandType.RemoveVds,
                        new RemoveVdsVDSCommandParameters(getVdsId(), newHost));
        runVdsCommand(VDSCommandType.AddVds, new AddVdsVDSCommandParameters(getVdsId()));
    }

    /**
     * Updates the host protocol to {@code VdsProtocol.STOMP} and reestablish connections according to the
     * updated protocol
     */
    protected void reestablishConnectionIfNeeded() {
        VDS host = getVds();
        if (host.getProtocol() == VdsProtocol.XML && host.getHostOs() != null) {
            VdsStatic hostStaticData = host.getStaticData();
            hostStaticData.setProtocol(VdsProtocol.STOMP);
            getVdsStaticDao().update(hostStaticData);
            resourceManager.reestablishConnection(getVdsId());
        }
    }

    @Override
    protected String getDescription() {
        return getVdsName();
    }

    protected void runSleepOnReboot() {
        runSleepOnReboot(VDSStatus.NonResponsive);
    }

    protected void runSleepOnReboot(final VDSStatus status) {
        ThreadPoolUtil.execute(() -> sleepOnReboot(status));
    }

    private void sleepOnReboot(final VDSStatus status) {
        int sleepTimeInSec = Config.<Integer> getValue(ConfigValues.ServerRebootTimeout);
        log.info("Waiting {} seconds, for server to finish reboot process.",
                sleepTimeInSec);
        ThreadUtils.sleep(TimeUnit.SECONDS.toMillis(sleepTimeInSec));
        setVdsStatus(status);
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     */
    private void alert(AuditLogType logType) {
        alert(logType, null);
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     * @param operation
     *            Operation name.
     */
    private void alert(AuditLogType logType, String operation) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getVds().getId());
        String op = (operation == null) ? getActionType().name(): operation;
        alert.addCustomValue("Operation", op);
        AlertDirector.alert(alert, logType, auditLogDirector);
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
    private void alert(AuditLogType logType, String operation, Throwable throwable) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getVds().getId());
        String op = (operation == null) ? getActionType().name(): operation;
        alert.addCustomValue("Operation", op);
        alert.updateCallStackFromThrowable(throwable);
        AlertDirector.alert(alert, logType, auditLogDirector);
    }

    /**
     * Alerts if power management not configured.
     *
     * @param vdsStatic
     *            The VDS static.
     */
    protected void alertIfPowerManagementNotConfigured(VdsStatic vdsStatic) {
        if (getCluster() != null && !getCluster().supportsVirtService()) {
            return;
        }

        if (!vdsStatic.isPmEnabled()) {
            alert(AuditLogType.VDS_ALERT_FENCE_IS_NOT_CONFIGURED);
            // remove any test failure alerts
            AlertDirector.removeVdsAlert(vdsStatic.getId(),
                    AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
        } else {
            AlertDirector.removeVdsAlert(vdsStatic.getId(),
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
            pmHealthCheckManager.pmHealthCheck(vdsStatic.getId());
        }
    }

    /**
     * Alerts if power management operation failed.
     */
    protected void alertIfPowerManagementOperationFailed() {
        alert(AuditLogType.VDS_ALERT_FENCE_OPERATION_FAILED);
    }

    /**
     * Alerts if power management operation skipped.
     * @param operation The operation name.
     */
    protected void alertIfPowerManagementOperationSkipped(String operation, Throwable throwable) {
        alert(AuditLogType.VDS_ALERT_FENCE_OPERATION_SKIPPED, operation, throwable);
    }

    protected void logSettingVmToDown(Guid vdsId, Guid vmId) {
        AuditLogableBase logable = new AuditLogableBase(vdsId, vmId);
        auditLogDirector.log(logable,
                AuditLogType.VM_WAS_SET_DOWN_DUE_TO_HOST_REBOOT_OR_MANUAL_FENCE);
    }

    /**
     * Check if given agent is valid.
     * The check that is made is that 'user' and 'password' values are not empty.
     */
    protected boolean isFenceAgentValid(FenceAgent agent) {
        if (StringUtils.isEmpty(agent.getUser()) ||
                StringUtils.isEmpty(agent.getPassword())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT_CREDENTIALS);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if power management(pm) parameters are legal.
     * Parameters are valid if:
     *  1) user don't have pm enabled and fence agents are null.
     *  2) user has pm enabled and send valid non-empty list of fence agents
     * Parameters aren't valid if:
     *  1) user has pm enabled and send empty list of fence agents
     */
    protected boolean isPowerManagementLegal(boolean pmEnabled,
            List<FenceAgent> fenceAgents,
            String clusterCompatibilityVersion) {
        if (pmEnabled && fenceAgents != null) {
            if (fenceAgents.isEmpty()) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT);
                return false;
            }
            FenceValidator fenceValidator = new FenceValidator();
            for (FenceAgent agent : fenceAgents) {
                if (!fenceValidator.isFenceAgentVersionCompatible(agent,
                        clusterCompatibilityVersion,
                        getReturnValue().getValidationMessages())
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

    public EngineLock acquireMonitorLock(String lockReleaseMessage) {
        return this.hostLocking.acquireMonitorLock(getVds(), lockReleaseMessage, log);
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

    protected VDSReturnValue setVdsStatus(VDSStatus status) {
        SetVdsStatusVDSCommandParameters parameters = new SetVdsStatusVDSCommandParameters(getVdsId(), status);
        return invokeSetHostStatus(parameters);
    }

    protected VDSReturnValue setVdsStatus(VDSStatus status, NonOperationalReason reason) {
        SetVdsStatusVDSCommandParameters parameters = new SetVdsStatusVDSCommandParameters(getVdsId(), status, reason);
        return invokeSetHostStatus(parameters);
    }

    private VDSReturnValue invokeSetHostStatus(SetVdsStatusVDSCommandParameters parameters) {
        return runVdsCommand(VDSCommandType.SetVdsStatus, parameters);
    }

    protected String getErrorMessage(String msg) {
        return !StringUtils.isEmpty(msg) ? msg : String.format(
            "Please refer to %1$s/engine.log and log logs under %1$s/host-deploy/ for further details.",
            EngineLocalConfig.getInstance().getLogDir()
        );
    }

    protected boolean validateNetworkProviderProperties(Guid providerId, String networkMappings) {
        NetworkProviderValidator validator = new NetworkProviderValidator(getProviderDao().get(providerId));
        return validate(validator.providerIsSet())
                && validate(validator.providerTypeValid())
                && validate(validator.networkMappingsProvided(networkMappings))
                && validate(validator.messagingBrokerProvided());
    }
}
