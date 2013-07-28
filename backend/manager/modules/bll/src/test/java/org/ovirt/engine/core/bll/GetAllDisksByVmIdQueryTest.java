package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VmDeviceDAO;

/**
 * A test case for {@link GetAllDisksByVmIdQuery}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetAllDisksByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetAllDisksByVmIdQuery<IdQueryParameters>> {
    private static final int NUM_DISKS_OF_EACH_KIND = 3;

    /** The {@link DiskDAO} mocked for the test */
    private DiskDao diskDAOMock;

    /** The {@link VmDeviceDAO} mocked for the test */
    private VmDeviceDAO vmDeviceDAOMock;

    /** The ID of the VM the disks belong to */
    private Guid vmID;

    /** A plugged disk for the test */
    private DiskImage pluggedDisk;

    /** An unplugged disk for the test */
    private DiskImage unpluggedDisk;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmID = Guid.newGuid();
        pluggedDisk = createDiskImage();
        unpluggedDisk = createDiskImage();
        setUpDAOMocks();
    }

    private void setUpDAOMocks() {
        // Mock some devices
        VmDevice pluggedDevice = createVMDevice(vmID, pluggedDisk);

        // Mock the DAOs
        DbFacade dbFacadeMock = getDbFacadeMockInstance();

        // Disk Image DAO
        List<Disk> returnArray = new ArrayList<Disk>();
        returnArray.add(pluggedDisk);
        returnArray.add(unpluggedDisk);
        diskDAOMock = mock(DiskDao.class);
        when(dbFacadeMock.getDiskDao()).thenReturn(diskDAOMock);
        when(diskDAOMock.getAllForVm(vmID, getUser().getUserId(), getQueryParameters().isFiltered())).thenReturn(returnArray);

        // VM Device DAO
        vmDeviceDAOMock = mock(VmDeviceDAO.class);
        when(dbFacadeMock.getVmDeviceDao()).thenReturn(vmDeviceDAOMock);
        when(vmDeviceDAOMock.getVmDeviceByVmIdTypeAndDevice(vmID,
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                getUser().getUserId(),
                getQueryParameters().isFiltered())).
                thenReturn(Collections.singletonList(pluggedDevice));

        // Snapshots
        doReturn(new ArrayList<DiskImage>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND,
                createDiskSnapshot(pluggedDisk.getId())))).when(getQuery()).getAllImageSnapshots(pluggedDisk);
        doReturn(Collections.nCopies(NUM_DISKS_OF_EACH_KIND, createDiskSnapshot(unpluggedDisk.getId()))).when(getQuery())
                .getAllImageSnapshots(unpluggedDisk);
    }

    private static VmDevice createVMDevice(Guid vmID, DiskImage disk) {
        return new VmDevice(new VmDeviceId(disk.getImageId(), vmID),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                1,
                null,
                true,
                true,
                true,
                "",
                null);
    }

    private DiskImage createDiskImage() {
        DiskImage di = new DiskImage();
        di.setActive(true);
        di.setId(Guid.newGuid());
        di.setImageId(Guid.newGuid());
        di.setParentId(Guid.newGuid());
        di.setImageStatus(ImageStatus.OK);
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
        List<DiskImage> disks = (List<DiskImage>) getQuery().getQueryReturnValue().getReturnValue();

        // Assert the correct disks are returned
        assertTrue("plugged disk should be in the return value", disks.contains(pluggedDisk));
        assertTrue("unplugged disk should be in the return value", disks.contains(unpluggedDisk));

        // Assert the disks have the correct snapshots
        assertCorrectSnapshots(pluggedDisk);
        assertCorrectSnapshots(unpluggedDisk);
    }

    /**
     * Assert the given disk contains {@link #NUM_DISKS_OF_EACH_KIND} copies of itself as snapshot (as should have been returned by the DAO)
     * @param disk The disk to check
     */
    private static void assertCorrectSnapshots(DiskImage disk) {
        for (int i = 0; i < NUM_DISKS_OF_EACH_KIND; ++i) {
            assertEquals("Wrong snapshot " + i + " for disk ", disk.getId(), disk.getSnapshots().get(i).getId());
        }
    }
}
