package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

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
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;

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

    /**
     * Only fence the host if the VDS is down, otherwise it might have gone back up until this command was executed. If
     * the VDS is not fenced then don't send an audit log event.
     */
    @Override
    protected void executeCommand() {
        setVds(null);
        if (getVds() == null) {
            setCommandShouldBeLogged(false);
            log.infoFormat("Host {0}({1}) not fenced since it doesn't exist anymore.", getVdsName(), getVdsId());
            getReturnValue().setSucceeded(false);
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
            log.infoFormat("Host {0}({1}) not fenced since it's status is ok, or it doesn't exist anymore.",
                    getVdsName(), getVdsId());
        }
        getReturnValue().setSucceeded(shouldBeFenced);
    }

    @Override
    protected void setStatus() {
    }

    @Override
    protected void handleError() {
        // if fence failed on spm, move storage pool to non operational
        if (getVds().getSpmStatus() != VdsSpmStatus.None) {
            log.infoFormat("Fence failed on vds {0} which is spm of pool {1} - moving pool to non operational",
                    getVds().getName(), getVds().getStoragePoolId());
            runInternalAction(
                    VdcActionType.SetStoragePoolStatus,
                    new SetStoragePoolStatusParameters(getVds().getStoragePoolId(), StoragePoolStatus.NotOperational,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM));
        }
        log.errorFormat("Failed to run Fence script on vds:{0}.", getVdsName());
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
}
