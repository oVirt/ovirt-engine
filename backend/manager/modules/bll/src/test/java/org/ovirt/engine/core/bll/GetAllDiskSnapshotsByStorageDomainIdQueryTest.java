package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetAllDiskSnapshotsByStorageDomainIdQueryTest
        extends AbstractQueryTest<IdQueryParameters,
        GetAllDiskSnapshotsByStorageDomainIdQuery<IdQueryParameters>> {

    /** The {@link org.ovirt.engine.core.dao.DiskImageDAO} mocked for the test */
    private DiskImageDAO diskImageDAO;

    /** The {@link org.ovirt.engine.core.dao.SnapshotDao} mocked for the test */
    private SnapshotDao snapshotDaoMock;

    /** The queried storage domain ID */
    private Guid storageDoaminId;

    /** A snapshot for the test */
    private Snapshot snapshot;

    /** The disks to use for testing */
    private DiskImage disk1;
    private DiskImage disk2;
    private DiskImage disk3;

    private final static String snapshotDescription = "Test Snapshot";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        storageDoaminId = Guid.newGuid();
        setUpDAOMocks();
    }

    private void setUpDAOMocks() {

        // Mock the DAOs
        DbFacade dbFacadeMock = getDbFacadeMockInstance();

        diskImageDAO = mock(DiskImageDAO.class);
        when(dbFacadeMock.getDiskImageDao()).thenReturn(diskImageDAO);
        snapshotDaoMock = mock(SnapshotDao.class);
        when(dbFacadeMock.getSnapshotDao()).thenReturn(snapshotDaoMock);

        Guid snapshotId = Guid.newGuid();
        snapshot = new Snapshot(snapshotId, SnapshotStatus.OK, Guid.newGuid(), null, SnapshotType.REGULAR,
                snapshotDescription, new Date(), "");

        disk1 = new DiskImage();
        disk1.setVmSnapshotId(snapshotId);

        disk2 = new DiskImage();
        disk2.setVmSnapshotId(snapshotId);

        disk3 = new DiskImage();
        disk3.setActive(true);

        List diskImages = new ArrayList(Arrays.asList(disk1, disk2, disk3));
        List snapshots = new ArrayList(Arrays.asList(snapshot));

        when(diskImageDAO.getAllSnapshotsForStorageDomain(storageDoaminId)).thenReturn(diskImages);
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
        assertEquals("There should be two images returned", diskImages.size(), 2);
        assertEquals("DiskImage should contain the VmSnapshotDescription", diskImages.get(0).getVmSnapshotDescription(),
                snapshotDescription);
    }

    @Test
    public void testNoActiveImagesReturned() {
        IdQueryParameters params = getQueryParameters();
        when(params.getId()).thenReturn(storageDoaminId);

        GetAllDiskSnapshotsByStorageDomainIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        List<DiskImage> diskImages = query.getQueryReturnValue().getReturnValue();
        List<DiskImage> activeDiskImages = (List<DiskImage>) CollectionUtils.select(diskImages, new Predicate() {
            @Override
            public boolean evaluate(Object diskImage) {
                return ((DiskImage) diskImage).getActive();
            }
        });

        // Assert the no active images are returned
        assertEquals("Active images shouldn't be returned", activeDiskImages.size(), 0);
    }
}
