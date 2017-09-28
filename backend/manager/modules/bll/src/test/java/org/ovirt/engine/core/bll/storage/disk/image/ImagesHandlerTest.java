package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

/** A test case for {@link ImagesHandler} */
@RunWith(MockitoJUnitRunner.class)
public class ImagesHandlerTest {

    @Mock
    private StorageDomainDao storageDomainDaoMock;

    @InjectMocks
    private ImagesHandler imagesHandler = new ImagesHandler();

    /** The prefix to use for all tests */
    private static final String prefix = "PREFIX";

    /** The disks to use for testing */
    private DiskImage disk1;
    private DiskImage disk2;
    private DiskImage disk3;

    @Before
    public void setUp() {
        disk1 = new DiskImage();
        disk2 = new DiskImage();
        disk3 = new DiskImage();
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

    @Test
    public void testDetermineTotalImageInitialSizeFromNfsToCow() {
        DiskImage disk = new DiskImage();
        disk.setSize(5000);
        disk.setActualSizeInBytes(0);

        Guid srcDomainGuid = Guid.newGuid();
        StorageDomain srcDomain = new StorageDomain();
        srcDomain.setId(srcDomainGuid);
        srcDomain.setStorageType(StorageType.NFS);
        when(storageDomainDaoMock.get(srcDomainGuid)).thenReturn(srcDomain);

        Guid dstDomainGuid = Guid.newGuid();
        StorageDomain destDomain = new StorageDomain();
        destDomain.setId(dstDomainGuid);
        destDomain.setStorageType(StorageType.ISCSI);
        when(storageDomainDaoMock.get(dstDomainGuid)).thenReturn(destDomain);

        assertEquals("Total Initial Size should be 0",
                Long.valueOf(0L),
                imagesHandler.determineTotalImageInitialSize(disk, VolumeFormat.COW, srcDomainGuid, dstDomainGuid));
    }

    @Test
    public void testAggregateDiskImagesSnapshots() {
        disk1.setId(Guid.newGuid());
        disk1.setActive(true);
        disk1.setStorageIds(new ArrayList<>());

        disk2.setId(disk1.getId());
        disk2.setActive(false);

        disk3.setId(disk1.getId());
        disk3.setActive(false);

        DiskImage diskWithoutSnapshots = new DiskImage();
        diskWithoutSnapshots.setId(Guid.newGuid());
        diskWithoutSnapshots.setActive(true);
        diskWithoutSnapshots.setStorageIds(new ArrayList<>());

        List<DiskImage> result =
                new ArrayList<>(imagesHandler.aggregateDiskImagesSnapshots(Arrays.asList(
                        disk1,
                        disk2,
                        disk3,
                        diskWithoutSnapshots)));
        DiskImage resultDisk =
                result.stream().filter(diskImage -> diskImage.getId() == disk1.getId()).findFirst().orElse(null);
        assertNotNull("resultDisk should hold a reference to DiskImage object", resultDisk);
        assertEquals("wrong number of disks returned", 2, result.size());
        assertEquals("wrong number of snapshots for disk1", 3, resultDisk.getSnapshots().size());
    }

    @Test
    public void testAggregateDiskImagesSnapshotsWithEmptyList() {
        Collection<DiskImage> result =  imagesHandler.aggregateDiskImagesSnapshots(Collections.emptyList());
        assertTrue("should return an empty list", result.isEmpty());
    }
}
