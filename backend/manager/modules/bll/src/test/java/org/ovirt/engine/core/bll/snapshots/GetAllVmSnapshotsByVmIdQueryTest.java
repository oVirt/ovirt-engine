package org.ovirt.engine.core.bll.snapshots;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * A test case for {@link GetAllVmSnapshotsByVmIdQuery}. This test mocks away all the Daos, and just tests the flow of
 * the query itself.
 */
public class GetAllVmSnapshotsByVmIdQueryTest
        extends AbstractUserQueryTest<IdQueryParameters,
        GetAllVmSnapshotsByVmIdQuery<IdQueryParameters>> {

    /** The ID of the VM the disks belong to */
    private Guid vmId;

    /** A snapshot for the test */
    private Snapshot snapshot;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmId = Guid.newGuid();
        snapshot =
                new Snapshot(Guid.newGuid(), SnapshotStatus.OK, vmId, null, SnapshotType.REGULAR, "", new Date(), "");
        setUpDaoMocks();
    }

    private void setUpDaoMocks() {

        // Mock the Daos
        DbFacade dbFacadeMock = getDbFacadeMockInstance();

        // Disk Image Dao
        SnapshotDao snapshotDaoMock = mock(SnapshotDao.class);
        when(dbFacadeMock.getSnapshotDao()).thenReturn(snapshotDaoMock);
        when(snapshotDaoMock.getAll(vmId, getUser().getId(), getQueryParameters().isFiltered()))
                .thenReturn(Collections.singletonList(snapshot));
    }

    @Test
    public void testExecuteQueryCommand() {
        IdQueryParameters params = getQueryParameters();
        when(params.getId()).thenReturn(vmId);

        GetAllVmSnapshotsByVmIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<Snapshot> snapshots = query.getQueryReturnValue().getReturnValue();

        // Assert the correct disks are returned
        assertTrue("snapshot should be in the return value", snapshots.contains(snapshot));
        assertEquals("there should be exactly one snapshot returned", 1, snapshots.size());
    }
}
