package org.ovirt.engine.core.bll.snapshots;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
 * A test case for {@link GetAllVmSnapshotsWithLeasesFromConfigurationByVmIdQuery}.
 */
public class GetAllVmSnapshotsWithLeasesFromConfigurationByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters,
        GetAllVmSnapshotsWithLeasesFromConfigurationByVmIdQuery<IdQueryParameters>> {

    /** The {@link org.ovirt.engine.core.dao.SnapshotDao} mocked for the test */
    @Mock
    private SnapshotDao snapshotDaoMock;

    /** The ID of the VM the disks belong to */
    private Guid vmId;

    /** A snapshot for the test */
    private Snapshot snapshot;

    /** The disks to use for testing */
    private DiskImage disk1;
    private DiskImage disk2;

    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    @BeforeEach
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
        when(snapshotDaoMock.getAllWithConfiguration(vmId)).thenReturn(Collections.singletonList(snapshot));
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

        doReturn(vm).when(snapshotVmConfigurationHelper).getVmFromConfiguration(any());
        getQuery().executeQueryCommand();
        Map<Snapshot, Guid> snapshots = getQuery().getQueryReturnValue().getReturnValue();

        // Assert the correct disks are returned
        assertEquals(1, snapshots.keySet().size(), "there should be exactly one snapshot returned");
        assertTrue(snapshots.containsKey(snapshot), "snapshot should be in the return value");
        Snapshot snap = (Snapshot) snapshots.keySet().toArray()[0];
        assertEquals(2, snap.getDiskImages().size(), "snapshot should contain a list of 2 diskImages");
    }
}
