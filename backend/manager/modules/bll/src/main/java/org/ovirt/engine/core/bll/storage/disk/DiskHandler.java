package org.ovirt.engine.core.bll.storage.disk;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;

@Singleton
public class DiskHandler {

    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    /**
     * loads the disk info for the active snapshot, for luns the lun disk will be returned.
     */
    public Disk loadActiveDisk(Guid diskId) {
        return diskDao.get(diskId);
    }

    /**
     * Loads a disk info for selected snapshot, if no snapshot ID was specified it will load the active snapshot
     */
    public Disk loadDiskFromSnapshot(Guid diskId, Guid snapshotId) {
        return snapshotId == null ?  loadActiveDisk(diskId) :
                diskImageDao.getDiskSnapshotForVmSnapshot(diskId, snapshotId);
    }

    public Map<Disk, DiskVmElement> getDiskToDiskVmElementMap(Guid vmId, Map<Guid, ? extends Disk> disksMap) {
        return getDiskToDiskVmElementMap(disksMap, diskVmElementDao.getAllForVm(vmId));
    }

    public Map<Disk, DiskVmElement> getDiskToDiskVmElementMap(Collection<DiskImage> disks,
            Collection<DiskVmElement> diskVmElements) {
        return getDiskToDiskVmElementMap(disks.stream().collect(Collectors.toMap(Disk::getId, Function.identity())),
                diskVmElements);
    }

    private Map<Disk, DiskVmElement> getDiskToDiskVmElementMap(Map<Guid, ? extends Disk> disksMap,
            Collection<DiskVmElement> diskVmElements) {
        return diskVmElements.stream()
                .filter(dve -> disksMap.keySet().contains(dve.getDiskId()))
                .collect(Collectors.toMap(diskVmElement ->
                        disksMap.get(diskVmElement.getId().getDeviceId()), Function.identity()));
    }

    /**
     * Loads the {@code DiskVmElement} with respect to the specified disk and VM/template
     * and update the specified disk.
     * @param disk the disk to update
     * @param vmId the identifier of the vm/template for which to load the DiskVmElement
     */
    public void updateDiskVmElementFromDb(Disk disk, Guid vmId) {
        DiskVmElement dve = diskVmElementDao.get(new VmDeviceId(disk.getId(), vmId));
        disk.setDiskVmElements(Collections.singletonList(dve));
    }
}
