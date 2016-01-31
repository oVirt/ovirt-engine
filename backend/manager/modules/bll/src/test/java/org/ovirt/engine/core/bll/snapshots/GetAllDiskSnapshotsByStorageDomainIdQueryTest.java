package org.ovirt.engine.core.bll.snapshots;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetAllDiskSnapshotsByStorageDomainIdQueryTest
        extends AbstractQueryTest<IdQueryParameters,
        GetAllDiskSnapshotsByStorageDomainIdQuery<IdQueryParameters>> {

    /** The {@link org.ovirt.engine.core.dao.DiskImageDao} mocked for the test */
    @Mock
    private DiskImageDao diskImageDao;

    /** The {@link org.ovirt.engine.core.dao.SnapshotDao} mocked for the test */
    @Mock
    private SnapshotDao snapshotDaoMock;

    /** The queried storage domain ID */
    private Guid storageDoaminId;

    private static final String snapshotDescription = "Test Snapshot";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        storageDoaminId = Guid.newGuid();
        setUpDaoMocks();
    }

    private void setUpDaoMocks() {

        // Mock the Daos
        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        when(dbFacadeMock.getDiskImageDao()).thenReturn(diskImageDao);
        when(dbFacadeMock.getSnapshotDao()).thenReturn(snapshotDaoMock);

        Guid snapshotId = Guid.newGuid();
        Snapshot snapshot = new Snapshot(snapshotId, SnapshotStatus.OK, Guid.newGuid(), null, SnapshotType.REGULAR,
                snapshotDescription, new Date(), "");

        DiskImage disk1 = new DiskImage();
        disk1.setVmSnapshotId(snapshotId);

        DiskImage disk2 = new DiskImage();
        disk2.setVmSnapshotId(snapshotId);

        DiskImage disk3 = new DiskImage();
        disk3.setActive(true);

        List<DiskImage> diskImages = Arrays.asList(disk1, disk2, disk3);
        List<Snapshot> snapshots = Collections.singletonList(snapshot);

        when(diskImageDao.getAllSnapshotsForStorageDomain(storageDoaminId)).thenReturn(diskImages);
        when(snapshotDaoMock.getAllByStorageDomain(storageDoaminId)).thenReturn(snapshots);
    }

    @Test
    public void testExecuteQueryCommand() {
        IdQueryParameters params = getQueryParameters();
        when(params.getId()).thenReturn(storageDoaminId);

        GetAllDiskSnapshotsByStorageDomainIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        List<DiskImage> diskImages = query.getQueryReturnValue().getReturnValue();

        // Assert the correct disks are returned
        assertEquals("There should be two images returned", 2, diskImages.size());
        assertEquals("DiskImage should contain the VmSnapshotDescription", snapshotDescription,
                diskImages.get(0).getVmSnapshotDescription());
    }

    @Test
    public void testNoActiveImagesReturned() {
        IdQueryParameters params = getQueryParameters();
        when(params.getId()).thenReturn(storageDoaminId);

        GetAllDiskSnapshotsByStorageDomainIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        List<DiskImage> diskImages = query.getQueryReturnValue().getReturnValue();

        // Assert the no active images are returned
        assertTrue("Active images shouldn't be returned", diskImages.stream().noneMatch(DiskImage::getActive));
    }
}
