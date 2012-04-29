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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.ovirt.engine.core.utils.RandomUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * A test case for {@GetAllDisksByVmIdQuery}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
@RunWith(PowerMockRunner.class)
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
        vmID = Guid.NewGuid();
        pluggedDisk = createDiskImage(true);
        unpluggedDisk = createDiskImage(true);
        inactiveDisk = createDiskImage(false);
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
        when(ImagesHandler.getAllImageSnapshots(pluggedDisk.getImageId(), pluggedDisk.getit_guid())).thenReturn
                (new ArrayList<DiskImage>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND,
                        createDiskSnapshot(pluggedDisk.getId()))));
        when(ImagesHandler.getAllImageSnapshots(unpluggedDisk.getImageId(), unpluggedDisk.getit_guid())).thenReturn
                (new ArrayList<DiskImage>(Collections.nCopies(NUM_DISKS_OF_EACH_KIND,
                        createDiskSnapshot(unpluggedDisk.getId()))));
    }

    private static VmDevice createVMDevice(Guid vmID, DiskImage disk) {
        return new VmDevice(new VmDeviceId(disk.getImageId(), vmID),
                VmDeviceType.DISK.getName(),
                VmDeviceType.DISK.getName(),
                "",
                1,
                null,
                true,
                true,
                true);
    }

    private DiskImage createDiskImage(boolean isActive) {
        return new DiskImage(
                isActive,
                new Date(),
                new Date(),
                1L,
                "1",
                Guid.NewGuid(),
                "2",
                Guid.NewGuid(),
                1L,
                vmID,
                Guid.NewGuid(),
                ImageStatus.OK,
                new Date(),
                "", VmEntityType.VM, null, null);
    }

    private DiskImage createDiskSnapshot(Guid diskId) {
        return new DiskImage(
                RandomUtils.instance().nextBoolean(),
                new Date(),
                new Date(),
                1L,
                "1",
                Guid.NewGuid(),
                "2",
                Guid.NewGuid(),
                1L,
                vmID,
                diskId,
                ImageStatus.OK,
                new Date(),
                "", VmEntityType.VM, null, null);
    }

    @Test
    public void testExecuteQueryCommand() {
        params = getQueryParameters();
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
            assertEquals("Wrong snapshot " + i + " for disk ", disk.getId(), disk.getSnapshots().get(i).getId());
        }
    }
}
