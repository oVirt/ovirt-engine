package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class MigrateVmToServerCommand<T extends MigrateVmToServerParameters> extends MigrateVmCommand<T> {

    public MigrateVmToServerCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void init() {
        setDestinationVdsId(getParameters().getVdsId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected boolean validateImpl() {
        if (getDestinationVds() == null) {
            return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
        }

        if (getDestinationVds().getStatus() != VDSStatus.Up) {
            addValidationMessage(EngineMessage.VAR__HOST_STATUS__UP);
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
        }

        if (!super.validateImpl()) {
            return false;
        }

        if (getParameters().getTargetClusterId() != null && !getParameters().getTargetClusterId().equals(getDestinationVds().getClusterId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DESTINATION_HOST_NOT_IN_DESTINATION_CLUSTER);
        }

        if (getVm().getRunOnVds() != null && getVm().getRunOnVds().equals(getDestinationVdsId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_TO_SAME_HOST);
        }

        if (!getVm().getClusterId().equals(getDestinationVds().getClusterId()) && getParameters().getTargetClusterId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIGRATE_BETWEEN_TWO_CLUSTERS);
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

    @Override
    protected boolean canScheduleVm() {
        // 'Do not schedule' parameter is used by MigrateMultipleVms command,
        // so that scheduling side effects are not executed twice.
        if (getParameters().isSkipScheduling()) {
            return true;
        }
        return super.canScheduleVm();
    }

    @Override
    protected Optional<Guid> getVdsToRunOn() {
        // 'Do not schedule' parameter is used by MigrateMultipleVms command,
        // so that scheduling side effects are not executed twice.
        if (getParameters().isSkipScheduling()) {
            return Optional.ofNullable(getParameters().getVdsId());
        }

        return super.getVdsToRunOn();
    }
}
