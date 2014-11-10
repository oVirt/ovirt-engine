package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

/**
 * @see RestartVdsCommand on why this command is requiring a lock
 */
@NonTransactiveCommandAttribute
public class VdsNotRespondingTreatmentCommand<T extends FenceVdsActionParameters> extends RestartVdsCommand<T> {
    /**
     * use this member to determine if fence failed but vms moved to unknown mode (for the audit log type)
     */
    private static final String RESTART = "Restart";

    public VdsNotRespondingTreatmentCommand(T parameters) {
        this(parameters, null);
    }

    public VdsNotRespondingTreatmentCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    /**
     * Create an executor which retries to find a proxy, since this command is automatic and we don't want it to fail
     * fast if no proxy is available, but to try a few times.
     *
     * @return The executor, which is used to check if a proxy is available for fence the host.
     */
    @Override
    protected FenceExecutor createExecutorForProxyCheck() {
        return new FenceExecutor(getVds(), getParameters().getAction());
    }

    private boolean shouldFencingBeSkipped(VDS vds) {
        // check if fencing in cluster is enabled
        VDSGroup vdsGroup = getDbFacade().getVdsGroupDao().get(vds.getVdsGroupId());
        if (vdsGroup != null && !vdsGroup.getFencingPolicy().isFencingEnabled()) {
            AuditLogableBase alb = new AuditLogableBase(vds.getId());
            alb.setRepeatable(true);
            AuditLogDirector.log(alb, AuditLogType.VDS_ALERT_FENCE_DISABLED_BY_CLUSTER_POLICY);
            return true;
        }

        // check if connectivity is not broken
        if (isConnectivityBrokenThresholdReached(getVds())) {
            return true;
        }

        // fencing will be executed
        return false;
    }

    /**
     * Only fence the host if the VDS is down, otherwise it might have gone back up until this command was executed. If
     * the VDS is not fenced then don't send an audit log event.
     */
    @Override
    protected void executeCommand() {
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

        VdsValidator validator = new VdsValidator(getVds());
        boolean shouldBeFenced = validator.shouldVdsBeFenced();
        if (shouldBeFenced) {
            getParameters().setParentCommand(VdcActionType.VdsNotRespondingTreatment);
            VdcReturnValueBase retVal =
                    runInternalAction(VdcActionType.VdsKdumpDetection,
                            getParameters(),
                            getContext());

            if (retVal.getSucceeded()) {
                // kdump on host detected and finished successfully, stop hard fencing execution
                getReturnValue().setSucceeded(true);
                return;
            }

            // load cluster fencing policy
            FencingPolicy fencingPolicy = getDbFacade().getVdsGroupDao().get(
                    getVds().getVdsGroupId()
            ).getFencingPolicy();
            getParameters().setFencingPolicy(fencingPolicy);

            if (fencingPolicy.isSkipFencingIfSDActive()) {
                // host storage lease should be renewed each ConfigValues.HostStorageLeaseAliveInterval
                // so we need to be sure not to execute fencing before host is non responsive for longer time
                long interval = TimeUnit.SECONDS.toMillis(
                        Config.<Integer>getValue(ConfigValues.HostStorageLeaseAliveCheckingInterval));
                long difference = System.currentTimeMillis() - getParameters().getLastUpdate();
                if (difference < interval) {
                    int sleepMs = (int)(interval - difference);
                    log.info("Sleeping {} ms before proceeding with fence execution", sleepMs);
                    ThreadUtils.sleep(sleepMs);
                }
            }

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
            AuditLogDirector.log(alb, AuditLogType.VDS_ALERT_NOT_RESTARTED_DUE_TO_POLICY);
            setSucceeded(false);
            setCommandShouldBeLogged(false);
        } else {
            getReturnValue().setSucceeded(shouldBeFenced);
        }
    }

    @Override
    protected void setStatus() {
    }

    @Override
    protected void handleError() {
        // if fence failed on spm, move storage pool to non operational
        if (getVds().getSpmStatus() != VdsSpmStatus.None) {
            log.info("Fence failed on vds '{}' which is spm of pool '{}' - moving pool to non operational",
                    getVds().getName(), getVds().getStoragePoolId());
            runInternalAction(
                    VdcActionType.SetStoragePoolStatus,
                    new SetStoragePoolStatusParameters(getVds().getStoragePoolId(), StoragePoolStatus.NotOperational,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM));
        }
        log.error("Failed to run Fence script on vds '{}'.", getVdsName());
        AlertIfPowerManagementOperationSkipped(RESTART, null);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.VDS_RECOVER : AuditLogType.VDS_RECOVER_FAILED;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<String, String>();
            jobProperties.put(VdcObjectType.VDS.name().toLowerCase(),
                    (getVdsName() == null) ? "" : getVdsName());
        }
        return jobProperties;
    }

    private boolean isConnectivityBrokenThresholdReached(VDS vds) {
        VDSGroup cluster = DbFacade.getInstance().getVdsGroupDao().get(vds.getVdsGroupId());
        double percents = 0.0;
        boolean result = false;
        if (cluster.getFencingPolicy().isSkipFencingIfConnectivityBroken()) {
            List<VDS> hosts = DbFacade.getInstance().getVdsDao().getAllForVdsGroup(cluster.getId());
            double hostsNumber = hosts.size();
            List<VDS> hostsWithBrokenConnectivity = LinqUtils.filter(hosts,
                    new Predicate<VDS>() {
                        @Override
                        public boolean eval(VDS a) {
                            return (a.getStatus() == VDSStatus.Connecting || a.getStatus() == VDSStatus.NonResponsive);
                        }
                    });
            double hostsWithBrokenConnectivityNumber = hostsWithBrokenConnectivity.size();
            percents = (hostsWithBrokenConnectivityNumber/hostsNumber)*100.0;
            result = (percents >= cluster.getFencingPolicy().getHostsWithBrokenConnectivityThreshold());
        }
        if (result) {
            logAlert(vds, percents);
        }
        return result;
    }

    private void logAlert(VDS host, Double percents) {
        AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.addCustomValue("Percents", percents.toString());
        auditLogable.setVdsId(host.getId());
        auditLogable.setRepeatable(true);
        AuditLogDirector.log(auditLogable, AuditLogType.VDS_ALERT_FENCE_OPERATION_SKIPPED_BROKEN_CONNECTIVITY);
    }
}
