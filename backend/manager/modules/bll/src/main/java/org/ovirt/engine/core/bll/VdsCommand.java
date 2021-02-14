package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.pm.PmHealthCheckManager;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.FenceValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
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
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public abstract class VdsCommand<T extends VdsActionParameters> extends CommandBase<T> {

    protected String _failureMessage = null;

    @Inject
    protected AuditLogDirector auditLogDirector;
    @Inject
    protected FenceValidator fenceValidator;
    @Inject
    private PmHealthCheckManager pmHealthCheckManager;
    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private HostLocking hostLocking;
    @Inject
    protected VdsSpmIdMapDao vdsSpmIdMapDao;
    @Inject
    protected GlusterUtil glusterUtil;
    @Inject
    protected GlusterDBUtils glusterDBUtils;
    @Inject
    private AlertDirector alertDirector;
    @Inject
    private VdsStaticDao vdsStaticDao;

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

    @Override
    protected String getDescription() {
        return getVdsName();
    }

    protected void runSleepOnReboot() {
        runSleepOnReboot(false, VDSStatus.NonResponsive);
    }

    protected void runSleepOnReboot(boolean synchronous, final VDSStatus status) {
        if (synchronous) {
            sleepOnReboot(status);
        } else {
            ThreadPoolUtil.execute(() -> sleepOnReboot(status));
        }
    }

    private void sleepOnReboot(final VDSStatus status) {
        int sleepTimeInSec = Config.<Integer> getValue(ConfigValues.ServerRebootTimeout);
        log.info("Waiting {} seconds, for server to finish reboot process.",
                sleepTimeInSec);
        resourceManager.getVdsManager(getVdsId()).setInServerRebootTimeout(true);
        ThreadUtils.sleep(TimeUnit.SECONDS.toMillis(sleepTimeInSec));
        resourceManager.getVdsManager(getVdsId()).setInServerRebootTimeout(false);
        setVdsStatus(status);
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     */
    private void alert(AuditLogType logType) {
        AuditLogable alert = new AuditLogableImpl();
        alert.setVdsName(getVds().getName());
        alert.setVdsId(getVds().getId());
        auditLogDirector.log(alert, logType);
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

        if (!getVds().isManaged()) {
            return;
        }

        // Check first if PM is enabled on the cluster level
        if (getVds().isFencingEnabled()) {
            if (!vdsStatic.isPmEnabled()) {
                alert(AuditLogType.VDS_ALERT_FENCE_IS_NOT_CONFIGURED);
                // remove any test failure alerts
                alertDirector.removeVdsAlert(vdsStatic.getId(), AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
            } else {
                alertDirector.removeVdsAlert(vdsStatic.getId(), AuditLogType.VDS_ALERT_FENCE_IS_NOT_CONFIGURED);
            }
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
     */
    protected void alertIfPowerManagementOperationSkipped() {
        alert(AuditLogType.VDS_ALERT_NO_PM_CONFIG_FENCE_OPERATION_SKIPPED);
    }

    /**
     * Alerts if fence operation skipped.
     */
    protected void alertIfFenceOperationSkipped() {
        alert(AuditLogType.VDS_ALERT_FENCE_OPERATION_SKIPPED);
    }

    protected void logSettingVmToDown(VM vm) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsName(getVds().getName());
        logable.setVdsId(getVds().getId());
        logable.setVmName(vm.getName());
        logable.setVmId(vm.getId());
        auditLogDirector.log(logable, AuditLogType.VM_WAS_SET_DOWN_DUE_TO_HOST_REBOOT_OR_MANUAL_FENCE);
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
            String clusterCompatibilityVersion,
            boolean validateAgents) {
        if (pmEnabled && fenceAgents != null) {
            if (fenceAgents.isEmpty()) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT);
                return false;
            }
            if (validateAgents) {
                for (FenceAgent agent : fenceAgents) {
                    if (!fenceValidator.isFenceAgentVersionCompatible(agent,
                            clusterCompatibilityVersion,
                            getReturnValue().getValidationMessages())
                            || !isFenceAgentValid(agent)) {
                        return false;
                    }
                }
            } else {
                return true;
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

    protected void markVdsReinstalled() {
        vdsStaticDao.updateReinstallRequired(getVds().getStaticData().getId(), false);
    }

    protected String getErrorMessage(String msg) {
        return !StringUtils.isEmpty(msg) ? msg : String.format(
            "Please refer to %1$s/engine.log and log logs under %1$s/host-deploy/ for further details.",
            EngineLocalConfig.getInstance().getLogDir()
        );
    }
}
