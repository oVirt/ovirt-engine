package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmSlaPolicyParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VM;
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
    private DiskDao diskDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private BackendInternal backend;

    public List<Guid> getRunningVmsWithCpuProfiles(Collection<Guid> cpuProfileIds) {

        List<Guid> guids = new ArrayList<>();
        for (VM vm : vmDao.getAllForCpuProfiles(cpuProfileIds)) {
            if (vm.getStatus().isQualifiedForQosChange()) {
                guids.add(vm.getId());
            }
        }
        return guids;
    }

    public List<Guid> getRunningVmsWithCpuQos(Guid cpuQosId) {
        List<Guid> cpuProfileIds = Entities.getIds(cpuProfileDao.getAllForQos(cpuQosId));
        if (cpuProfileIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getRunningVmsWithCpuProfiles(cpuProfileIds);
    }

    public Map<Guid, List<DiskImage>> getRunningVmDiskImageMapWithProfiles(Collection<Guid> diskProfileIds) {
        Map<Guid, List<DiskImage>> vmDiskMap = new HashMap<>();

        List<Guid> vmIds = new ArrayList<>();
        for (VM vm : vmDao.getAllForDiskProfiles(diskProfileIds)) {
            if (vm.getStatus().isQualifiedForQosChange()) {
                vmIds.add(vm.getId());
            }
        }
        if (vmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Guid, List<Disk>> attachedDisks = diskDao.getAllForVms(vmIds);
        for (Guid vmId : vmIds) {
            List<DiskImage> updatedDisks = new ArrayList<>();
            for (Disk disk : attachedDisks.get(vmId)) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE && ((DiskImage) disk).getActive()
                        && diskProfileIds.contains(((DiskImage) disk).getDiskProfileId())) {
                    updatedDisks.add((DiskImage) disk);
                }
            }
            vmDiskMap.put(vmId, updatedDisks);
        }

        return vmDiskMap;
    }

    public Map<Guid, List<DiskImage>> getRunningVmDiskImageMapWithQos(Guid storageQosId) {
        Set<Guid> profileIds = new HashSet<>(Entities.getIds(diskProfileDao.getAllForQos(storageQosId)));

        if (profileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return getRunningVmDiskImageMapWithProfiles(profileIds);
    }

    public void refreshVmsCpuQos(List<Guid> vmIds, final CpuQos newQos) {
        for (final Guid vmId : vmIds) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override public void run() {
                    backend.runInternalAction(VdcActionType.VmSlaPolicy,
                            new VmSlaPolicyParameters(vmId, newQos));
                }
            });
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

    public void refreshVmsStorageQos(Map<Guid, List<DiskImage>> vmDiskMap, StorageQos newQos) {
        for (Map.Entry<Guid, List<DiskImage>> entry : vmDiskMap.entrySet()) {
            final VmSlaPolicyParameters cmdParams = new VmSlaPolicyParameters(entry.getKey());

            for (DiskImage img : entry.getValue()) {
                cmdParams.getStorageQos().put(img, newQos);
            }

            ThreadPoolUtil.execute(new Runnable() {
                @Override public void run() {
                    backend.runInternalAction(VdcActionType.VmSlaPolicy, cmdParams);
                }
            });
        }
    }

    public void refreshRunningVmsWithStorageQos(Guid storageQosId, StorageQos newQos) {
        refreshVmsStorageQos(getRunningVmDiskImageMapWithQos(storageQosId), newQos);
    }

    public void refreshRunningVmsWithDiskProfile(Guid diskProfileId, StorageQos newQos) {
        refreshVmsStorageQos(
                getRunningVmDiskImageMapWithProfiles(Collections.singleton(diskProfileId)),
                newQos);
    }
}
