package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class IsBalloonEnabledQueryTest extends AbstractQueryTest<IdQueryParameters, IsBalloonEnabledQuery<IdQueryParameters>> {

    /** The {@link VmDeviceDao} mocked for the test */
    private VmDeviceDao vmDeviceDaoMock;

    /** The {@link SnapshotDao} mocked for the test */
    private SnapshotDao snapshotDaoMock;

    /** The ID of the VM the disks belong to */
    private Guid vmID;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmID = Guid.newGuid();
        setUpDaoMocks();
    }

    private void setUpDaoMocks() {
        // Mock the Daos
        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        // VM Device Dao
        vmDeviceDaoMock = mock(VmDeviceDao.class);
        when(dbFacadeMock.getVmDeviceDao()).thenReturn(vmDeviceDaoMock);
        // Snapshot Dao
        snapshotDaoMock = mock(SnapshotDao.class);
        when(dbFacadeMock.getSnapshotDao()).thenReturn(snapshotDaoMock);
    }

    @Test
    public void testExecuteQueryCommand() {
        params = getQueryParameters();
        when(params.getId()).thenReturn(vmID);

        IsBalloonEnabledQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        assertTrue(!((Boolean) query.getQueryReturnValue().getReturnValue()).booleanValue());
    }
}
