package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
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
    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    private SnapshotDao snapshotDaoMock;
    private Guid existingSnapshotId = Guid.newGuid();
    private Guid existingVmId = Guid.newGuid();
    private Snapshot existingSnapshot;
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    private static final String EXISTING_VM_NAME = "Dummy configuration";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        injectorRule.bind(MacPoolPerDc.class, mock(MacPoolPerDc.class));

        existingSnapshot = createSnapshot(existingSnapshotId);
        existingSnapshot.setVmConfiguration(EXISTING_VM_NAME); // Dummy configuration
        snapshotVmConfigurationHelper = spy(new SnapshotVmConfigurationHelper());
        when(getQuery().getSnapshotVmConfigurationHelper()).thenReturn(snapshotVmConfigurationHelper);
        SnapshotsManager snapshotsManager = mock(SnapshotsManager.class);
        when(snapshotVmConfigurationHelper.getSnapshotManager()).thenReturn(snapshotsManager);
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
        snapshotDaoMock = mock(SnapshotDao.class);
        doReturn(snapshotDaoMock).when(getQuery()).getSnapshotDao();
        when(snapshotDaoMock.get(existingSnapshotId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(existingSnapshot);
    }

    @Test
    public void testQuery() throws Exception {
        GetVmConfigurationBySnapshotQuery<IdQueryParameters> query =
                setupQueryBySnapshotId(existingSnapshotId);
        VM vm = new VM();
        doReturn(vm).when(snapshotVmConfigurationHelper).getVmFromConfiguration(
                any(String.class), any(Guid.class), any(Guid.class));
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
        when(snapshotDaoMock.get(any(Guid.class))).thenReturn(null);
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
