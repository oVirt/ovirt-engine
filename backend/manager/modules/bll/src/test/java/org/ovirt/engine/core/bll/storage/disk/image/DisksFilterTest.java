package org.ovirt.engine.core.bll.storage.disk.image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;

public class DisksFilterTest {

    @Test
    public void testFilterNonImageDisks() {
        Disk lunDisk = createDisk(DiskStorageType.LUN, false, false, false, false);
        Disk imageDisk = createDisk(DiskStorageType.IMAGE, false, false, true, false);
        Disk cinderDisk = createDisk(DiskStorageType.CINDER, false, false, true, false);
        Disk managedBlockDisk = createDisk(DiskStorageType.MANAGED_BLOCK_STORAGE, false, false, true, false);


        List<Disk> disksList = Arrays.asList(lunDisk, imageDisk, cinderDisk, managedBlockDisk);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList);

        assertEquals(1, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(imageDisk));
    }

    @Test
    public void testFilterNonLunDisks() {
        Disk lunDisk = createDisk(DiskStorageType.LUN, false, false, false, false);
        Disk imageDisk = createDisk(DiskStorageType.IMAGE, false, false, true, false);
        Disk cinderDisk = createDisk(DiskStorageType.CINDER, false, false, true, false);
        Disk managedBlockDisk = createDisk(DiskStorageType.MANAGED_BLOCK_STORAGE, false, false, true, false);


        List<Disk> disksList = Arrays.asList(lunDisk, imageDisk, cinderDisk, managedBlockDisk);
        List<LunDisk> filteredList = DisksFilter.filterLunDisks(disksList);

        assertEquals(1, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(lunDisk));
    }

    @Test
    public void testFilterNonCinderDisks() {
        Disk lunDisk = createDisk(DiskStorageType.LUN, false, false, false, false);
        Disk imageDisk = createDisk(DiskStorageType.IMAGE, false, false, true, false);
        Disk cinderDisk = createDisk(DiskStorageType.CINDER, false, false, true, false);
        Disk managedBlockDisk = createDisk(DiskStorageType.MANAGED_BLOCK_STORAGE, false, false, true, false);


        List<Disk> disksList = Arrays.asList(lunDisk, imageDisk, cinderDisk, managedBlockDisk);
        List<CinderDisk> filteredList = DisksFilter.filterCinderDisks(disksList);

        assertEquals(1, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(cinderDisk));
    }

    @Test
    public void testFilterNonManagedBlockDiskDisks() {
        Disk lunDisk = createDisk(DiskStorageType.LUN, false, false, false, false);
        Disk imageDisk = createDisk(DiskStorageType.IMAGE, false, false, true, false);
        Disk cinderDisk = createDisk(DiskStorageType.CINDER, false, false, true, false);
        Disk managedBlockDisk = createDisk(DiskStorageType.MANAGED_BLOCK_STORAGE, false, false, true, false);


        List<Disk> disksList = Arrays.asList(lunDisk, imageDisk, cinderDisk, managedBlockDisk);
        List<ManagedBlockStorageDisk> filteredList = DisksFilter.filterManagedBlockStorageDisks(disksList);

        assertEquals(1, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(managedBlockDisk));
    }

    @Test
    public void testFilterShareableDisks() {
        Disk shareableDisk1 = createDisk(DiskStorageType.IMAGE, true, true, false, false);
        Disk shareableDisk2 = createDisk(DiskStorageType.IMAGE, false, true, false, false);
        Disk nonShareableDisk1 = createDisk(DiskStorageType.IMAGE, true, false, true, false);
        Disk nonShareableDisk2 = createDisk(DiskStorageType.IMAGE, false, false, true, false);

        List<Disk> disksList = Arrays.asList(shareableDisk1, nonShareableDisk1, shareableDisk2, nonShareableDisk2);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList, ONLY_NOT_SHAREABLE);

        assertEquals(2, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(nonShareableDisk1, nonShareableDisk2));
    }

    @Test
    public void testFilterNonActiveDisks() {
        Disk activeDisk1 = createDisk(DiskStorageType.IMAGE, true, true, false, false);
        Disk activeDisk2 = createDisk(DiskStorageType.IMAGE, true, false, true, false);
        Disk nonActiveDisk1 = createDisk(DiskStorageType.IMAGE, false, true, false, false);
        Disk nonActiveDisk2 = createDisk(DiskStorageType.IMAGE, false, false, true, false);

        List<Disk> disksList = Arrays.asList(activeDisk1, nonActiveDisk1, activeDisk2, nonActiveDisk2);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList, ONLY_ACTIVE);

        assertEquals(2, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(activeDisk1, activeDisk2));
    }

    @Test
    public void testFilterNonSnapableDisks() {
        Disk snapableDisk1 = createDisk(DiskStorageType.IMAGE, true, false, true, false);
        Disk snapableDisk2 = createDisk(DiskStorageType.IMAGE, false, false, true, false);
        Disk nonSnapableDisk1 = createDisk(DiskStorageType.IMAGE, true, true, false, false);
        Disk nonSnapableDisk2 = createDisk(DiskStorageType.IMAGE, false, true, false, false);

        List<Disk> disksList = Arrays.asList(snapableDisk1, nonSnapableDisk1, snapableDisk2, nonSnapableDisk2);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList, ONLY_SNAPABLE);

        assertEquals(2, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(snapableDisk1, snapableDisk2));
    }

    @Test
    public void testFilterUnpluggedDisks() {
        Disk pluggedDisk1 = createDisk(DiskStorageType.IMAGE, true, false, true, true);
        Disk pluggedDisk2 = createDisk(DiskStorageType.IMAGE, false, false, true, true);
        Disk unpluggedDisk1 = createDisk(DiskStorageType.IMAGE, true, true, false, false);
        Disk unpluggedDisk2 = createDisk(DiskStorageType.IMAGE, false, true, false, false);

        List<Disk> disksList = Arrays.asList(pluggedDisk1, unpluggedDisk1, pluggedDisk2, unpluggedDisk2);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList, ONLY_PLUGGED);

        assertEquals(2, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(pluggedDisk1, pluggedDisk2));
    }

    private Disk createDisk
            (DiskStorageType type, boolean isActive, boolean isShareable, boolean isSnapable, boolean isPlugged) {
        Disk disk = null;
        switch (type) {
        case IMAGE:
            disk = new DiskImage();
            setDiskImageProperties((DiskImage) disk, isActive, isShareable, isSnapable);
            break;
        case LUN:
            if (isSnapable) {
                throw new IllegalArgumentException("A LUN disk cannot be snapable");
            }
            disk = new LunDisk();
            break;
        case CINDER:
            disk = new CinderDisk();
            setDiskImageProperties((DiskImage) disk, isActive, isShareable, isSnapable);
            break;
        case MANAGED_BLOCK_STORAGE:
            disk = new ManagedBlockStorageDisk();
            setDiskImageProperties((DiskImage) disk, isActive, isShareable, isSnapable);
            break;
        }
        disk.setPlugged(isPlugged);
        return disk;
    }

    private void setDiskImageProperties(DiskImage disk, boolean isActive, boolean isShareable, boolean isSnapable) {
        if (isShareable == isSnapable) {
            throw new IllegalArgumentException("An image disk cannot be both sharable and snapable or vice versa");
        }
        disk.setActive(isActive);
        disk.setShareable(isShareable);
    }
}
