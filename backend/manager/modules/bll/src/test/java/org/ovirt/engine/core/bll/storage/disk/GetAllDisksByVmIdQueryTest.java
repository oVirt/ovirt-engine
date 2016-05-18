package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

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
    private DiskVmElementDao diskVmElementDao;

    @Before
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
        // Mock some devices
        VmDevice pluggedDevice = createVMDevice(vmID, pluggedDisk);
        VmDevice unpluggedDevice = createVMDevice(vmID, unpluggedDisk);
        VmDevice pluggedSnapshotDevice = createVMDevice(vmID, pluggedDiskSnapshot);
        VmDevice unpluggedSnapshotDevice = createVMDevice(vmID, unpluggedDiskSnapshot);

        // Mock the Daos
        DbFacade dbFacadeMock = getDbFacadeMockInstance();

        // Disk Image Dao
        List<Disk> returnArray = new ArrayList<>();
        returnArray.add(pluggedDisk);
        returnArray.add(unpluggedDisk);
        returnArray.add(pluggedDiskSnapshot);
        returnArray.add(unpluggedDiskSnapshot);

        DiskDao diskDaoMock = mock(DiskDao.class);
        when(dbFacadeMock.getDiskDao()).thenReturn(diskDaoMock);
        when(diskDaoMock.getAllForVm(vmID, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(returnArray);

        when(dbFacadeMock.getDiskVmElementDao()).thenReturn(diskVmElementDao);
        when(diskVmElementDao.get(any(VmDeviceId.class))).thenReturn(new DiskVmElement(new VmDeviceId()));

        // VM Device Dao
        VmDeviceDao vmDeviceDaoMock = mock(VmDeviceDao.class);
        when(dbFacadeMock.getVmDeviceDao()).thenReturn(vmDeviceDaoMock);
        when(vmDeviceDaoMock.getVmDeviceByVmIdTypeAndDevice(vmID,
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                getUser().getId(),
                getQueryParameters().isFiltered())).
                thenReturn(Arrays.asList(pluggedDevice, unpluggedDevice, pluggedSnapshotDevice, unpluggedSnapshotDevice));

        // Snapshots
        doReturn(new ArrayList<>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND,
                createDiskSnapshot(pluggedDisk.getId())))).when(getQuery()).getAllImageSnapshots(pluggedDisk);
        doReturn(Collections.nCopies(NUM_DISKS_OF_EACH_KIND, createDiskSnapshot(unpluggedDisk.getId()))).when(getQuery())
                .getAllImageSnapshots(unpluggedDisk);
        doReturn(new ArrayList<>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND,
                createDiskSnapshot(pluggedDiskSnapshot.getId())))).when(getQuery()).getAllImageSnapshots(pluggedDiskSnapshot);
        doReturn(Collections.nCopies(NUM_DISKS_OF_EACH_KIND, createDiskSnapshot(unpluggedDiskSnapshot.getId()))).when(getQuery())
                .getAllImageSnapshots(unpluggedDiskSnapshot);
    }

    private VmDevice createVMDevice(Guid vmID, DiskImage disk) {
        return new VmDevice(new VmDeviceId(disk.getId(), vmID),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                1,
                null,
                true,
                true,
                true,
                "",
                null,
                disk.getVmSnapshotId(),
                null);
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
        assertTrue("plugged disk should be in the return value", disks.contains(pluggedDisk));
        assertTrue("unplugged disk should be in the return value", disks.contains(unpluggedDisk));
        assertTrue("plugged disk snapshots should be in the return value", disks.contains(pluggedDiskSnapshot));
        assertTrue("unplugged disk snapshots should be in the return value", disks.contains(unpluggedDiskSnapshot));

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
            assertEquals("Wrong snapshot " + i + " for disk ", disk.getId(), disk.getSnapshots().get(i).getId());
        }
    }
}
