package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.scheduling.VdsFreeMemoryChecker;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.MigrateStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

@NonTransactiveCommandAttribute
public class MigrateVmCommand<T extends MigrateVmParameters> extends RunVmCommandBase<T> {

    /** The VDS that the VM is going to migrate to */
    private VDS destinationVds;

    /** Used to log the migration error. */
    private VdcBllErrors migrationErrorCode;

    public MigrateVmCommand(T parameters) {
        this(parameters, null);
    }

    public MigrateVmCommand(T migrateVmParameters, CommandContext cmdContext) {
        super(migrateVmParameters, cmdContext);

        if (migrateVmParameters.getTargetVdsGroupId() != null) {
            setVdsGroupId(migrateVmParameters.getTargetVdsGroupId());
            // force reload
            setVdsGroup(null);
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    /**
     * this property is used for audit log events
     */
    public final String getDestinationVdsName() {
        VDS destinationVds = getDestinationVds();
        return destinationVds != null ? destinationVds.getName() : null;
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

    /**
     * Returns the VDS that the VM is about to migrate to
     */
    protected VDS getDestinationVds() {
        return destinationVds;
    }

    @Override
    protected void processVmOnDown() {
        // In case the migration failed and the VM turned back to Up in the
        // source, we don't need to handle it as a VM that failed to run
        if (getVm().getStatus() != VMStatus.Up) {
            super.processVmOnDown();
        }
    }

    protected boolean initVdss() {
        setVdsIdRef(getVm().getRunOnVds());
        Guid vdsToRunOn =
                SchedulingManager.getInstance().schedule(getVdsGroup(),
                        getVm(),
                        getVdsBlackList(),
                        getVdsWhiteList(),
                        getDestinationVdsId(),
                        new ArrayList<String>(),
                        new VdsFreeMemoryChecker(this),
                        getCorrelationId());
        setDestinationVdsId(vdsToRunOn);
        if (vdsToRunOn != null && !Guid.Empty.equals(vdsToRunOn)) {
            getRunVdssList().add(vdsToRunOn);
        }
        VmHandler.updateVmGuestAgentVersion(getVm());

        if (vdsToRunOn != null && vdsToRunOn.equals(Guid.Empty)) {
            return false;
        }

        if (getDestinationVds() == null || getVds() == null) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeVmCommand() {
        setSucceeded(initVdss() && perform());
    }

    private boolean perform() {
        getParameters().setStartTime(new Date());

        try {
            if (connectLunDisks(getDestinationVdsId()) && migrateVm()) {
                ExecutionHandler.setAsyncJob(getExecutionContext(), true);
                return true;
            }
        }
        catch (VdcBLLException e) {
        }

        runningFailed();
        return false;
    }

    private boolean migrateVm() {
        setActionReturnValue(Backend.getInstance().getResourceManager()
                .RunAsyncVdsCommand(
                        VDSCommandType.Migrate,
                        createMigrateVDSCommandParameters(),
                        this)
                .getReturnValue());

        return getActionReturnValue() == VMStatus.MigratingFrom;
    }

    private MigrateVDSCommandParameters createMigrateVDSCommandParameters() {
        String srcVdsHost = getVds().getHostName();
        String dstVdsHost = String.format("%1$s:%2$s",
                getDestinationVds().getHostName(),
                getDestinationVds().getPort());

        return new MigrateVDSCommandParameters(getVdsId(), getVmId(), srcVdsHost, getDestinationVdsId(),
                dstVdsHost, MigrationMethod.ONLINE, isTunnelMigrationUsed(), getMigrationNetworkIp(), getVds().getVdsGroupCompatibilityVersion(),
                getMaximumMigrationDowntime(), getAutoConverge(), getMigrateCompressed());
    }

    @Override
    public void runningSucceded() {
        try {
            getVmDynamicDao().clearMigratingToVds(getVmId());
            updateVmAfterMigrationToDifferentCluster();
        }
        finally {
            super.runningSucceded();
        }
    }

    private void updateVmAfterMigrationToDifferentCluster() {
        if (getVm().getVdsGroupId().equals(getParameters().getTargetVdsGroupId())) {
            return;
        }

        ChangeVMClusterParameters params = new ChangeVMClusterParameters(getParameters().getTargetVdsGroupId(), getVmId());
        setSucceeded(getBackend().runInternalAction(VdcActionType.ChangeVMCluster, params).getSucceeded());
    }

    private Boolean getAutoConverge() {
        if (FeatureSupported.autoConvergence(getVm().getVdsGroupCompatibilityVersion())) {
            if (getVm().getAutoConverge() != null) {
                return getVm().getAutoConverge();
            }

            if (getVdsGroup().getAutoConverge() != null) {
                return getVdsGroup().getAutoConverge();
            }

            return Config.getValue(ConfigValues.DefaultAutoConvergence);
        }

        return null;
    }

    private Boolean getMigrateCompressed() {
        if (FeatureSupported.migrationCompression(getVm().getVdsGroupCompatibilityVersion())) {
            if (getVm().getMigrateCompressed() != null) {
                return getVm().getMigrateCompressed();
            }

            if (getVdsGroup().getMigrateCompressed() != null) {
                return getVdsGroup().getMigrateCompressed();
            }

            return Config.getValue(ConfigValues.DefaultMigrationCompression);
        }

        return null;
    }

    private int getMaximumMigrationDowntime() {
        if (getVm().getMigrationDowntime() != null) {
            return getVm().getMigrationDowntime();
        }

        return Config.getValue(ConfigValues.DefaultMaximumMigrationDowntime);
    }

    private boolean isTunnelMigrationUsed() {
        if (!FeatureSupported.tunnelMigration(getVm().getVdsGroupCompatibilityVersion())) {
            return false;
        }
        // if vm has no override for tunnel migration (its null),
        // use cluster's setting
        return getVm().getTunnelMigration() != null ?
                getVm().getTunnelMigration()
                : getVdsGroup().isTunnelMigration();
    }

    private String getMigrationNetworkIp() {

        if (!FeatureSupported.migrationNetwork(getVm().getVdsGroupCompatibilityVersion())) {
            return null;
        }

        Network migrationNetwork = null;

        // Find migrationNetworkCluster
        List<Network> allNetworksInCluster = getNetworkDAO().getAllForCluster(getVm().getVdsGroupId());

        for (Network tempNetwork : allNetworksInCluster) {
            if (tempNetwork.getCluster().isMigration()) {
                migrationNetwork = tempNetwork;
                break;
            }
        }

        if (migrationNetwork != null) {

            // assure migration network is active on source host
            if (getMigrationNetworkAddress(getVds().getId(), migrationNetwork.getName()) == null) {
                return null;
            }

            // find migration IP address on destination host
            return getMigrationNetworkAddress(getDestinationVds().getId(), migrationNetwork.getName());
        }

        return null;
    }

    private String getMigrationNetworkAddress(Guid hostId, String migrationNetworkName) {
        final List<VdsNetworkInterface> nics =
                getDbFacade().getInterfaceDao().getAllInterfacesForVds(hostId);

        for (VdsNetworkInterface nic : nics) {
            if (migrationNetworkName.equals(nic.getNetworkName()) && migrationInterfaceUp(nic, nics)) {
                return nic.getAddress();
            }
        }

        return null;
    }

    protected boolean migrationInterfaceUp(VdsNetworkInterface nic, List<VdsNetworkInterface> nics) {
        if (NetworkUtils.isVlan(nic)) {
            String physicalNic = nic.getBaseInterface();
            for (VdsNetworkInterface iface : nics) {
                if (iface.getName().equals(physicalNic)) {
                    return iface.getStatistics().getStatus() == InterfaceStatus.UP;
                }
            }
        }

        return nic.getStatistics().getStatus() == InterfaceStatus.UP;
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
                        : getAuditLogForMigrationStarted()
                : _isRerun ?
                        AuditLogType.VM_MIGRATION_TRYING_RERUN
                        : getAuditLogForMigrationFailure();
    }

    private AuditLogType getAuditLogForMigrationStarted() {
        return isInternalExecution() ?
                AuditLogType.VM_MIGRATION_START_SYSTEM_INITIATED
                : AuditLogType.VM_MIGRATION_START;
    }

    protected AuditLogType getAuditLogForMigrationFailure() {
        if (getVds().getStatus() == VDSStatus.PreparingForMaintenance) {
            return AuditLogType.VM_MIGRATION_FAILED_DURING_MOVE_TO_MAINTENANCE;
        }

        if (getDestinationVds() == null) {
            return AuditLogType.VM_MIGRATION_FAILED_NO_VDS_TO_RUN_ON;
        }

        return AuditLogType.VM_MIGRATION_FAILED;
    }

    protected Guid getDestinationVdsId() {
        VDS destinationVds = getDestinationVds();
        return destinationVds != null ? destinationVds.getId() : null;
    }

    protected void setDestinationVdsId(Guid vdsId) {
        destinationVds = vdsId != null ? getVdsDAO().get(vdsId) : null;
    }

    @Override
    protected boolean canDoAction() {
        final VM vm = getVm();

        if (vm == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        VmValidator vmValidator = new VmValidator(vm);
        if (!validate(vmValidator.isVmPluggedDiskNotUsingScsiReservation())) {
            return false;
        }

        if (!FeatureSupported.isMigrationSupported(getVdsGroup().getArchitecture(), getVdsGroup().getCompatibilityVersion())) {
            return failCanDoAction(VdcBllMessages.MIGRATION_IS_NOT_SUPPORTED);
        }

        // If VM is pinned to host, no migration can occur
        if (vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_PINNED_TO_HOST);
        }

        if (vm.getMigrationSupport() == MigrationSupport.IMPLICITLY_NON_MIGRATABLE
                && !getParameters().isForceMigrationForNonMigratableVm()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE_AND_IS_NOT_FORCED_BY_USER_TO_MIGRATE);
        }

        switch (vm.getStatus()) {
        case MigratingFrom:
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS);

        case NotResponding:
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(VMStatus.NotResponding));

        case Paused:
            if (vm.getVmPauseStatus() == VmPauseStatus.EIO) {
                return failCanDoAction(VdcBllMessages.MIGRATE_PAUSED_EIO_VM_IS_NOT_SUPPORTED);
            }
            break;

        default:
        }

        if (!vm.isQualifyToMigrate()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
        }

        if (!validate(vmValidator.vmNotHavingPluggedDiskSnapshots(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_PLUGGED_DISK_SNAPSHOT))
                || !validate(vmValidator.vmNotHavingPassthroughVnics())) {
            return false;
        }

        if (getParameters().getTargetVdsGroupId() != null) {
            ChangeVmClusterValidator changeVmClusterValidator = new ChangeVmClusterValidator(this, getParameters().getTargetVdsGroupId());
            if (!changeVmClusterValidator.validate()) {
                return false;
            }
        }

        return validate(new SnapshotsValidator().vmNotDuringSnapshot(vm.getId()))
                // This check was added to prevent migration of VM while its disks are being migrated
                // TODO: replace it with a better solution
                && validate(new DiskImagesValidator(ImagesHandler.getPluggedActiveImagesForVm(vm.getId())).diskImagesNotLocked())
                && SchedulingManager.getInstance().canSchedule(getVdsGroup(),
                        getVm(),
                        getVdsBlackList(),
                        getParameters().getInitialHosts(),
                        getDestinationVdsId(),
                        getReturnValue().getCanDoActionMessages());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MIGRATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    public void rerun() {
         // make Vm property to null in order to refresh it from db
        setVm(null);

        determineMigrationFailureForAuditLog();

        // if vm is up and rerun is called then it got up on the source, try to rerun
        if (getVm() != null && getVm().getStatus() == VMStatus.Up) {
            super.rerun();
        } else {
            // vm went down on the destination and source, migration failed.
            runningFailed();
            // signal the caller that a rerun was made so that it won't log
            // the failure message again
            _isRerun = true;
        }
    }

    @Override
    protected void reexecuteCommand() {
        setDestinationVdsId(null);
        super.reexecuteCommand();
    }

    /**
     * Log that the migration had failed with the error code that is in the VDS and needs to be retrieved.
     */
    protected void determineMigrationFailureForAuditLog() {
        if (getVm() != null && getVm().getStatus() == VMStatus.Up) {
            try {
                runVdsCommand(VDSCommandType.MigrateStatus,
                        new MigrateStatusVDSCommandParameters(getVdsId(), getVmId()));
            } catch (VdcBLLException e) {
                migrationErrorCode = e.getErrorCode();
            }
        }
    }

    @Override
    protected Guid getCurrentVdsId() {
        Guid destinationVdsId = getDestinationVdsId();
        return destinationVdsId != null ? destinationVdsId : super.getCurrentVdsId();
    }

    // Duration: time that took for the actual migration
    public String getDuration() {
        return DurationFormatUtils.formatDurationWords(new Date().getTime() - getParameters().getStartTime().getTime(), true, true);
    }

    // TotalDuration: time that took migration including retries (can be identical to Duration)
    public String getTotalDuration() {
        return DurationFormatUtils.formatDurationWords(new Date().getTime() - getParameters().getTotalMigrationTime().getTime(), true, true);
    }

    @Override
    protected String getLockMessage() {
        StringBuilder builder = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_BEING_MIGRATED.name());
        builder.append(String.format("$VmName %1$s", getVmName()));
        return builder.toString();
    }

    // hosts that cannot be selected for scheduling (failed hosts + VM source host)
    private List<Guid> getVdsBlackList() {
        List<Guid> blackList = new ArrayList<Guid>(getRunVdssList());
        if (getVdsId() != null) {
            blackList.add(getVdsId());
        }
        return blackList;
    }

    // initial hosts list picked for scheduling, currently
    // passed by load balancing process.
    protected List<Guid> getVdsWhiteList() {
        return getParameters().getInitialHosts();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        if (getParameters().getTargetVdsGroupId() != null) {
            // additional permissions needed since changing the cluster
            permissionList.addAll(VmHandler.getPermissionsNeededToChangeCluster(getParameters().getVmId(), getParameters().getTargetVdsGroupId()));
        }

        return permissionList;
    }

    @Override
    public void onPowerringUp() {
        // nothing to do
    }
}
