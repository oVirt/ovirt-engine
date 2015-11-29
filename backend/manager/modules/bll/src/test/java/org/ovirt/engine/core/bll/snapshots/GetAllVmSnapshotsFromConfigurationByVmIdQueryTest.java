package org.ovirt.engine.core.bll.snapshots;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * A test case for {@link GetAllVmSnapshotsFromConfigurationByVmIdQuery}.
 */
public class GetAllVmSnapshotsFromConfigurationByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters,
        GetAllVmSnapshotsFromConfigurationByVmIdQuery<IdQueryParameters>> {

    /** The {@link org.ovirt.engine.core.dao.SnapshotDao} mocked for the test */
    private SnapshotDao snapshotDaoMock;

    /** The ID of the VM the disks belong to */
    private Guid vmId;

    /** A snapshot for the test */
    private Snapshot snapshot;

    /** The disks to use for testing */
    private DiskImage disk1;
    private DiskImage disk2;

    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmId = Guid.newGuid();
        snapshot = new Snapshot(
                Guid.newGuid(), SnapshotStatus.OK, vmId, null, SnapshotType.REGULAR, "", new Date(), "");
        disk1 = new DiskImage();
        disk2 = new DiskImage();
        snapshotVmConfigurationHelper = spy(new SnapshotVmConfigurationHelper());
        when(getQuery().getSnapshotVmConfigurationHelper()).thenReturn(snapshotVmConfigurationHelper);
        setUpDaoMocks();
    }

    private void setUpDaoMocks() {
        snapshotDaoMock = mock(SnapshotDao.class);
        doReturn(snapshotDaoMock).when(getQuery()).getSnapshotDao();
        when(snapshotDaoMock.getAllWithConfiguration(vmId)).thenReturn(Arrays.asList(snapshot));
    }

    @Test
    public void testExecuteQueryCommand() {
        IdQueryParameters params = getQueryParameters();
        when(params.getId()).thenReturn(vmId);

        disk1.setId(Guid.newGuid());
        disk2.setId(Guid.newGuid());

        VM vm = new VM();
        vm.setId(vmId);
        vm.setImages(new ArrayList<>(Arrays.asList(disk1, disk2)));

        doReturn(vm).when(snapshotVmConfigurationHelper).getVmFromConfiguration(
                any(String.class), any(Guid.class), any(Guid.class));
        getQuery().executeQueryCommand();
        List<Snapshot> snapshots = getQuery().getQueryReturnValue().getReturnValue();

        // Assert the correct disks are returned
        assertTrue("snapshot should be in the return value", snapshots.contains(snapshot));
        assertEquals("there should be exactly one snapshot returned", 1, snapshots.size());
        assertEquals("snapshot should contain a list of 2 diskImages", 2, snapshots.get(0).getDiskImages().size());
    }
}
