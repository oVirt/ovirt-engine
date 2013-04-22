package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.MigrateStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class MigrateVmCommand<T extends MigrateVmParameters> extends RunVmCommandBase<T> {

    private static final long serialVersionUID = -89419649366187512L;
    private Guid _vdsDestinationId;
    protected boolean forcedMigrationForNonMigratableVM;

    /**
     * Used to log the migration error.
     */
    private VdcBllErrors migrationErrorCode;

    public MigrateVmCommand(T parameters) {
        super(parameters);
        setVdsSelector(new VdsSelector(getVm(), getVdsDestinationId(), true, new VdsFreeMemoryChecker(this)));
        forcedMigrationForNonMigratableVM = parameters.isForceMigrationForNonMigratableVM();
    }

    // this property is used for audit log events
    public String getVdsDestination() {
        if (getDestinationVds() != null) {
            return getDestinationVds().getName();
        } else {
            return null;
        }
    }

    /**
     * @return Migration error text which is used in audit log message, if the migration status was queried from VDSM.
     */
    public String getDueToMigrationError() {
        if (migrationErrorCode == null) {
            return " ";
        }

        return " due to Error: " + Backend.getInstance()
                .getVdsErrorsTranslator()
                .TranslateErrorTextSingle(migrationErrorCode.name(), true);
    }

    @Override
    protected VDS getDestinationVds() {
        if (_destinationVds == null && _vdsDestinationId != null) {
            _destinationVds = DbFacade.getInstance().getVdsDao().get(_vdsDestinationId);
        }
        return _destinationVds;
    }

    @Override
    protected void failedToRunVm() {
        if (getVm().getStatus() != VMStatus.Up) {
            super.failedToRunVm();
        }
    }

    protected void initVdss() {
        setVdsIdRef(new Guid(getVm().getRunOnVds().toString()));
        setVdsDestinationId(getVdsSelector().getVdsToRunOn(true));
        // make _destinationVds null in order to refresh it from db in case it
        // changed.
        _destinationVds = null;
        if (_vdsDestinationId != null && _vdsDestinationId.equals(Guid.Empty)) {
            throw new VdcBLLException(VdcBllErrors.RESOURCE_MANAGER_CANT_ALLOC_VDS_MIGRATION);
        }
        if (getDestinationVds() == null) {
            throw new VdcBLLException(VdcBllErrors.RESOURCE_MANAGER_VDS_NOT_FOUND);
        }

        if (getVds() == null) {
            throw new VdcBLLException(VdcBllErrors.RESOURCE_MANAGER_VDS_NOT_FOUND);
        }
    }

    @Override
    protected void executeVmCommand() {
        initVdss();
        perform();
        processVm();
        setSucceeded(true);
    }

    private void processVm() {
        if (getVm().getStatus() != VMStatus.Up) {
            decreasePendingVms(getVds().getId());
        }
    }

    private void perform() {
        getVm().setMigratingToVds(_vdsDestinationId);

        String srcVdsHost = getVds().getHostName();
        String dstVdsHost = String.format("%1$s:%2$s", getDestinationVds().getHostName(), getDestinationVds()
                .getPort());
        Boolean tunnelMigration = null;
        if (FeatureSupported.tunnelMigration(getVm().getVdsGroupCompatibilityVersion())) {
            // if vm has no override for tunnel migration (its null),
            // use cluster's setting
            tunnelMigration =
                    getVm().getTunnelMigration() != null ? getVm().getTunnelMigration()
                            : getVdsGroup().isTunnelMigration();
        }

        // Starting migration at src VDS
        boolean connectToLunDiskSuccess = connectLunDisks(_vdsDestinationId);
        if (connectToLunDiskSuccess) {
            setActionReturnValue(Backend
                    .getInstance()
                    .getResourceManager()
                    .RunAsyncVdsCommand(
                            VDSCommandType.Migrate,
                            new MigrateVDSCommandParameters(getVdsId(), getVmId(), srcVdsHost, _vdsDestinationId,
                                    dstVdsHost, MigrationMethod.ONLINE, tunnelMigration, getMigrationNetworkIp()),
                            this)
                    .getReturnValue());
        }
        if (!connectToLunDiskSuccess || (VMStatus) getActionReturnValue() != VMStatus.MigratingFrom) {
            getVm().setMigreatingToPort(0);
            getVm().setMigreatingFromPort(0);
            getVm().setMigratingToVds(null);
            throw new VdcBLLException(VdcBllErrors.RESOURCE_MANAGER_MIGRATION_FAILED_AT_DST);
        }
        ExecutionHandler.setAsyncJob(getExecutionContext(), true);
    }

    private String getMigrationNetworkIp() {

        if (!FeatureSupported.migrationNetwork(getVm().getVdsGroupCompatibilityVersion())) {
            return null;
        }

        Network migrationNetwork = null;

        // Find migrationNetworkCluster
        List<Network> allNetworksInCluster =
                DbFacade.getInstance().getNetworkDao().getAllForCluster(getVm().getVdsGroupId());

        for (Network tempNetwork : allNetworksInCluster) {
            if (tempNetwork.getCluster().isMigration()) {
                migrationNetwork = tempNetwork;
                break;
            }
        }

        // Find migration ip
        if (migrationNetwork != null) {
            final List<VdsNetworkInterface> allInterfacesForDstVds =
                    DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(getDestinationVds().getId());

            for (VdsNetworkInterface nic : allInterfacesForDstVds) {
                if (migrationNetwork.getName().equals(nic.getNetworkName())) {
                    return nic.getAddress();
                }
            }
        }

        return null;
    }

    /**
     * command succeeded and VM is up => migration done
     * command succeeded and VM is not up => migration started
     * command failed and rerun flag is set => rerun migration was initiated
     * command failed and rerun flag is not set => migration failed
     */
    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ?
                getActionReturnValue() == VMStatus.Up ?
                        AuditLogType.VM_MIGRATION_DONE
                        : isInternalExecution() ?
                                AuditLogType.VM_MIGRATION_START_SYSTEM_INITIATED
                                : AuditLogType.VM_MIGRATION_START
                : _isRerun ?
                        AuditLogType.VM_MIGRATION_TRYING_RERUN
                        : getVds().getStatus() == VDSStatus.PreparingForMaintenance ?
                                AuditLogType.VM_MIGRATION_FAILED_DURING_MOVE_TO_MAINTENANCE
                                : AuditLogType.VM_MIGRATION_FAILED;
    }

    protected Guid getVdsDestinationId() {
        return _vdsDestinationId;
    }

    protected void setVdsDestinationId(Guid value) {
        _vdsDestinationId = value;
    }

    @Override
    protected boolean canDoAction() {
        return canMigrateVm(getVm());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MIGRATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    protected boolean canMigrateVm(VM vm) {
        if (vm == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        // If VM is pinned to host, no migration can occur
        if (vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_PINNED_TO_HOST);
        }

        if (vm.getMigrationSupport() == MigrationSupport.IMPLICITLY_NON_MIGRATABLE
                && !forcedMigrationForNonMigratableVM) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE_AND_IS_NOT_FORCED_BY_USER_TO_MIGRATE);
        }

        if (vm.getStatus() == VMStatus.MigratingFrom) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS);
        }

        if (vm.getStatus() == VMStatus.NotResponding) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        }

        if (vm.getStatus() == VMStatus.Paused) {
            return failCanDoAction(VdcBllMessages.MIGRATE_PAUSED_VM_IS_UNSUPPORTED);
        }

        if (!vm.isQualifyToMigrate()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
        }

        if (getDestinationVds() != null && getDestinationVds().getStatus() != VDSStatus.Up) {
            addCanDoActionMessage(VdcBllMessages.VAR__HOST_STATUS__UP);
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
        }

        return
                // This check was added to prevent migration of VM while its disks are being migrated
                // TODO: replace it with a better solution
                validate(new DiskImagesValidator(ImagesHandler.getPluggedImagesForVm(vm.getId())).diskImagesNotLocked())
                && validate(new SnapshotsValidator().vmNotDuringSnapshot(vm.getId()))
                && getVdsSelector().canFindVdsToRunOn(getReturnValue().getCanDoActionMessages(), true);
    }

    @Override
    public void rerun() {
        /**
         * make Vm property to null in order to refresh it from db.
         */
        setVm(null);
        determineMigrationFailureForAuditLog();
        /**
         * if vm is up and rerun is called then it got up on the source, try to
         * rerun
         */
        if (getVm() != null && getVm().getStatus() == VMStatus.Up) {
            setVdsDestinationId(null);
            super.rerun();
        } else {
            /**
             * vm went down on the destination and source, migration failed.
             */
            decreasePendingVms(getDestinationVds().getId());
            _isRerun = true;
            setSucceeded(false);
            log();
        }
    }

    /**
     * Log that the migration had failed with the error code that is in the VDS and needs to be retrieved.
     */
    protected void determineMigrationFailureForAuditLog() {
        if (getVm() != null && getVm().getStatus() == VMStatus.Up) {
            try {
                Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.MigrateStatus,
                        new MigrateStatusVDSCommandParameters(getVdsId(), getVmId()));
            } catch (VdcBLLException e) {
                migrationErrorCode = e.getErrorCode();
            }
        }
    }

    @Override
    protected Guid getCurrentVdsId() {
        if (getVdsDestinationId() != null) {
            return getVdsDestinationId();
        } else {
            return super.getCurrentVdsId();
        }
    }
}
