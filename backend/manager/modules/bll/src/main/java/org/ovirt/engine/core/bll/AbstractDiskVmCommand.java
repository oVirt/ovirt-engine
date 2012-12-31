package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.storage.StorageHelperDirector;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;

@CustomLogFields({ @CustomLogField("DiskAlias") })
public abstract class AbstractDiskVmCommand<T extends VmDiskOperationParameterBase> extends VmCommand<T> {

    private static final long serialVersionUID = -4596432908703489958L;

    public AbstractDiskVmCommand(T parameters) {
        super(parameters);
    }

    protected AbstractDiskVmCommand(Guid commandId) {
        super(commandId);
    }

    protected void performPlugCommnad(VDSCommandType commandType,
            Disk disk, VmDevice vmDevice) {
        if (disk.getDiskStorageType() == DiskStorageType.LUN) {
            LunDisk lunDisk = (LunDisk) disk;
            if (commandType == VDSCommandType.HotPlugDisk) {
                LUNs lun = lunDisk.getLun();
                updateLUNConnectionsInfo(lun);
                if (!StorageHelperDirector.getInstance()
                        .getItem(getLUNStorageType(lun))
                        .ConnectStorageToLunByVdsId(null,
                                getVm().getRunOnVds().getValue(),
                                lun,
                                getVm().getStoragePoolId())) {
                    throw new VdcBLLException(VdcBllErrors.StorageServerConnectionError);
                }
            }
        }
        runVdsCommand(commandType, new HotPlugDiskVDSParameters(getVm().getRunOnVds().getValue(),
                getVm().getId(), disk, vmDevice));
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
        List<VmNetworkInterface> vmInterfaces = getVmNetworkInterfaceDao().getAllForVm(getVmId());
        List<Disk> allVmDisks = new ArrayList<Disk>(getVm().getDiskMap().values());
        allVmDisks.add(diskInfo);

        return checkPciAndIdeLimit(getVm().getNumOfMonitors(),
                vmInterfaces,
                allVmDisks,
                getReturnValue().getCanDoActionMessages());
    }

    protected boolean isDiskCanBeAddedToVm(Disk diskInfo) {
        boolean returnValue = true;
        updateDisksFromDb();
        if (returnValue && diskInfo.isBoot()) {
            for (Disk disk : getVm().getDiskMap().values()) {
                if (disk.isBoot()) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE);
                    getReturnValue().getCanDoActionMessages().add(
                            String.format("$DiskName %1$s", disk.getDiskAlias()));
                    break;
                }
            }
        }
        return returnValue;
    }

    /** Update's the VM's disks from the database */
    protected void updateDisksFromDb() {
        VmHandler.updateDisksFromDb(getVm());
    }

    protected boolean isVersionSupportedForShareable(Disk disk, String compVersion) {
        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            return Config.<Boolean> GetValue(ConfigValues.ShareableDiskEnabled, compVersion);
        }
        return true;
    }

    protected boolean isVolumeFormatSupportedForShareable(VolumeFormat volumeFormat) {
        return volumeFormat == VolumeFormat.RAW;
    }

    protected boolean isVmUpOrDown() {
        if (getVm().getStatus() != VMStatus.Up && getVm().getStatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
            return false;
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

    protected boolean isInterfaceSupportedForPlugUnPlug(Disk disk) {
        if (!DiskInterface.VirtIO.equals(disk.getDiskInterface())) {
            addCanDoActionMessage(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_VIRTIO);
            return false;
        }
        return true;
    }

    private boolean isDiskExistInVm(Disk disk) {
        List<VM> listVms = getVmDAO().getVmsListForDisk(disk.getId());
        for (VM vm : listVms) {
            if (vm.getId().equals(getVmId())) {
                return true;
            }
        }
        return false;
    }

    protected ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    protected DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
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

}
