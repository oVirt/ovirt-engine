package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * A test case for {@link GetAllDisksByVmIdQuery}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetAllDisksByVmIdQueryTest extends AbstractUserQueryTest<GetAllDisksByVmIdParameters, GetAllDisksByVmIdQuery<GetAllDisksByVmIdParameters>> {
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
        vmID = Guid.NewGuid();
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
        return new DiskImage(
                true,
                new Date(),
                new Date(),
                1L,
                "1",
                Guid.NewGuid(),
                "2",
                Guid.NewGuid(),
                1L,
                Guid.NewGuid(),
                ImageStatus.OK,
                new Date(),
                "", VmEntityType.VM, 1, null, null, QuotaEnforcementTypeEnum.DISABLED, false);
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
                diskId,
                ImageStatus.OK,
                new Date(),
                "", VmEntityType.VM, 1, null, null, QuotaEnforcementTypeEnum.DISABLED, false);
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
