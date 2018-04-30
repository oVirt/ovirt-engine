package org.ovirt.engine.core.bll.snapshots;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * A test case for {@link GetAllVmSnapshotsByVmIdQuery}. This test mocks away all the Daos, and just tests the flow of
 * the query itself.
 */
public class GetAllVmSnapshotsByVmIdQueryTest
        extends AbstractUserQueryTest<IdQueryParameters,
        GetAllVmSnapshotsByVmIdQuery<IdQueryParameters>> {

    @Mock
    private SnapshotDao snapshotDaoMock;

    /** The ID of the VM the disks belong to */
    private Guid vmId;

    /** A snapshot for the test */
    private Snapshot snapshot;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmId = Guid.newGuid();
        snapshot =
                new Snapshot(Guid.newGuid(), SnapshotStatus.OK, vmId, null, SnapshotType.REGULAR, "", new Date(), "");
        setUpDaoMocks();
    }

    private void setUpDaoMocks() {
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
        assertTrue(snapshots.contains(snapshot), "snapshot should be in the return value");
        assertEquals(1, snapshots.size(), "there should be exactly one snapshot returned");
    }
}
