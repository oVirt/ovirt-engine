package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.IStorageHelper;
import org.ovirt.engine.core.bll.storage.StorageHelperBase;
import org.ovirt.engine.core.bll.storage.StorageHelperDirector;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.DiskValidator;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;

public abstract class AbstractDiskVmCommand<T extends VmDiskOperationParameterBase> extends VmCommand<T> {

    public AbstractDiskVmCommand(T parameters) {
        this(parameters, null);
    }

    protected AbstractDiskVmCommand(Guid commandId) {
        super(commandId);
    }

    public AbstractDiskVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected void performPlugCommand(VDSCommandType commandType,
                                      Disk disk, VmDevice vmDevice) {
        if (disk.getDiskStorageType() == DiskStorageType.LUN) {
            LunDisk lunDisk = (LunDisk) disk;
            if (commandType == VDSCommandType.HotPlugDisk) {
                LUNs lun = lunDisk.getLun();
                updateLUNConnectionsInfo(lun);
                Map<StorageType, List<StorageServerConnections>> lunsByStorageType =
                        StorageHelperBase.filterConnectionsByStorageType(lun);
                for (StorageType storageType : lunsByStorageType.keySet()) {
                    if (!getStorageHelper(storageType).connectStorageToLunByVdsId(null,
                            getVm().getRunOnVds(),
                            lun,
                            getVm().getStoragePoolId())) {
                        throw new VdcBLLException(VdcBllErrors.StorageServerConnectionError);
                    }
                }
            }
        }
        runVdsCommand(commandType, new HotPlugDiskVDSParameters(getVm().getRunOnVds(),
                getVm(), disk, vmDevice));
    }

    private IStorageHelper getStorageHelper(StorageType storageType) {
        return StorageHelperDirector.getInstance().getItem(storageType);
    }

    /**
     * If the LUN has no connections we assume that it is FCP storage type, since FCP does not have connections,
     * otherwise, we return the storage type of the first connection
     *
     * @param lun
     *            - The lun we set the connection at.
     * @return The storage type of the lun (ISCSI or FCP).
     */
    protected StorageType getLUNStorageType(LUNs lun) {
        return lun.getLunConnections().isEmpty() ? StorageType.FCP : lun.getLunConnections().get(0).getstorage_type();
    }

    /**
     * Sets the LUN connection list from the DB.
     *
     * @param lun
     *            - The lun we set the connection at.
     */
    private void updateLUNConnectionsInfo(LUNs lun) {
        lun.setLunConnections(new ArrayList<StorageServerConnections>(getDbFacade()
                .getStorageServerConnectionDao()
                .getAllForLun(lun.getLUN_id())));
    }

    protected boolean isDiskPassPciAndIdeLimit(Disk diskInfo) {
        List<VmNic> vmInterfaces = getVmNicDao().getAllForVm(getVmId());
        List<Disk> allVmDisks = new ArrayList<Disk>(getVm().getDiskMap().values());
        allVmDisks.add(diskInfo);

        return checkPciAndIdeLimit(getVm().getOs(),
                getVm().getVdsGroupCompatibilityVersion(),
                getVm().getNumOfMonitors(),
                vmInterfaces,
                allVmDisks,
                isVirtioScsiControllerAttached(getVmId()),
                hasWatchdog(getVmId()),
                isBalloonEnabled(getVmId()),
                isSoundDeviceEnabled(getVmId()),
                getReturnValue().getCanDoActionMessages());
    }

    protected boolean isVirtioScsiControllerAttached(Guid vmId) {
        return VmDeviceUtils.isVirtioScsiControllerAttached(vmId);
    }

    protected boolean isBalloonEnabled(Guid vmId) {
        return VmDeviceUtils.isBalloonEnabled(vmId);
    }

    protected boolean isSoundDeviceEnabled(Guid vmId) {
        return VmDeviceUtils.isSoundDeviceEnabled(vmId);
    }

    protected boolean hasWatchdog(Guid vmId) {
        return VmDeviceUtils.hasWatchdog(vmId);
    }

    protected boolean isDiskCanBeAddedToVm(Disk diskInfo, VM vm) {
        if (!diskInfo.isDiskSnapshot() && diskInfo.isBoot()) {
            for (Disk disk : vm.getDiskMap().values()) {
                if (disk.isBoot() && !disk.isDiskSnapshot()) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE);
                    getReturnValue().getCanDoActionMessages().add(
                            String.format("$DiskName %1$s", disk.getDiskAlias()));
                    getReturnValue().getCanDoActionMessages().add(
                            String.format("$VmName %1$s", vm.getName()));
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * loads the disk info for the active snapshot, for luns the lun disk will be returned.
     */
    protected Disk loadActiveDisk(Guid diskId) {
        return getDiskDao().get(diskId);
    }

    protected Disk loadDiskFromSnapshot(Guid diskId, Guid snapshotId) {
        return getDiskImageDao().getDiskSnapshotForVmSnapshot(diskId, snapshotId);
    }

    protected Disk loadDisk(Guid diskId) {
        if (getParameters().getSnapshotId() == null) {
            return loadActiveDisk(diskId);
        } else {
            return loadDiskFromSnapshot(diskId, getParameters().getSnapshotId());
        }
    }

    /** Updates the VM's disks from the database */
    protected void updateDisksFromDb() {
        VmHandler.updateDisksFromDb(getVm());
    }

    protected boolean isVersionSupportedForShareable(Disk disk, String compVersion) {
        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            return Config.<Boolean> getValue(ConfigValues.ShareableDiskEnabled, compVersion);
        }
        return true;
    }

    protected boolean isVolumeFormatSupportedForShareable(VolumeFormat volumeFormat) {
        return volumeFormat == VolumeFormat.RAW;
    }

    protected boolean isVmInUpPausedDownStatus() {
        if (getVm().getStatus() != VMStatus.Up && getVm().getStatus() != VMStatus.Down
                && getVm().getStatus() != VMStatus.Paused) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(getVm().getStatus()));
        }
        return true;
    }

    protected boolean isVmExist() {
        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        }
        return true;
    }

    protected boolean isDiskExist(Disk disk) {
        if (disk == null || !isDiskExistInVm(disk)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
            return false;
        }
        return true;
    }

    protected boolean checkDiskUsedAsOvfStore(Disk disk) {
        return checkDiskUsedAsOvfStore(getDiskValidator(disk));
    }

    protected boolean checkDiskUsedAsOvfStore(DiskValidator diskValidator) {
        return validate(diskValidator.isDiskUsedAsOvfStore());
    }

    private boolean isDiskExistInVm(Disk disk) {
        List<VM> listVms = getVmDAO().getVmsListForDisk(disk.getId(), true);
        for (VM vm : listVms) {
            if (vm.getId().equals(getVmId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isHotPlugSupported() {
        if (getParameters().getSnapshotId() == null) {
            return super.isHotPlugSupported();
        }

        return isHotPlugDiskSnapshotSupported();
    }

    protected boolean isHotPlugDiskSnapshotSupported() {
        if (!FeatureSupported.hotPlugDiskSnapshot(getVds().getVdsGroupCompatibilityVersion())) {
            return failCanDoAction(VdcBllMessages.HOT_PLUG_DISK_SNAPSHOT_IS_NOT_SUPPORTED);
        }

        return true;
    }

    protected ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    protected ImageStorageDomainMapDao getImageStorageDomainMapDao() {
        return getDbFacade().getImageStorageDomainMapDao();
    }

    protected BaseDiskDao getBaseDiskDao() {
        return getDbFacade().getBaseDiskDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }


    /**
     * @return The StoragePoolIsoMapDAO
     */
    protected StoragePoolIsoMapDAO getStoragePoolIsoMapDao() {
        return getDbFacade().getStoragePoolIsoMapDao();
    }

    public String getDiskAlias() {
        return getParameters().getDiskInfo().getDiskAlias();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskAlias());
        }
        return jobProperties;
    }

    protected SnapshotsValidator getSnapshotsValidator() {
        return new SnapshotsValidator();
    }

    protected DiskValidator getDiskValidator(Disk disk) {
        return new DiskValidator(disk);
    }

    protected boolean isVmNotInPreviewSnapshot() {
        final SnapshotsValidator snapshotsValidator = getSnapshotsValidator();
        return
                getVmId() != null &&
                validate(snapshotsValidator.vmNotDuringSnapshot(getVmId())) &&
                validate(snapshotsValidator.vmNotInPreview(getVmId()));
    }

    protected boolean isVmNotLocked() {
        return
                getVm() != null &&
                validate(new VmValidator(getVm()).vmNotLocked());
    }
}
