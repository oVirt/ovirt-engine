package org.ovirt.engine.core.bll.pm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.PreviousHostedEngineHost;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.validator.FenceValidator;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.monitoring.MonitoringStrategyFactory;

/**
 * @see RestartVdsCommand on why this command is requiring a lock
 */
@NonTransactiveCommandAttribute
public class VdsNotRespondingTreatmentCommand<T extends FenceVdsActionParameters> extends RestartVdsCommand<T> {
    /**
     * use this member to determine if fence failed but vms moved to unknown mode (for the audit log type)
     */
    private static final String RESTART = "Restart";

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private PreviousHostedEngineHost previousHostedEngineHost;

    @Inject
    private MonitoringStrategyFactory monitoringStrategyFactory;

    public VdsNotRespondingTreatmentCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    private boolean shouldFencingBeSkipped(VDS vds) {
        // check if fencing in cluster is enabled
        Cluster cluster = getDbFacade().getClusterDao().get(vds.getClusterId());
        if (cluster != null && !cluster.getFencingPolicy().isFencingEnabled()) {
            AuditLogableBase alb = new AuditLogableBase(vds.getId());
            alb.setRepeatable(true);
            auditLogDirector.log(alb, AuditLogType.VDS_ALERT_FENCE_DISABLED_BY_CLUSTER_POLICY);
            return true;
        }

        // check if connectivity is not broken
        if (isConnectivityBrokenThresholdReached(getVds())) {
            return true;
        }

        // fencing will be executed
        return false;
    }

    @Override
    protected boolean validate() {
        HostValidator validator = new HostValidator(getVds());
        return validate(validator.hostExists());
    }

    /**
     * Only fence the host if the VDS is down, otherwise it might have gone back up until this command was executed. If
     * the VDS is not fenced then don't send an audit log event.
     */
    @Override
    protected void executeCommand() {
        if (!previousHostedEngineHost.isPreviousHostId(getVds().getId()) && !new FenceValidator().isStartupTimeoutPassed() ||
                !isQuietTimeFromLastActionPassed()) {
            log.error("Failed to run Fence script on vds '{}'.", getVdsName());
            alertIfPowerManagementOperationSkipped(RESTART, null);
            // If fencing can't be done and the host is the SPM, set storage-pool to non-operational
            if (getVds().getSpmStatus() != VdsSpmStatus.None) {
                setStoragePoolNonOperational();
            }
            return;
        }
        setVds(null);
        if (getVds() == null) {
            setCommandShouldBeLogged(false);
            log.info("Host '{}' ({}) not fenced since it doesn't exist anymore.", getVdsName(), getVdsId());
            getReturnValue().setSucceeded(false);
            return;
        }

        if (shouldFencingBeSkipped(getVds())) {
            setSucceeded(false);
            setCommandShouldBeLogged(false);
            return;
        }

        boolean shouldBeFenced = getVds().shouldVdsBeFenced();
        if (shouldBeFenced) {
            getParameters().setParentCommand(VdcActionType.VdsNotRespondingTreatment);
            VdcReturnValueBase retVal;

            retVal = runInternalAction(VdcActionType.SshSoftFencing,
                    getParameters(),
                    cloneContext().withoutExecutionContext());
            if (retVal.getSucceeded()) {
                // SSH Soft Fencing was successful and host is Up, stop non responding treatment
                getReturnValue().setSucceeded(true);
                return;
            }

            // proceed with non responding treatment only if PM action are allowed and PM enabled for host
            if (!monitoringStrategyFactory.getMonitoringStrategyForVds(getVds()).isPowerManagementSupported()
                    || !getVds().isPmEnabled()) {
                setSucceeded(false);
                setCommandShouldBeLogged(false);
                return;
            }

            retVal = runInternalAction(VdcActionType.VdsKdumpDetection,
                    getParameters(),
                    cloneContext().withoutExecutionContext());
            if (retVal.getSucceeded()) {
                // kdump on host detected and finished successfully, stop hard fencing execution
                getReturnValue().setSucceeded(true);
                return;
            }

            // load cluster fencing policy
            FencingPolicy fencingPolicy = getDbFacade().getClusterDao().get(
                    getVds().getClusterId()
            ).getFencingPolicy();
            getParameters().setFencingPolicy(fencingPolicy);

            waitUntilSkipFencingIfSDActiveAllowed(fencingPolicy.isSkipFencingIfSDActive());

            // Make sure that the StopVdsCommand that runs by the RestartVds
            // don't write over our job, and disrupt marking the job status correctly
            ExecutionContext ec = (ExecutionContext) ObjectUtils.clone(this.getExecutionContext());
            if (ec != null) {
                ec.setJob(this.getExecutionContext().getJob());
                super.executeCommand();
                this.setExecutionContext(ec);
            } else {
                super.executeCommand();
                // Since the parent class run the command, we need to reinitialize the execution context
                if (this.getExecutionContext() != null) {
                    this.getExecutionContext().setJob(getDbFacade().getJobDao().get(this.getJobId()));
                }
            }
        } else {
            setCommandShouldBeLogged(false);
            log.info("Host '{}' ({}) not fenced since it's status is ok, or it doesn't exist anymore.",
                    getVdsName(), getVdsId());
        }
        if (skippedDueToFencingPolicy) {
            // fencing was skipped, fire an alert and suppress standard command logging
            AuditLogableBase alb = new AuditLogableBase(getVds().getId());
            alb.setRepeatable(true);
            auditLogDirector.log(alb, AuditLogType.VDS_ALERT_NOT_RESTARTED_DUE_TO_POLICY);
            setSucceeded(false);
            setCommandShouldBeLogged(false);
        } else {
            getReturnValue().setSucceeded(shouldBeFenced);
        }
    }

    private void setStoragePoolNonOperational() {
        log.info("Fence failed on vds '{}' which is spm of pool '{}' - moving pool to non operational",
                getVds().getName(),
                getVds().getStoragePoolId());
        CommandContext commandContext = getContext().clone();
        // CommandContext clone is 'shallow' and does not clone the internal ExecutionContext.
        // So ExecutionContext is cloned here manually to prevent a bug (BZ1145099).
        commandContext.withExecutionContext(new ExecutionContext(commandContext.getExecutionContext()));
        runInternalAction(
                VdcActionType.SetStoragePoolStatus,
                new SetStoragePoolStatusParameters(getVds().getStoragePoolId(),
                        StoragePoolStatus.NotOperational,
                        AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM), commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.VDS_RECOVER : AuditLogType.VDS_RECOVER_FAILED;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
            jobProperties.put(VdcObjectType.VDS.name().toLowerCase(),
                    (getVdsName() == null) ? "" : getVdsName());
        }
        return jobProperties;
    }

    private boolean isConnectivityBrokenThresholdReached(VDS vds) {
        Cluster cluster = DbFacade.getInstance().getClusterDao().get(vds.getClusterId());
        int percents = 0;
        boolean result = false;
        if (cluster.getFencingPolicy().isSkipFencingIfConnectivityBroken()) {
            List<VDS> hosts = DbFacade.getInstance().getVdsDao().getAllForCluster(cluster.getId());
            double hostsNumber = hosts.size();
            double hostsWithBrokenConnectivityNumber =
                    hosts.stream().filter(h -> h.getStatus() == VDSStatus.Connecting || h.getStatus() == VDSStatus.NonResponsive).count();
            percents = (int) ((hostsWithBrokenConnectivityNumber/hostsNumber)*100);
            result = percents >= cluster.getFencingPolicy().getHostsWithBrokenConnectivityThreshold();
        }
        if (result) {
            logAlert(vds, percents);
        }
        return result;
    }

    private void logAlert(VDS host, int percents) {
        AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.addCustomValue("Percents", String.valueOf(percents));
        auditLogable.setVdsId(host.getId());
        auditLogable.setRepeatable(true);
        auditLogDirector.log(auditLogable, AuditLogType.VDS_ALERT_FENCE_OPERATION_SKIPPED_BROKEN_CONNECTIVITY);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    private void waitUntilSkipFencingIfSDActiveAllowed(boolean skipFencingIfSDActive) {
        if (skipFencingIfSDActive) {
            // host storage lease should be renewed each ConfigValues.HostStorageLeaseAliveInterval
            // so we need to be sure not to execute fencing before host is non responsive for longer time
            long interval = TimeUnit.SECONDS.toMillis(
                    Config.<Integer>getValue(ConfigValues.HostStorageLeaseAliveCheckingInterval));
            long lastUpdate = getResourceManager().getVdsManager(getVdsId()).getLastUpdate();
            long difference = System.currentTimeMillis() - lastUpdate;
            if (difference < interval) {
                long sleepMs = interval - difference;
                log.info("Sleeping {} ms before proceeding with fence execution", sleepMs);
                ThreadUtils.sleep(sleepMs);
            }
        }
    }
}
