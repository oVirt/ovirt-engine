package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

/** A test case for {@link ImagesHandler} */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ImagesHandlerTest {

    @Mock
    private StorageDomainDao storageDomainDaoMock;

    @Mock
    private VmDao vmDao;

    @Mock
    private VdsDao vdsDao;

    @InjectMocks
    private ImagesHandler imagesHandler = new ImagesHandler();

    /** The prefix to use for all tests */
    private static final String prefix = "PREFIX";

    /** The disks to use for testing */
    private DiskImage disk1;
    private DiskImage disk2;
    private DiskImage disk3;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

    @BeforeEach
    public void setUp() {
        disk1 = new DiskImage();
        disk2 = new DiskImage();
        disk3 = new DiskImage();
    }

    @Test
    public void testGetSuggestedDiskAliasNullDisk() {
        assertEquals(prefix + ImagesHandler.DISK + ImagesHandler.DefaultDriveName,
                ImagesHandler.getSuggestedDiskAlias(null, prefix, 1),
                "null disk does not give the default name");
    }

    @Test
    public void testGetSuggestedDiskAliasNullAliasDisk() {
        disk1.setDiskAlias(null);
        assertEquals(prefix + ImagesHandler.DISK + ImagesHandler.DefaultDriveName,
                ImagesHandler.getSuggestedDiskAlias(disk1, prefix, 1),
                "disk with null alias does not give the default name");
    }

    @Test
    public void testGetSuggestedDiskAliasNotNullAliasDisk() {
        disk1.setDiskAlias("someAlias");
        assertEquals(disk1.getDiskAlias(), ImagesHandler.getSuggestedDiskAlias(disk1, prefix, 1),
                "a new alias was generated instead of returning the pre-defined one");
    }

    @Test
    public void testGetDiskAliasWithDefaultNullAlias() {
        assertEquals(ImagesHandler.getDiskAliasWithDefault(disk1, "default"), "default");
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

        assertEquals(3, result.size(), "Wrong number of Guids returned");
        assertTrue(result.containsAll(Arrays.asList(sdId1, sdId2, sdIdShared)), "Wrong Guids returned");
    }

    @Test
    public void testImagesSubtract() {
        disk1.setId(Guid.newGuid());
        disk2.setId(Guid.newGuid());
        disk3.setId(Guid.newGuid());

        List<DiskImage> list1 = new ArrayList<>(Arrays.asList(disk1, disk2, disk3));
        List<DiskImage> list2 = new ArrayList<>(Arrays.asList(disk2, disk3));

        List<DiskImage> intersection = ImagesHandler.imagesSubtract(list1, list2);

        assertEquals(1, intersection.size(), "Intersection should contain only one disk");
        assertTrue(intersection.contains(disk1), "Intersection should contains disk1");
    }

    @Test
    public void testImagesIntersection() {
        disk1.setId(Guid.newGuid());
        disk2.setId(Guid.newGuid());
        disk3.setId(Guid.newGuid());

        List<DiskImage> list1 = new ArrayList<>(Arrays.asList(disk1, disk2));
        List<DiskImage> list2 = new ArrayList<>(Arrays.asList(disk1, disk3));

        List<DiskImage> intersection = ImagesHandler.imagesIntersection(list1, list2);

        assertTrue(intersection.size() == 1 && intersection.contains(disk1), "Intersection should contain only disk1");
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

        assertEquals(Long.valueOf(0L),
                imagesHandler.determineTotalImageInitialSize(disk, VolumeFormat.COW, srcDomainGuid, dstDomainGuid),
                "Total Initial Size should be 0");
    }

    @Test
    @MockedConfig("mockConfiguration")
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
        assertNotNull(resultDisk, "resultDisk should hold a reference to DiskImage object");
        assertEquals(2, result.size(), "wrong number of disks returned");
        assertEquals(3, resultDisk.getSnapshots().size(), "wrong number of snapshots for disk1");
    }

    @Test
    public void testAggregateDiskImagesSnapshotsWithEmptyList() {
        Collection<DiskImage> result =  imagesHandler.aggregateDiskImagesSnapshots(Collections.emptyList());
        assertTrue(result.isEmpty(), "should return an empty list");
    }

    @Test
    public void testGetHostForExecutionRunningVM() {
        Guid storagePoolID = Guid.newGuid();
        Guid imageGroupID = Guid.newGuid();

        VDS vds1 = new VDS();
        vds1.setId(Guid.newGuid());
        vds1.setClusterCompatibilityVersion(Version.v4_4);
        vds1.setStatus(VDSStatus.Up);

        VM runningVM = new VM();
        runningVM.setRunOnVds(vds1.getId());
        runningVM.setStatus(VMStatus.Up);

        disk1.setId(imageGroupID);
        disk1.setActive(true);
        Map<Boolean, List<VM>> pluggedVMs = new HashMap<>();
        pluggedVMs.put(Boolean.TRUE, List.of(runningVM));

        when(vmDao.getForDisk(imageGroupID, true)).thenReturn(pluggedVMs);
        when(vdsDao.get(vds1.getId())).thenReturn(vds1);

        assertEquals(imagesHandler.getHostForMeasurement(storagePoolID, imageGroupID), vds1.getId());
    }
}
