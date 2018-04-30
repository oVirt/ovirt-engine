package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class IsBalloonEnabledQueryTest extends AbstractQueryTest<IdQueryParameters, IsBalloonEnabledQuery<IdQueryParameters>> {

    /** The {@link VmDeviceDao} mocked for the test */
    @Mock
    private VmDeviceDao vmDeviceDaoMock;

    /** The {@link SnapshotDao} mocked for the test */
    @Mock
    private SnapshotDao snapshotDaoMock;

    /** The ID of the VM the disks belong to */
    private Guid vmID;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmID = Guid.newGuid();
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
