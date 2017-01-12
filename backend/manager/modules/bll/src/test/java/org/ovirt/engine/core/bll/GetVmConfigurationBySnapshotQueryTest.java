package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * A test case for {@link GetVmConfigurationBySnapshotQuery}. This test mocks away all
 * the Daos, and just tests the flow of the query itself.
 */
public class GetVmConfigurationBySnapshotQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetVmConfigurationBySnapshotQuery<IdQueryParameters>> {
    @Mock
    private SnapshotDao snapshotDaoMock;
    private Guid existingSnapshotId = Guid.newGuid();
    private Guid existingVmId = Guid.newGuid();
    private Snapshot existingSnapshot;

    @Spy
    @InjectMocks
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    private static final String EXISTING_VM_NAME = "Dummy configuration";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingSnapshot = createSnapshot(existingSnapshotId);
        existingSnapshot.setVmConfiguration(EXISTING_VM_NAME); // Dummy configuration
        setUpDaoMocks();
    }

    private Snapshot createSnapshot(Guid existingSnapshotId) {
        Snapshot snapshot = new Snapshot();
        snapshot.setId(existingSnapshotId);
        snapshot.setVmId(existingVmId);
        snapshot.setVmConfiguration(EXISTING_VM_NAME);
        return snapshot;
    }

    private void setUpDaoMocks() {
        when(snapshotDaoMock.get(existingSnapshotId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(existingSnapshot);
    }

    @Test
    public void testQuery() throws Exception {
        GetVmConfigurationBySnapshotQuery<IdQueryParameters> query =
                setupQueryBySnapshotId(existingSnapshotId);
        VM vm = new VM();
        doReturn(vm).when(snapshotVmConfigurationHelper).getVmFromConfiguration(
                anyString(), any(Guid.class), any(Guid.class));
        query.execute();
        VdcQueryReturnValue returnValue = query.getQueryReturnValue();
        assertNotNull("Return value from query cannot be null", returnValue);
        VM returnedVm = returnValue.getReturnValue();
        assertEquals(vm, returnedVm);
    }

    @Test
    public void testNonExistingSnapshotQuery() throws Exception {
        GetVmConfigurationBySnapshotQuery<IdQueryParameters> query =
                setupQueryBySnapshotId(Guid.newGuid());
        VdcQueryReturnValue returnValue = query.getQueryReturnValue();
        VM returnedVm = returnValue.getReturnValue();
        assertNull("Return value from non existent query should be null", returnedVm);
    }

    private GetVmConfigurationBySnapshotQuery<IdQueryParameters> setupQueryBySnapshotId(Guid snapshotId) {
        IdQueryParameters queryParams = getQueryParameters();
        when(queryParams.getId()).thenReturn(snapshotId);
        return getQuery();
    }

}
