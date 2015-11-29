package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.compat.Guid;

/** A test case for {@link ImagesHandler} */
public class ImagesHandlerTest {

    /** The prefix to use for all tests */
    private static final String prefix = "PREFIX";

    /** The disks to use for testing */
    private DiskImage disk1;
    private DiskImage disk2;
    private DiskImage disk3;
    private LunDisk lunDisk1;
    private LunDisk lunDisk2;

    @Before
    public void setUp() {
        disk1 = new DiskImage();
        disk2 = new DiskImage();
        disk3 = new DiskImage();
        lunDisk1 = new LunDisk();
        lunDisk2 = new LunDisk();
    }

    @Test
    public void testGetSuggestedDiskAliasNullDisk() {
        assertEquals("null disk does not give the default name",
                prefix + ImagesHandler.DISK + ImagesHandler.DefaultDriveName,
                ImagesHandler.getSuggestedDiskAlias(null, prefix, 1));
    }

    @Test
    public void testGetSuggestedDiskAliasNullAliasDisk() {
        disk1.setDiskAlias(null);
        assertEquals("disk with null alias does not give the default name",
                prefix + ImagesHandler.DISK + ImagesHandler.DefaultDriveName,
                ImagesHandler.getSuggestedDiskAlias(disk1, prefix, 1));
    }

    @Test
    public void testGetSuggestedDiskAliasNotNullAliasDisk() {
        disk1.setDiskAlias("someAlias");
        assertEquals("a new alias was generated instead of returning the pre-defined one",
                disk1.getDiskAlias(),
                ImagesHandler.getSuggestedDiskAlias(disk1, prefix, 1));
    }

    @Test
    public void testGetDiskAliasWithDefaultNullAlias() {
        assertEquals("default", ImagesHandler.getDiskAliasWithDefault(disk1, "default"));
    }

    @Test
    public void testGetDiskAliasWithDefaultNotNullAlias() {
        disk1.setDiskAlias("alias");
        assertEquals("alias", ImagesHandler.getDiskAliasWithDefault(disk1, "default"));
    }

    @Test
    public void testGetAllStorageIdsForImageIds() {
        Guid sdIdShared = Guid.newGuid();
        Guid sdId1 = Guid.newGuid();
        Guid sdId2 = Guid.newGuid();

        disk1.setStorageIds(new ArrayList<>(Arrays.asList(sdId1, sdIdShared)));
        disk2.setStorageIds(new ArrayList<>(Arrays.asList(sdId2, sdIdShared)));

        Set<Guid> result = ImagesHandler.getAllStorageIdsForImageIds(Arrays.asList(disk1, disk2));

        assertEquals("Wrong number of Guids returned", 3, result.size());
        assertTrue("Wrong Guids returned", result.containsAll(Arrays.asList(sdId1, sdId2, sdIdShared)));
    }

    @Test
    public void filterImageDisksFilterDiskSnapshots() {
        disk1.setActive(true);

        List<DiskImage> result = ImagesHandler.filterImageDisks(Arrays.asList(disk1, disk2), false, false, true);

        assertEquals("Wrong number of Guids returned", 1, result.size());
        assertTrue("The result should contain the active image disk", result.contains(disk1));
    }

    @Test
    public void filterDiskBasedOnLunsReturnsOnlyLunDisks() {
        List<LunDisk> returnedLuns = ImagesHandler.filterDiskBasedOnLuns(Arrays.asList(disk1, lunDisk1), true);
        assertEquals("The returned list should contain only lun disks.",
                new ArrayList<>(Collections.singletonList(lunDisk1)),
                new ArrayList<>(returnedLuns));
    }

    @Test
    public void filterDiskBasedOnLunsReturnsAllLuns() {
        lunDisk1.setShareable(true);
        Collection<LunDisk> allLuns = new ArrayList<>(Arrays.asList(lunDisk1, lunDisk2));
        List<LunDisk> returnedLuns = ImagesHandler.filterDiskBasedOnLuns(allLuns, true);
        assertEquals("The returned list should contain both shareable and not shareable lun disks.",
                allLuns, new ArrayList<>(returnedLuns));
    }

    @Test
    public void filterDiskBasedOnLunsReturnsOnlyNotShareableLuns() {
        lunDisk1.setShareable(true);
        List<LunDisk> returnedLuns = ImagesHandler.filterDiskBasedOnLuns(Arrays.asList(lunDisk1, lunDisk2), false);
        assertEquals("The returned list should contain only not shareable lun disks.",
                new ArrayList<>(Collections.singletonList(lunDisk2)),
                new ArrayList<>(returnedLuns));
    }

    @Test
    public void testImagesSubtract() {
        disk1.setId(Guid.newGuid());
        disk2.setId(Guid.newGuid());
        disk3.setId(Guid.newGuid());

        List<DiskImage> list1 = new ArrayList<>(Arrays.asList(disk1, disk2, disk3));
        List<DiskImage> list2 = new ArrayList<>(Arrays.asList(disk2, disk3));

        List<DiskImage> intersection = ImagesHandler.imagesSubtract(list1, list2);

        assertEquals("Intersection should contain only one disk", 1, intersection.size());
        assertTrue("Intersection should contains disk1", intersection.contains(disk1));
    }

    @Test
    public void testImagesIntersection() {
        disk1.setId(Guid.newGuid());
        disk2.setId(Guid.newGuid());
        disk3.setId(Guid.newGuid());

        List<DiskImage> list1 = new ArrayList<>(Arrays.asList(disk1, disk2));
        List<DiskImage> list2 = new ArrayList<>(Arrays.asList(disk1, disk3));

        List<DiskImage> intersection = ImagesHandler.imagesIntersection(list1, list2);

        assertTrue("Intersection should contain only disk1", intersection.size() == 1 && intersection.contains(disk1));
    }
}
