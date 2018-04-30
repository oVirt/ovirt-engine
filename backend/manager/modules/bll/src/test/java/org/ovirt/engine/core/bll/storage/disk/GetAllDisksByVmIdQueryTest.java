package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;

/**
 * A test case for {@link GetAllDisksByVmIdQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetAllDisksByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetAllDisksByVmIdQuery<IdQueryParameters>> {
    private static final int NUM_DISKS_OF_EACH_KIND = 3;

    /** The ID of the VM the disks belong to */
    private Guid vmID;

    /** A plugged disk for the test */
    private DiskImage pluggedDisk;

    /** An unplugged disk for the test */
    private DiskImage unpluggedDisk;

    /** A plugged disk snapshot for the test */
    private DiskImage pluggedDiskSnapshot;

    /** An unplugged disk snapshot for the test */
    private DiskImage unpluggedDiskSnapshot;

    @Mock
    private DiskDao diskDaoMock;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmID = Guid.newGuid();
        Guid snapshotId = Guid.newGuid();
        pluggedDisk = createDiskImage(true);
        unpluggedDisk = createDiskImage(true);
        pluggedDiskSnapshot = createDiskImage(false);
        pluggedDiskSnapshot.setVmSnapshotId(snapshotId);
        unpluggedDiskSnapshot = createDiskImage(false);
        unpluggedDiskSnapshot.setVmSnapshotId(snapshotId);
        setUpDaoMocks();
    }

    private void setUpDaoMocks() {
        // Disk Image Dao
        List<Disk> returnArray = new ArrayList<>();
        returnArray.add(pluggedDisk);
        returnArray.add(unpluggedDisk);
        returnArray.add(pluggedDiskSnapshot);
        returnArray.add(unpluggedDiskSnapshot);

        when(diskDaoMock.getAllForVm(vmID, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(returnArray);

        when(diskVmElementDao.get(any())).thenReturn(new DiskVmElement(new VmDeviceId()));

        // Snapshots
        doReturn(new ArrayList<>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND,
                createDiskSnapshot(pluggedDisk.getId())))).when(diskImageDao).getAllSnapshotsForLeaf(pluggedDisk.getImageId());
        doReturn(Collections.nCopies(NUM_DISKS_OF_EACH_KIND, createDiskSnapshot(unpluggedDisk.getId()))).when(diskImageDao)
                .getAllSnapshotsForLeaf(unpluggedDisk.getImageId());
        doReturn(new ArrayList<>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND,
                createDiskSnapshot(pluggedDiskSnapshot.getId())))).when(diskImageDao).getAllSnapshotsForLeaf(pluggedDiskSnapshot.getImageId());
        doReturn(Collections.nCopies(NUM_DISKS_OF_EACH_KIND, createDiskSnapshot(unpluggedDiskSnapshot.getId()))).when(diskImageDao)
                .getAllSnapshotsForLeaf(unpluggedDiskSnapshot.getImageId());
    }

    private DiskImage createDiskImage(boolean active) {
        DiskImage di = new DiskImage();
        di.setId(Guid.newGuid());
        di.setImageId(Guid.newGuid());
        di.setParentId(Guid.newGuid());
        di.setImageStatus(ImageStatus.OK);
        di.setActive(active);
        return di;
    }

    private DiskImage createDiskSnapshot(Guid diskId) {
        DiskImage di = new DiskImage();
        di.setActive(false);
        di.setId(diskId);
        di.setImageId(Guid.newGuid());
        di.setParentId(Guid.newGuid());
        di.setImageStatus(ImageStatus.OK);
        return di;
    }

    @Test
    public void testExecuteQueryCommand() {
        params = getQueryParameters();
        when(params.getId()).thenReturn(vmID);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<DiskImage> disks = getQuery().getQueryReturnValue().getReturnValue();

        // Assert the correct disks are returned
        assertTrue(disks.contains(pluggedDisk), "plugged disk should be in the return value");
        assertTrue(disks.contains(unpluggedDisk), "unplugged disk should be in the return value");
        assertTrue(disks.contains(pluggedDiskSnapshot), "plugged disk snapshots should be in the return value");
        assertTrue(disks.contains(unpluggedDiskSnapshot), "unplugged disk snapshots should be in the return value");

        // Assert the disks have the correct snapshots
        assertCorrectSnapshots(pluggedDisk);
        assertCorrectSnapshots(unpluggedDisk);
    }

    /**
     * Assert the given disk contains {@link #NUM_DISKS_OF_EACH_KIND} copies of itself as snapshot (as should have been returned by the Dao)
     * @param disk The disk to check
     */
    private static void assertCorrectSnapshots(DiskImage disk) {
        for (int i = 0; i < NUM_DISKS_OF_EACH_KIND; ++i) {
            assertEquals(disk.getId(), disk.getSnapshots().get(i).getId(), "Wrong snapshot " + i + " for disk ");
        }
    }
}
