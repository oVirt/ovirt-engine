package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmSlaPolicyParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.dao.qos.CpuQosDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@Singleton
public class VmSlaPolicyUtils {

    @Inject
    private CpuProfileDao cpuProfileDao;

    @Inject
    private DiskProfileDao diskProfileDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private CpuQosDao cpuQosDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private StorageQosDao storageQosDao;

    @Inject
    private BackendInternal backend;

    public List<Guid> getRunningVmsWithCpuProfiles(Collection<Guid> cpuProfileIds) {
        return vmDao.getAllForCpuProfiles(cpuProfileIds).stream()
                .filter(vm -> vm.getStatus().isQualifiedForQosChange())
                .map(VM::getId)
                .collect(Collectors.toList());
    }

    public List<Guid> getRunningVmsWithCpuQos(Guid cpuQosId) {
        List<Guid> cpuProfileIds = cpuProfileDao.getAllForQos(cpuQosId).stream()
                .map(CpuProfile::getId)
                .collect(Collectors.toList());

        if (cpuProfileIds.isEmpty()) {
            return Collections.emptyList();
        }

        return getRunningVmsWithCpuProfiles(cpuProfileIds);
    }

    public Map<Guid, List<DiskImage>> getRunningVmDiskImageMapWithProfiles(Collection<Guid> diskProfileIds) {
        Map<Guid, List<DiskImage>> vmDiskMap = new HashMap<>();

        List<Guid> vmIds = vmDao.getAllForDiskProfiles(diskProfileIds).stream()
                .filter(vm -> vm.getStatus().isQualifiedForQosChange())
                .map(VM::getId)
                .collect(Collectors.toList());

        if (vmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Guid, List<Disk>> attachedDisks = diskDao.getAllForVms(vmIds);
        for (Guid vmId : vmIds) {
            List<DiskImage> updatedDisks = attachedDisks.get(vmId).stream()
                    .filter(disk -> (disk.getDiskStorageType() == DiskStorageType.IMAGE) && disk.getPlugged())
                    .map(DiskImage.class::cast)
                    .filter(disk -> disk.getActive() && diskProfileIds.contains(disk.getDiskProfileId()))
                    .collect(Collectors.toList());

            vmDiskMap.put(vmId, updatedDisks);
        }

        return vmDiskMap;
    }

    public Map<Guid, List<DiskImage>> getRunningVmDiskImageMapWithQos(Guid storageQosId) {
        Set<Guid> profileIds = diskProfileDao.getAllForQos(storageQosId).stream()
                .map(DiskProfile::getId)
                .collect(Collectors.toSet());

        if (profileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return getRunningVmDiskImageMapWithProfiles(profileIds);
    }

    public void refreshVmsCpuQos(List<Guid> vmIds, CpuQos newQos) {
        for (Guid vmId : vmIds) {
            ThreadPoolUtil.execute(() ->
                    backend.runInternalAction(ActionType.VmSlaPolicy,
                            new VmSlaPolicyParameters(vmId, newQos)));
        }
    }

    public void refreshRunningVmsWithCpuQos(Guid cpuQosId, CpuQos newQos) {
        refreshVmsCpuQos(getRunningVmsWithCpuQos(cpuQosId), newQos);
    }

    public void refreshRunningVmsWithCpuProfile(Guid profileId, CpuQos newQos) {
        refreshVmsCpuQos(
                getRunningVmsWithCpuProfiles(Collections.singleton(profileId)),
                newQos);
    }

    /**
     * Refresh CPU QoS of a running VM.
     */
    public void refreshCpuQosOfRunningVm(VM vm) {
        if (!vm.getStatus().isQualifiedForQosChange()) {
            // It only makes sense to try a QoS live change on a running VM.
            throw new IllegalArgumentException(
                    String.format("VM %s is not running. Can't perform a live QoS upgrade", vm.getId())
            );
        }
        Guid vmId = vm.getId();
        List<Guid> vmIds = Arrays.asList(vmId);
        CpuQos cpuQos = cpuQosDao.getCpuQosByVmIds(vmIds).get(vmId);
        if (cpuQos == null) {
            refreshVmsCpuQos(Arrays.asList(vmId), new CpuQos());
        } else {
            refreshVmsCpuQos(Arrays.asList(vmId), cpuQos);
        }
    }

    public void refreshVmsStorageQos(Map<Guid, List<DiskImage>> vmDiskMap, StorageQos newQos) {
        // No QoS means default QoS which means unlimited
        if (newQos == null) {
            newQos = new StorageQos();
        }

        List<ActionParametersBase> params = new ArrayList<>(vmDiskMap.size());
        for (Map.Entry<Guid, List<DiskImage>> entry : vmDiskMap.entrySet()) {
            VmSlaPolicyParameters cmdParams = new VmSlaPolicyParameters(entry.getKey());
            for (DiskImage img : entry.getValue()) {
                cmdParams.getStorageQos().put(img, newQos);
            }
            params.add(cmdParams);
        }

        ThreadPoolUtil.execute(() -> backend.runInternalMultipleActions(ActionType.VmSlaPolicy, params));
    }

    public void refreshRunningVmsWithStorageQos(Guid storageQosId, StorageQos newQos) {
        refreshVmsStorageQos(getRunningVmDiskImageMapWithQos(storageQosId), newQos);
    }

    public void refreshRunningVmsWithDiskProfile(Guid diskProfileId) {
        refreshVmsStorageQos(
                getRunningVmDiskImageMapWithProfiles(Collections.singleton(diskProfileId)),
                storageQosDao.getQosByDiskProfileId(diskProfileId)
        );
    }

    public void refreshRunningVmsWithDiskImage(DiskImage diskImage) {
        Map<Guid, List<DiskImage>> vmDiskMap = vmDao.getVmsListForDisk(diskImage.getId(), false).stream()
                .filter(vm -> vm.getStatus().isQualifiedForQosChange())
                .map(VM::getId)
                .collect(Collectors.toMap(
                        vmId -> vmId,
                        vmId -> Collections.singletonList(diskImage)
                ));

        refreshVmsStorageQos(vmDiskMap, storageQosDao.getQosByDiskProfileId(diskImage.getDiskProfileId()));
    }
}
