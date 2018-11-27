package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConnectManagedBlockStorageDeviceCommandParameters;
import org.ovirt.engine.core.common.action.DisconnectManagedBlockStorageDeviceParameters;
import org.ovirt.engine.core.common.action.SaveManagedBlockStorageDiskDeviceCommandParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@Singleton
public class ManagedBlockStorageCommandUtil {
    @Inject
    private BackendInternal backend;
    @Inject
    private VDSBrokerFrontend resourceManager;
    @Inject
    private VmDeviceDao vmDeviceDao;

    public boolean attachManagedBlockStorageDisks(VM vm, VmHandler vmHandler, VDS vds) {
        if (vm.getDiskMap().isEmpty()) {
            vmHandler.updateDisksFromDb(vm);
        }

        List<ManagedBlockStorageDisk> disks = DisksFilter.filterManagedBlockStorageDisks(vm.getDiskMap().values());
        return disks.stream()
                .allMatch(disk -> this.saveDevices(disk, vds, vm.getId()));
    }

    private boolean saveDevices(ManagedBlockStorageDisk disk, VDS vds, Guid vmId) {
        VDSReturnValue returnValue = attachManagedBlockStorageDisk(disk, vds);

        if (returnValue == null) {
            return false;
        }

        saveAttachedHost(disk, vmId, vds.getId());

        disk.setDevice((Map<String, Object>) returnValue.getReturnValue());
        SaveManagedBlockStorageDiskDeviceCommandParameters parameters =
                new SaveManagedBlockStorageDiskDeviceCommandParameters();
        parameters.setDevice(disk.getDevice());
        parameters.setDiskId(disk.getId());
        parameters.setStorageDomainId(disk.getStorageIds().get(0));
        backend.runInternalAction(ActionType.SaveManagedBlockStorageDiskDevice, parameters);

        return true;
    }

    private void saveAttachedHost(ManagedBlockStorageDisk disk, Guid vmId, Guid vdsId) {
        TransactionSupport.executeInNewTransaction(() -> {
            VmDevice vmDevice = vmDeviceDao.get(new VmDeviceId(disk.getId(), vmId));
            vmDevice.getSpecParams().put(ManagedBlockStorageDisk.ATTACHED_VDS_ID, vdsId);
            vmDeviceDao.update(vmDevice);

            return null;
        });
    }

    private VDSReturnValue attachManagedBlockStorageDisk(ManagedBlockStorageDisk disk, VDS vds) {
        ActionReturnValue returnValue = getConnectionInfo(disk, vds);

        if (!returnValue.getSucceeded()) {
            return null;
        }

        disk.setConnectionInfo(returnValue.getActionReturnValue());
        AttachManagedBlockStorageVolumeVDSCommandParameters params =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(vds, returnValue.getActionReturnValue());
        params.setVolumeId(disk.getImageId());
        VDSReturnValue vdsReturnValue =
                resourceManager.runVdsCommand(VDSCommandType.AttachManagedBlockStorageVolume, params);
        return vdsReturnValue;
    }

    private ActionReturnValue getConnectionInfo(ManagedBlockStorageDisk disk, VDS vds) {
        ConnectManagedBlockStorageDeviceCommandParameters params = new ConnectManagedBlockStorageDeviceCommandParameters();
        params.setDiskId(disk.getId());
        params.setStorageDomainId(disk.getStorageIds().get(0));
        params.setConnectorInfo(vds.getConnectorInfo());
        ActionReturnValue actionReturnValue =
                backend.runInternalAction(ActionType.ConnectManagedBlockStorageDevice, params);
        return actionReturnValue;
    }

    public void disconnectManagedBlockStorageDisks(VM vm, VmHandler vmHandler) {
        if (vm.getDiskMap().isEmpty()) {
            vmHandler.updateDisksFromDb(vm);
        }

        List<ManagedBlockStorageDisk> disks = DisksFilter.filterManagedBlockStorageDisks(vm.getDiskMap().values());
        disks.forEach(disk -> {
            DisconnectManagedBlockStorageDeviceParameters parameters =
                    new DisconnectManagedBlockStorageDeviceParameters();
            parameters.setStorageDomainId(disk.getStorageIds().get(0));
            parameters.setDiskId(disk.getId());
            backend.runInternalAction(ActionType.DisconnectManagedBlockStorageDevice, parameters);
        });

    }

}
