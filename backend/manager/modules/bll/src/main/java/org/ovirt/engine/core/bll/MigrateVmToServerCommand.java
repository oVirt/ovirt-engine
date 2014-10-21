package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class MigrateVmToServerCommand<T extends MigrateVmToServerParameters> extends MigrateVmCommand<T> {
    public MigrateVmToServerCommand(T parameters) {
        this(parameters, null);
    }

    public MigrateVmToServerCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setDestinationVdsId(parameters.getVdsId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected boolean canDoAction() {
        if (getDestinationVds() == null) {
            return failCanDoAction(VdcBllMessages.VDS_INVALID_SERVER_ID);
        }

        if (getDestinationVds().getStatus() != VDSStatus.Up) {
            addCanDoActionMessage(VdcBllMessages.VAR__HOST_STATUS__UP);
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
        }

        if (!super.canDoAction()) {
            return false;
        }

        if (getParameters().getTargetVdsGroupId() != null && !getParameters().getTargetVdsGroupId().equals(getDestinationVds().getVdsGroupId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DESTINATION_HOST_NOT_IN_DESTINATION_CLUSTER);
        }

        if (getVm().getRunOnVds() != null && getVm().getRunOnVds().equals(getDestinationVdsId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_TO_SAME_HOST);
        }

        if (!getVm().getVdsGroupId().equals(getDestinationVds().getVdsGroupId()) && getParameters().getTargetVdsGroupId() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATE_BETWEEN_TWO_CLUSTERS);
        }

        return true;
    }

    @Override
    protected AuditLogType getAuditLogForMigrationFailure() {
        return AuditLogType.VM_MIGRATION_TO_SERVER_FAILED;
    }

    /**
     * In case we failed to migrate to that specific server, the VM should no longer be pending,
     * and we report failure, without an attempt to rerun
     */
    @Override
    public void rerun() {
        // make VM property to null in order to refresh it from db
        setVm(null);
        determineMigrationFailureForAuditLog();
        runningFailed();
    }

    @Override
    protected List<Guid> getVdsWhiteList() {
        return Arrays.asList(getDestinationVdsId());
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        jobProperties = super.getJobMessageProperties();
        if (getDestinationVdsName() != null) {
            jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), getDestinationVdsName());
        }
        return jobProperties;
    }
}
