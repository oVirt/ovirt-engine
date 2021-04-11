package org.ovirt.engine.core.bll.kubevirt;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;

import io.kubernetes.client.models.V1ObjectMeta;
import kubevirt.io.V1VirtualMachine;
import kubevirt.io.V1Volume;

@ApplicationScoped
public class VmUpdater {
    @Inject
    private Instance<BackendInternal> backend;

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private DiskImageDao diskImageDao;

    public boolean addVm(V1VirtualMachine vm, Guid clusterId) {
        V1ObjectMeta metadata = vm.getMetadata();
        List<VmStatic> dbVms = vmStaticDao.getAllByName(metadata.getName());
        Predicate<VmStatic> isOnSameClusterAndNamespace =
                v -> clusterId.equals(v.getClusterId()) && metadata.getNamespace().equals(v.getNamespace());
        if (dbVms.stream().anyMatch(isOnSameClusterAndNamespace)) {
            return false;
        }

        VmStatic vmStatic = EntityMapper.toOvirtVm(vm, clusterId);
        vmStatic.setBiosType(BiosType.Q35_SEA_BIOS);

        AddVmParameters params = new AddVmParameters(vmStatic);
        params.setSoundDeviceEnabled(false);
        params.setVirtioScsiEnabled(false);

        // at some point we may want to call AddUnmanagedVm
        ActionReturnValue retVal = backend.get().runInternalAction(ActionType.AddVm, params);
        if (!retVal.getSucceeded()) {
            return false;
        }

        final Guid vmId = retVal.getActionReturnValue();

        List<PVCDisk> pvcs = toPvcs(vm, clusterId);
        List<DiskImage> disks = diskImageDao.getAllForStorageDomain(clusterId);
        // TODO: attachment should not fail as the PVC is already attached
        // in kubevirt but maybe that can fail due to a database issue?
        pvcs.stream().map(pvc -> match(pvc, disks, vmId)).filter(Objects::nonNull).forEach(this::attach);
        return true;
    }

    private void attach(DiskVmElement dve) {
        backend.get()
                .runInternalAction(ActionType.AttachDiskToVm,
                        new AttachDetachVmDiskParameters(dve));
    }

    private DiskVmElement match(PVCDisk pvc, List<DiskImage> disks, Guid vmId) {
        Optional<DiskImage> optionalDisk = disks.stream().filter(d -> pvc.equals(d)).findFirst();
        if (optionalDisk.isEmpty()) {
            return null;
        }
        DiskImage disk = optionalDisk.get();
        DiskVmElement dve = new DiskVmElement(disk.getId(), vmId);
        // TODO: bootable and interface properties
        dve.setDiskInterface(DiskInterface.VirtIO);
        return dve;
    }

    private List<PVCDisk> toPvcs(V1VirtualMachine vm, Guid clusterId) {
        String namespace = vm.getMetadata().getNamespace();
        List<V1Volume> volumes = vm.getSpec().getTemplate().getSpec().getVolumes();
        return volumes.stream()
                .map(V1Volume::getPersistentVolumeClaim)
                .filter(Objects::nonNull)
                .map(pvc -> {
                    PVCDisk disk = new PVCDisk(clusterId);
                    disk.setName(pvc.getClaimName());
                    disk.setNamespace(namespace);
                    return disk;
                })
                .collect(Collectors.toList());
    }

    public boolean removeVm(Guid vmId) {
        ActionReturnValue returnValue =
                backend.get().runInternalAction(ActionType.RemoveVm, new RemoveVmParameters(vmId, true));
        return returnValue.getSucceeded();
    }

    public boolean updateVM(V1VirtualMachine oldVm, V1VirtualMachine newVm, Guid clusterId) {
        VmStatic oldStatic = EntityMapper.toOvirtVm(oldVm, clusterId);
        VmStatic newStatic = EntityMapper.toOvirtVm(newVm, clusterId);

        Set<String> fields = ObjectIdentityChecker.getChangedFields(oldStatic, newStatic);
        if (fields.isEmpty()) {
            return false;
        }
        VmManagementParametersBase params = new VmManagementParametersBase(newStatic);
        params.setApplyChangesLater(true);
        ActionReturnValue returnValue =
                backend.get().runInternalAction(ActionType.UpdateVm, params);
        return returnValue.getSucceeded();
    }
}
