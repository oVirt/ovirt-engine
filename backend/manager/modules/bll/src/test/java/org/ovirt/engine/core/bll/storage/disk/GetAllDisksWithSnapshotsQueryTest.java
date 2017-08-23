package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * A test case for {@link GetAllDisksWithSnapshotsQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetAllDisksWithSnapshotsQueryTest
        extends AbstractUserQueryTest<QueryParametersBase, GetAllDisksWithSnapshotsQuery<QueryParametersBase>> {
    private static final int NUM_OF_SNAPSHOTS_TO_CREATE = 3;

    /**
     * Active disk with snapshots for the test
     */
    private DiskImage diskWithSnapshots;

    /**
     * List of all the snapshots for {@link GetAllDisksWithSnapshotsQueryTest#diskWithSnapshots}
     */
    private List<DiskImage> snapshotsList;

    /**
     * Active disk without snapshots for the test
     */
    private DiskImage diskWithoutSnapshots;

    /**
     * OVF image for the test
     */
    private DiskImage ovfImage;

    /**
     * Cinder disk for the test
     */
    private CinderDisk cinderDisk;

    /**
     * LUN disk for the test
     */
    private LunDisk lunDisk;

    @Mock
    private ImagesHandler imagesHandler;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        diskWithSnapshots = createDiskImage(null);
        snapshotsList = createSnapshotsForDiskImage(diskWithSnapshots.getId());
        diskWithoutSnapshots = createDiskImage(null);
        ovfImage = createDiskImage(null);
        ovfImage.setContentType(DiskContentType.OVF_STORE);
        cinderDisk = createCinderDisk();
        lunDisk = createLunDisk();
        setUpImagesHandlerMocks();
    }

    private void setUpImagesHandlerMocks() {
        List<DiskImage> diskImagesTestParam = new ArrayList<>(snapshotsList);
        Collections.addAll(diskImagesTestParam, diskWithSnapshots, diskWithoutSnapshots, ovfImage, cinderDisk);

        when(imagesHandler.aggregateDiskImagesSnapshots(diskImagesTestParam)).thenReturn(manualAggregateSnapshots());
    }

    private List<DiskImage> manualAggregateSnapshots() {
        diskWithSnapshots.getSnapshots().addAll(snapshotsList);
        return Arrays.asList(diskWithSnapshots, diskWithoutSnapshots, ovfImage, cinderDisk);
    }

    private DiskImage createDiskImage(Guid diskId) {
        DiskImage di = new DiskImage();
        if (diskId != null) {
            di.setActive(false);
            di.setId(diskId);
        } else {
            di.setId(Guid.newGuid());
            di.setActive(true);
        }
        return di;
    }

    private List<DiskImage> createSnapshotsForDiskImage(Guid diskId) {
        return IntStream.range(0, NUM_OF_SNAPSHOTS_TO_CREATE).mapToObj(i -> createDiskImage(diskId)).collect(
                Collectors.toList());
    }

    private CinderDisk createCinderDisk() {
        CinderDisk cd = new CinderDisk();
        cd.setId(Guid.newGuid());
        cd.setActive(true);
        return cd;
    }

    private LunDisk createLunDisk() {
        LunDisk lun = new LunDisk();
        lun.setId(Guid.newGuid());
        return lun;
    }

    /**
     * Test GetAllDisksWithSnapshotsQuery#aggregateDisksSnapshots method logic.
     * Given Images from various types, some of them are snapshots,
     * Verify that the snapshot disks aggregated under their active image and all the active disks return in a list.
     */
    @Test
    public void testAggregateDisksSnapshots() {
        List<Disk> disks = new ArrayList<>(snapshotsList);
        Collections.addAll(disks, diskWithSnapshots, diskWithoutSnapshots, ovfImage, cinderDisk, lunDisk);
        Collection<Disk> result = getQuery().aggregateDisksSnapshots(disks);

        // Assert each return disk is active
        assertAllDisksAreActive(result);
        // Verify the number of returned disks
        assertEquals("wrong number of disks returned", 5, result.size());

    }

    private void assertAllDisksAreActive(Collection<Disk> disks) {
        for (Disk disk : disks) {
            if (disk instanceof LunDisk) {
                continue;
            }
            DiskImage diskImage = (DiskImage) disk;
            assertTrue("disk should be active", diskImage.getActive());
        }
    }
}
