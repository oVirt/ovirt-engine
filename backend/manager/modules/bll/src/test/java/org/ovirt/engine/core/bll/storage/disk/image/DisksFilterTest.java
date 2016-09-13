package org.ovirt.engine.core.bll.storage.disk.image;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;

public class DisksFilterTest {

    @Test
    public void testFilterNonImageDisks() {
        Disk lunDisk = createDisk(DiskStorageType.LUN, false, false, false);
        Disk imageDisk = createDisk(DiskStorageType.IMAGE, false, false, true);
        Disk cinderDisk = createDisk(DiskStorageType.CINDER, false, false, true);

        List<Disk> disksList = Arrays.asList(lunDisk, imageDisk, cinderDisk);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList);

        assertEquals(1, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(imageDisk));
    }

    @Test
    public void testFilterShareableDisks() {
        Disk shareableDisk1 = createDisk(DiskStorageType.IMAGE, true, true, false);
        Disk shareableDisk2 = createDisk(DiskStorageType.IMAGE, false, true, false);
        Disk nonShareableDisk1 = createDisk(DiskStorageType.IMAGE, true, false, true);
        Disk nonShareableDisk2 = createDisk(DiskStorageType.IMAGE, false, false, true);

        List<Disk> disksList = Arrays.asList(shareableDisk1, nonShareableDisk1, shareableDisk2, nonShareableDisk2);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList, ONLY_NOT_SHAREABLE);

        assertEquals(2, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(nonShareableDisk1, nonShareableDisk2));
    }

    @Test
    public void testFilterNonActiveDisks() {
        Disk activeDisk1 = createDisk(DiskStorageType.IMAGE, true, true, false);
        Disk activeDisk2 = createDisk(DiskStorageType.IMAGE, true, false, true);
        Disk nonActiveDisk1 = createDisk(DiskStorageType.IMAGE, false, true, false);
        Disk nonActiveDisk2 = createDisk(DiskStorageType.IMAGE, false, false, true);

        List<Disk> disksList = Arrays.asList(activeDisk1, nonActiveDisk1, activeDisk2, nonActiveDisk2);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList, ONLY_ACTIVE);

        assertEquals(2, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(activeDisk1, activeDisk2));
    }

    @Test
    public void testFilterNonSnapableDisks() {
        Disk snapableDisk1 = createDisk(DiskStorageType.IMAGE, true, false, true);
        Disk snapableDisk2 = createDisk(DiskStorageType.IMAGE, false, false, true);
        Disk nonSnapableDisk1 = createDisk(DiskStorageType.IMAGE, true, true, false);
        Disk nonSnapableDisk2 = createDisk(DiskStorageType.IMAGE, false, true, false);

        List<Disk> disksList = Arrays.asList(snapableDisk1, nonSnapableDisk1, snapableDisk2, nonSnapableDisk2);
        List<DiskImage> filteredList = DisksFilter.filterImageDisks(disksList, ONLY_SNAPABLE);

        assertEquals(2, filteredList.size());
        assertThat(filteredList, containsInAnyOrder(snapableDisk1, snapableDisk2));
    }

    private Disk createDisk(DiskStorageType type, boolean isActive, boolean isShareable, boolean isSnapable) {
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
        }
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
