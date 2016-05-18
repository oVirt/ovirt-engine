package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.connection.IStorageHelper;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperBase;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.bll.storage.disk.cinder.CinderBroker;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoBuilder;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

public abstract class AbstractDiskVmCommand<T extends VmDiskOperationParameterBase> extends VmCommand<T> {

    private CinderBroker cinderBroker;

    @Inject
    private VmDao vmDao;

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
                        throw new EngineException(EngineError.StorageServerConnectionError);
                    }
                }
            }
        } else if (disk.getDiskStorageType() == DiskStorageType.CINDER) {
            CinderDisk cinderDisk = (CinderDisk) disk;
            setStorageDomainId(cinderDisk.getStorageIds().get(0));
            getCinderBroker().updateConnectionInfoForDisk(cinderDisk);
        }
        Map<String, String> diskAddressMap = getDiskAddressMap(vmDevice, getDiskVmElement().getDiskInterface());
        runVdsCommand(commandType, new HotPlugDiskVDSParameters(getVm().getRunOnVds(),
                getVm(), disk, vmDevice, diskAddressMap, getDiskVmElement().getDiskInterface()));
    }

    private IStorageHelper getStorageHelper(StorageType storageType) {
        return StorageHelperDirector.getInstance().getItem(storageType);
    }

    /**
     * Sets the LUN connection list from the DB.
     *
     * @param lun
     *            - The lun we set the connection at.
     */
    private void updateLUNConnectionsInfo(LUNs lun) {
        lun.setLunConnections(new ArrayList<>(getDbFacade()
                .getStorageServerConnectionDao()
                .getAllForLun(lun.getLUNId())));
    }

    protected boolean isDiskPassPciAndIdeLimit() {
        List<VmNic> vmInterfaces = getVmNicDao().getAllForVm(getVmId());
        List<DiskVmElement> diskVmElements = getDiskVmElementDao().getAllForVm(getVmId());

        diskVmElements.add(getDiskVmElement());

        return checkPciAndIdeLimit(getVm().getOs(),
                getVm().getCompatibilityVersion(),
                getVm().getNumOfMonitors(),
                vmInterfaces,
                diskVmElements,
                isVirtioScsiControllerAttached(getVmId()),
                hasWatchdog(getVmId()),
                isBalloonEnabled(getVmId()),
                isSoundDeviceEnabled(getVmId()),
                getReturnValue().getValidationMessages());
    }

    protected boolean isVirtioScsiControllerAttached(Guid vmId) {
        return VmDeviceUtils.hasVirtioScsiController(vmId);
    }

    protected boolean isBalloonEnabled(Guid vmId) {
        return VmDeviceUtils.hasMemoryBalloon(vmId);
    }

    protected boolean isSoundDeviceEnabled(Guid vmId) {
        return VmDeviceUtils.hasSoundDevice(vmId);
    }

    protected boolean hasWatchdog(Guid vmId) {
        return VmDeviceUtils.hasWatchdog(vmId);
    }

    /** Updates the VM's disks from the database */
    protected void updateDisksFromDb() {
        VmHandler.updateDisksFromDb(getVm());
    }

    protected boolean isVolumeFormatSupportedForShareable(VolumeFormat volumeFormat) {
        return volumeFormat == VolumeFormat.RAW;
    }

    protected boolean isVmInUpPausedDownStatus() {
        if (getVm().getStatus() != VMStatus.Up && getVm().getStatus() != VMStatus.Down
                && getVm().getStatus() != VMStatus.Paused) {
            return failVmStatusIllegal();
        }
        return true;
    }

    protected boolean isDiskExistAndAttachedToVm(Disk disk) {
        DiskValidator diskValidator = getDiskValidator(disk);
        return validate(diskValidator.isDiskExists()) && validate(diskValidator.isDiskAttachedToVm(getVm()));
    }

    protected boolean checkDiskUsedAsOvfStore(Disk disk) {
        return checkDiskUsedAsOvfStore(getDiskValidator(disk));
    }

    protected boolean checkDiskUsedAsOvfStore(DiskValidator diskValidator) {
        return validate(diskValidator.isDiskUsedAsOvfStore());
    }

    protected ImageStorageDomainMapDao getImageStorageDomainMapDao() {
        return getDbFacade().getImageStorageDomainMapDao();
    }

    public String getDiskAlias() {
        return getParameters().getDiskInfo().getDiskAlias();
    }

    protected boolean validateDiskVmData() {
        if (getDiskVmElement() == null || getDiskVmElement().getId() == null ||
                !Objects.equals(getDiskVmElement().getId().getVmId(), getVmId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_VM_DATA_MISSING);
        }
        return true;
    }

    protected DiskVmElement getDiskVmElement() {
        return getParameters().getDiskVmElement();
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

    public CinderBroker getCinderBroker() {
        if (cinderBroker == null) {
            cinderBroker = new CinderBroker(getStorageDomainId(), getReturnValue().getExecuteFailedMessages());
        }
        return cinderBroker;
    }

    /**
     * Returns disk's address map by specified VmDevice and DiskInterface
     * (note: for VirtIO_SCSI/SPAPR_VSCSI interfaces, the method updates the VM device's address accordingly).
     * @return disk's address map
     */
    public Map<String, String> getDiskAddressMap(VmDevice vmDevice, DiskInterface diskInterface) {
        String address = vmDevice.getAddress();
        if (diskInterface != DiskInterface.VirtIO_SCSI && diskInterface != DiskInterface.SPAPR_VSCSI) {
            if (StringUtils.isNotBlank(address)) {
                return XmlRpcStringUtils.string2Map(address);
            }
        } else {
            EngineLock vmDiskHotPlugEngineLock = null;
            try {
                vmDiskHotPlugEngineLock = lockVmDiskHotPlugWithWait();
                VM vm = vmDao.get(getParameters().getVmId());
                Map<DiskInterface, Integer> controllerIndexMap =
                        ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();

                int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);
                int sPaprVscsiIndex = controllerIndexMap.get(DiskInterface.SPAPR_VSCSI);

                if (diskInterface == DiskInterface.VirtIO_SCSI) {
                    Map<VmDevice, Integer> vmDeviceUnitMap = VmInfoBuilder.getVmDeviceUnitMapForVirtioScsiDisks(getVm());
                    return getAddressMapForScsiDisk(address, vmDeviceUnitMap, vmDevice, virtioScsiIndex, false);
                } else if (diskInterface == DiskInterface.SPAPR_VSCSI) {
                    Map<VmDevice, Integer> vmDeviceUnitMap = VmInfoBuilder.getVmDeviceUnitMapForSpaprScsiDisks(getVm());
                    return getAddressMapForScsiDisk(address, vmDeviceUnitMap, vmDevice, sPaprVscsiIndex, true);
                }
            } finally {
                LockManagerFactory.getLockManager().releaseLock(vmDiskHotPlugEngineLock);
            }
        }
        return null;
    }

    private Map<String, String> getAddressMapForScsiDisk(String address,
                                       Map<VmDevice, Integer> vmDeviceUnitMap,
                                       VmDevice vmDevice,
                                       int controllerIndex,
                                       boolean reserveFirstAddress) {
        Map<String, String> addressMap;
        int availableUnit = VmInfoBuilder.getAvailableUnitForScsiDisk(vmDeviceUnitMap, reserveFirstAddress);

        // If address has been already set before, verify its uniqueness;
        // Otherwise, set address according to the next available unit.
        if (StringUtils.isNotBlank(address)) {
            addressMap = XmlRpcStringUtils.string2Map(address);
            int unit = Integer.parseInt(addressMap.get(VdsProperties.Unit));
            if (vmDeviceUnitMap.containsValue(unit)) {
                addressMap = VmInfoBuilder.createAddressForScsiDisk(controllerIndex, availableUnit);
            }
        } else {
            addressMap = VmInfoBuilder.createAddressForScsiDisk(controllerIndex, availableUnit);
        }

        // Updating device's address immediately (instead of waiting to VmsMonitoring)
        // to prevent a duplicate unit value (i.e. ensuring a unique unit value).
        updateVmDeviceAddress(addressMap.toString(), vmDevice);

        return addressMap;
    }

    protected void updateVmDeviceAddress(final String address, final VmDevice vmDevice) {
        vmDevice.setAddress(address);
        getCompensationContext().snapshotEntity(vmDevice);
        getCompensationContext().stateChanged();
        getVmDeviceDao().update(vmDevice);
    }

    protected EngineLock lockVmDiskHotPlugWithWait() {
        EngineLock vmDiskHotPlugEngineLock = new EngineLock();
        vmDiskHotPlugEngineLock.setExclusiveLocks(Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_HOT_PLUG,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED)));
        getLockManager().acquireLockWait(vmDiskHotPlugEngineLock);
        return vmDiskHotPlugEngineLock;
    }
}
