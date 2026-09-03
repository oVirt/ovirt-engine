package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConnectManagedBlockStorageDeviceCommandParameters;
import org.ovirt.engine.core.common.action.DisconnectManagedBlockStorageDeviceParameters;
import org.ovirt.engine.core.common.action.SaveManagedBlockStorageDiskDeviceCommandParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedBlockStorageCommandUtil {
    private static final Logger log = LoggerFactory.getLogger(ManagedBlockStorageCommandUtil.class);

    @Inject
    private BackendInternal backend;
    @Inject
    private VDSBrokerFrontend resourceManager;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private StorageDomainDao storageDomainDao;


    public boolean attachManagedBlockStorageDisks(VM vm, VmHandler vmHandler, VDS vds) {
        return attachManagedBlockStorageDisks(vm, vmHandler, vds, false);
    }

    public boolean attachManagedBlockStorageDisks(VM vm,
            VmHandler vmHandler,
            VDS vds,
            boolean isLiveMigration) {
        if (vm.getDiskMap().isEmpty()) {
            vmHandler.updateDisksFromDb(vm);
        }

        List<ManagedBlockStorageDisk> disks = DisksFilter.filterManagedBlockStorageDisks(vm.getDiskMap().values());
        if (!disks.isEmpty() && vds.getConnectorInfo() == null) {
            AuditLogable event = new AuditLogableImpl();
            event.addCustomValue("VdsName", vds.getName());
            event.addCustomValue("VmName", vm.getName());
            auditLogDirector.log(event, AuditLogType.CONNECTOR_INFO_MISSING_ON_VDS);
            return false;
        }
        return disks.stream()
                .allMatch(disk -> {
                    VmDevice vmDevice = vmDeviceDao.get(new VmDeviceId(disk.getId(), vm.getId()));
                    return this.saveDevices(disk, vds, vmDevice, isLiveMigration);
                });
    }

    public boolean attachManagedBlockStorageDisksOnHost(Collection<ManagedBlockStorageDisk> disks,
            VDS vds,
            Guid vmOrTemplateEntityId) {
        if (CollectionUtils.isEmpty(disks)) {
            return true;
        }
        if (vds.getConnectorInfo() == null) {
            AuditLogable event = new AuditLogableImpl();
            event.addCustomValue("VdsName", vds.getName());
            event.addCustomValue("VmName", vmOrTemplateEntityId != null ? vmOrTemplateEntityId.toString() : "");
            auditLogDirector.log(event, AuditLogType.CONNECTOR_INFO_MISSING_ON_VDS);
            return false;
        }
        return disks.stream()
                .allMatch(disk -> saveDevices(
                        disk,
                        vds,
                        vmDeviceDao.get(new VmDeviceId(disk.getId(), vmOrTemplateEntityId)),
                        false));
    }

    public boolean saveDevices(ManagedBlockStorageDisk disk,
            VDS vds,
            VmDevice vmDevice) {
        return saveDevices(disk, vds, vmDevice, false);
    }


    public boolean saveDevices(ManagedBlockStorageDisk disk,
            VDS vds,
            VmDevice vmDevice,
            boolean isLiveMigration) {
        VDSReturnValue returnValue = attachManagedBlockStorageDisk(disk, vds);

        if (returnValue == null) {
            return false;
        }

        saveAttachedHost(vmDevice, vds.getId(), isLiveMigration);

        disk.setDevice((Map<String, Object>) returnValue.getReturnValue());
        vmInfoBuildUtils.setManagedDriverType(disk);

        SaveManagedBlockStorageDiskDeviceCommandParameters parameters =
                new SaveManagedBlockStorageDiskDeviceCommandParameters();
        parameters.setDevice(disk.getDevice());
        parameters.setDiskId(disk.getImageId());
        parameters.setStorageDomainId(disk.getStorageIds().get(0));
        ActionReturnValue saveDeviceReturnValue =
                backend.runInternalAction(ActionType.SaveManagedBlockStorageDiskDevice, parameters);

        return saveDeviceReturnValue.getSucceeded();
    }

    private void saveAttachedHost(VmDevice vmDevice,
            Guid vdsId,
            boolean isLiveMigration) {
        if (vmDevice == null) {
            log.warn(
                    "Managed-block attach: vm_device is null; skipping ATTACHED_VDS_ID persistence for VDS {}",
                    vdsId);
            return;
        }
        TransactionSupport.executeInNewTransaction(() -> {
            Map<String, Object> specParams = new HashMap<>();
            if (isLiveMigration) {
                specParams.put(ManagedBlockStorageDisk.DEST_VDS_ID, vdsId);
            } else {
                specParams.put(ManagedBlockStorageDisk.ATTACHED_VDS_ID, vdsId);
            }

            if (vmDevice.getSpecParams() != null) {
                vmDevice.getSpecParams().putAll(specParams);
            } else {
                vmDevice.setSpecParams(specParams);
            }

            vmDeviceDao.update(vmDevice);

            return null;
        });
    }

    public VDSReturnValue attachManagedBlockStorageDisk(ManagedBlockStorageDisk disk, VDS vds) {
        ActionReturnValue returnValue = getConnectionInfo(disk, vds);

        if (!returnValue.getSucceeded()) {
            return null;
        }

        disk.setConnectionInfo(returnValue.getActionReturnValue());
        AttachManagedBlockStorageVolumeVDSCommandParameters params =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(vds,
                        returnValue.getActionReturnValue(),
                        disk.getStorageIds().get(0));
        params.setVolumeId(disk.getImageId());
        return resourceManager.runVdsCommand(VDSCommandType.AttachManagedBlockStorageVolume, params);
    }

    private ActionReturnValue getConnectionInfo(ManagedBlockStorageDisk disk, VDS vds) {
        ConnectManagedBlockStorageDeviceCommandParameters params = new ConnectManagedBlockStorageDeviceCommandParameters();
        params.setDiskId(disk.getImageId());
        params.setStorageDomainId(findManagedBlockStorageDomainId(disk));
        params.setConnectorInfo(vds.getConnectorInfo());
        return backend.runInternalAction(ActionType.ConnectManagedBlockStorageDevice, params);
    }

    public boolean disconnectManagedBlockStorageDisks(VM vm, VmHandler vmHandler) {
        return disconnectManagedBlockStorageDisks(vm, vmHandler, false);
    }

    public boolean disconnectManagedBlockStorageDisks(VM vm, VmHandler vmHandler, boolean removeDest) {
        if (vm.getDiskMap().isEmpty()) {
            vmHandler.updateDisksFromDb(vm);
        }

        List<ManagedBlockStorageDisk> disks =
                DisksFilter.filterManagedBlockStorageDisks(vm.getDiskMap().values());
        return disks.stream().allMatch(disk -> disconnectManagedBlockStorageDisk(vm, disk, removeDest, null));
    }

    public boolean disconnectManagedBlockStorageDisk(VM vm, DiskImage disk, boolean removeDest) {
        return disconnectManagedBlockStorageDisk(vm, disk, removeDest, null);
    }

    public boolean disconnectManagedBlockStorageDisk(VM vm,
            DiskImage disk,
            boolean removeDest,
            Guid fallbackVdsIdWhenNoVmDevice) {
        VmDevice vmDevice = vmDeviceDao.get(new VmDeviceId(disk.getId(), vm.getId()));
        if (vmDevice == null) {
            if (!removeDest && fallbackVdsIdWhenNoVmDevice != null
                    && !Guid.isNullOrEmpty(fallbackVdsIdWhenNoVmDevice)) {
                log.debug(
                        "Managed-block disconnect: no vm_device for disk {} on {}; detaching from host {}",
                        disk.getId(),
                        vm.getId(),
                        fallbackVdsIdWhenNoVmDevice);
                return disconnectManagedBlockStorageDeviceFromHost(disk, fallbackVdsIdWhenNoVmDevice);
            }
            log.warn(
                    "Managed-block disconnect: no vm_device for disk {} on vm/template {}, and no fallback host; "
                            + "cannot determine VDS for detach",
                    disk.getId(),
                    vm.getId());
            return false;
        }
        DisconnectManagedBlockStorageDeviceParameters parameters =
                new DisconnectManagedBlockStorageDeviceParameters();
        parameters.setStorageDomainId(disk.getStorageIds().get(0));
        parameters.setDiskId(disk.getImageId());

        // In case of a live migration failure we want to detach from the destination
        Guid vdsId = removeDest ? (Guid) vmDevice.getSpecParams().get(ManagedBlockStorageDisk.DEST_VDS_ID) :
                (Guid) vmDevice.getSpecParams().get(ManagedBlockStorageDisk.ATTACHED_VDS_ID);

        // Attach has likely failed so we have nothing to disconnect
        if (vdsId == null) {
            return false;
        }

        // Disk is being disconnected as part of live migration
        Guid destVdsId = (Guid) vmDevice.getSpecParams().get(ManagedBlockStorageDisk.DEST_VDS_ID);
        if (destVdsId == null) {
            vmDevice.getSpecParams().remove(ManagedBlockStorageDisk.ATTACHED_VDS_ID);
        } else {
            if (!removeDest) {
                vmDevice.getSpecParams().put(ManagedBlockStorageDisk.ATTACHED_VDS_ID, destVdsId);
            }

            // The device is now attached only to the destination host
            vmDevice.getSpecParams().remove(ManagedBlockStorageDisk.DEST_VDS_ID);
        }

        parameters.setVdsId(vdsId);
        ActionReturnValue returnValue =
                backend.runInternalAction(ActionType.DisconnectManagedBlockStorageDevice, parameters);

        if (returnValue.getSucceeded()) {
            TransactionSupport.executeInNewTransaction(() -> {
                vmDeviceDao.update(vmDevice);
                return null;
            });

            return true;
        }

        return false;
    }

    public boolean disconnectManagedBlockStorageDeviceFromHost(DiskImage disk, Guid vdsId) {
        DisconnectManagedBlockStorageDeviceParameters parameters =
                new DisconnectManagedBlockStorageDeviceParameters();
        parameters.setStorageDomainId(findManagedBlockStorageDomainId(disk));
        parameters.setDiskId(disk.getId());
        parameters.setVdsId(vdsId);

        ActionReturnValue returnValue =
                backend.runInternalAction(ActionType.DisconnectManagedBlockStorageDevice, parameters);

        return returnValue.getSucceeded();
    }

    private Guid findManagedBlockStorageDomainId(DiskImage disk) {
        // Helper for finding a Managed Block Storage domain among disk's storage domains list,
        // in case that list has more than one domain (such as template disks).
        return disk.getStorageIds()
                .stream()
                .filter(s -> storageDomainDao.get(s).getStorageType() == StorageType.MANAGED_BLOCK_STORAGE)
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }
}
