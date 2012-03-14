package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * A test case for {@GetAllDisksByVmIdQuery}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
@PrepareForTest(ImagesHandler.class)
public class GetAllDisksByVmIdQueryTest extends AbstractUserQueryTest<GetAllDisksByVmIdParameters, GetAllDisksByVmIdQuery<GetAllDisksByVmIdParameters>> {
    private static final int NUM_DISKS_OF_EACH_KIND = 3;

    /** The {@link DiskImageDAO} mocked for the test */
    private DiskImageDAO diskImageDAOMock;

    /** The {@link VmDeviceDAO} mocked for the test */
    private VmDeviceDAO vmDeviceDAOMock;

    /** The ID of the VM the disks belong to */
    private Guid vmID;

    /** A plugged disk for the test */
    private DiskImage pluggedDisk;

    /** An unplugged disk for the test */
    private DiskImage unpluggedDisk;

    /** An inactive disk for the test */
    private DiskImage inactiveDisk;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmID = new Guid(UUID.randomUUID());
        pluggedDisk = createDiskImage(vmID, true);
        unpluggedDisk = createDiskImage(vmID, true);
        inactiveDisk = createDiskImage(vmID, false);
        setUpDAOMocks();
    }

    private void setUpDAOMocks() {
        // Mock some devices
        VmDevice pluggedDevice = createVMDevice(vmID, pluggedDisk);

        // Mock the DAOs
        DbFacade dbFacadeMock = getDbFacadeMockInstance();

        // Disk Image DAO
        diskImageDAOMock = mock(DiskImageDAO.class);
        when(dbFacadeMock.getDiskImageDAO()).thenReturn(diskImageDAOMock);
        when(diskImageDAOMock.getAllForVm(vmID, getUser().getUserId(), getQueryParameters().isFiltered())).thenReturn
                (Arrays.asList(pluggedDisk,
                        unpluggedDisk,
                        inactiveDisk));

        // VM Device DAO
        vmDeviceDAOMock = mock(VmDeviceDAO.class);
        when(dbFacadeMock.getVmDeviceDAO()).thenReturn(vmDeviceDAOMock);
        when(vmDeviceDAOMock.getVmDeviceByVmIdTypeAndDevice(vmID,
                VmDeviceType.DISK.getName(),
                VmDeviceType.DISK.getName(),
                getUser().getUserId(),
                getQueryParameters().isFiltered())).
                thenReturn(Collections.singletonList(pluggedDevice));

        // Image handler
        mockStatic(ImagesHandler.class);
        when(ImagesHandler.getAllImageSnapshots(pluggedDisk.getId(), pluggedDisk.getit_guid())).thenReturn
                (new ArrayList<DiskImage>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND, pluggedDisk)));
        when(ImagesHandler.getAllImageSnapshots(unpluggedDisk.getId(), unpluggedDisk.getit_guid())).thenReturn
                (new ArrayList<DiskImage>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND, unpluggedDisk)));
    }

    private static VmDevice createVMDevice(Guid vmID, DiskImage disk) {
        return new VmDevice(new VmDeviceId(disk.getId(), vmID),
                VmDeviceType.DISK.getName(),
                VmDeviceType.DISK.getName(),
                "",
                1,
                "",
                true,
                true,
                true);
    }

    private static DiskImage createDiskImage(Guid vmID, boolean isActive) {
        return new DiskImage(
                isActive,
                new Date(),
                new Date(),
                1L,
                "1",
                new Guid(UUID.randomUUID()),
                "2",
                new Guid(UUID.randomUUID()),
                1L,
                vmID,
                new Guid(UUID.randomUUID()),
                ImageStatus.OK,
                new Date(),
                "",VmEntityType.VM, null, null);
    }

    @Test
    public void testExecuteQueryCommand() {
        GetAllDisksByVmIdParameters params = getQueryParameters();
        when(params.getVmId()).thenReturn(vmID);

        GetAllDisksByVmIdQuery<GetAllDisksByVmIdParameters> query = getQuery();
        query.executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<DiskImage> disks = (List<DiskImage>) query.getQueryReturnValue().getReturnValue();

        // Assert the correct disks are returned
        assertTrue("plugged disk should be in the return value", disks.contains(pluggedDisk));
        assertTrue("unplugged disk should be in the return value", disks.contains(unpluggedDisk));
        assertFalse("inactive disk should not be in the return value", disks.contains(inactiveDisk));

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
            assertEquals("Wrong snapshot " + i + " for disk ", disk, disk.getSnapshots().get(i));
        }
    }
}
