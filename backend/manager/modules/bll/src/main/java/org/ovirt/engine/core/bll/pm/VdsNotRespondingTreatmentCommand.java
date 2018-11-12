package org.ovirt.engine.core.bll.pm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.PreviousHostedEngineHost;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.monitoring.MonitoringStrategyFactory;

/**
 * @see RestartVdsCommand on why this command is requiring a lock
 */
@NonTransactiveCommandAttribute
public class VdsNotRespondingTreatmentCommand<T extends FenceVdsActionParameters> extends VdsCommand<T> {
    /**
     * use this member to determine if fence failed but vms moved to unknown mode (for the audit log type)
     */
    private static final String RESTART = "Restart";

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private PreviousHostedEngineHost previousHostedEngineHost;

    @Inject
    private MonitoringStrategyFactory monitoringStrategyFactory;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private HostLocking hostLocking;

    public VdsNotRespondingTreatmentCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    private boolean shouldFencingBeSkipped(VDS vds) {
        // check if fencing in cluster is enabled
        Cluster cluster = clusterDao.get(vds.getClusterId());
        if (cluster != null && !cluster.getFencingPolicy().isFencingEnabled()) {
            AuditLogable alb = createAuditLogableForHost(vds);
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

    private AuditLogable createAuditLogableForHost(VDS vds) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(vds.getId());
        logable.setVdsName(vds.getName());
        logable.setClusterId(vds.getClusterId());
        logable.setClusterName(vds.getClusterName());
        logable.setRepeatable(true);
        return logable;
    }

    @Override
    protected boolean validate() {
        HostValidator validator = HostValidator.createInstance(getVds());
        return validate(validator.hostExists());
    }

    /**
     * Only fence the host if the VDS is down, otherwise it might have gone back up until this command was executed. If
     * the VDS is not fenced then don't send an audit log event.
     */
    @Override
    protected void executeCommand() {
        VDS host = getVds();
        if (!previousHostedEngineHost.isPreviousHostId(host.getId())
                && !fenceValidator.isStartupTimeoutPassed()
                && !host.isInFenceFlow()) {
            alertIfFenceOperationSkipped();
            // If fencing can't be done and the host is the SPM, set storage-pool to non-operational
            if (host.getSpmStatus() != VdsSpmStatus.None) {
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
        ActionReturnValue restartVdsResult = null;
        if (shouldBeFenced) {
            getParameters().setParentCommand(ActionType.VdsNotRespondingTreatment);
            ActionReturnValue retVal;

            retVal = runInternalAction(ActionType.SshSoftFencing,
                    getParameters(),
                    cloneContext().withoutExecutionContext());
            if (retVal.getSucceeded()) {
                // SSH Soft Fencing was successful and host is Up, stop non responding treatment
                getReturnValue().setSucceeded(true);
                setCommandShouldBeLogged(false);
                return;
            }

            // proceed with non responding treatment only if PM action are allowed and PM enabled for host
            if (!monitoringStrategyFactory.getMonitoringStrategyForVds(getVds()).isPowerManagementSupported()
                    || !getVds().isPmEnabled()) {
                alertIfPowerManagementOperationSkipped();
                setSucceeded(false);
                setCommandShouldBeLogged(false);
                return;
            }

            retVal = runInternalAction(ActionType.VdsKdumpDetection,
                    getParameters(),
                    cloneContext().withoutExecutionContext());
            if (retVal.getSucceeded()) {
                // kdump on host detected and finished successfully, stop hard fencing execution
                getReturnValue().setSucceeded(true);
                return;
            }

            //if an external-status other than OK has been set on the host,
            //that is considered an indication not to perform automatic
            //power-management operations on the host.
            if (!ExternalStatus.Ok.equals(host.getExternalStatus())) {
                AuditLogable logEntry = createAuditLogableForHost(host);
                logEntry.addCustomValue("ExternalStatus", host.getExternalStatus().toString());
                auditLogDirector.log(logEntry, AuditLogType.VDS_AUTO_FENCE_SKIPPED_DUE_TO_EXTERNAL_STATUS);
                getReturnValue().setSucceeded(false);
                return;
            }
            // load cluster fencing policy
            FencingPolicy fencingPolicy = clusterDao.get(getVds().getClusterId()).getFencingPolicy();
            getParameters().setFencingPolicy(fencingPolicy);

            waitUntilSkipFencingIfSDActiveAllowed(fencingPolicy.isSkipFencingIfSDActive());
            restartVdsResult = runInternalAction(ActionType.RestartVds,
                    getParameters(), cloneContext().withoutExecutionContext());
        } else {
            setCommandShouldBeLogged(false);
            log.info("Host '{}' ({}) not fenced since it's status is ok, or it doesn't exist anymore.",
                    getVdsName(), getVdsId());
        }
        if (restartVdsResult != null
                && restartVdsResult.<RestartVdsResult>getActionReturnValue() != null
                && restartVdsResult.<RestartVdsResult>getActionReturnValue().isSkippedDueToFencingPolicy()) {
            // fencing was skipped, fire an alert and suppress standard command logging
            AuditLogable alb = createAuditLogableForHost(getVds());
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
        CommandContext commandContext = getContext().clone().withoutLock();
        // CommandContext clone is 'shallow' and does not clone the internal ExecutionContext.
        // So ExecutionContext is cloned here manually to prevent a bug (BZ1145099).
        commandContext.withExecutionContext(new ExecutionContext(commandContext.getExecutionContext()));
        runInternalAction(
                ActionType.SetStoragePoolStatus,
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
        Cluster cluster = clusterDao.get(vds.getClusterId());
        int percents = 0;
        boolean result = false;
        if (cluster.getFencingPolicy().isSkipFencingIfConnectivityBroken()) {
            List<VDS> hosts = vdsDao.getAllForCluster(cluster.getId());
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
        AuditLogable auditLogable = createAuditLogableForHost(host);
        auditLogable.addCustomValue("Percents", String.valueOf(percents));
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

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return hostLocking.getPowerManagementLock(getVdsId());
    }
}
