package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmConfigurationBySnapshotQueryParams;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * A test case for {@GetVmConfigurationBySnapshotQuery}. This test mocks away all
 * the DAOs, and just tests the flow of the query itself.
 */
public class GetVmConfigurationBySnapshotQueryTest extends AbstractUserQueryTest<GetVmConfigurationBySnapshotQueryParams, GetVmConfigurationBySnapshotQuery<GetVmConfigurationBySnapshotQueryParams>> {
    private SnapshotDao snapshotDaoMock;
    private DiskImageDAO diskImageDaoMock;
    private Guid existingSnapshotId = Guid.NewGuid();
    private Guid existingVmId = Guid.NewGuid();
    private Guid existingImageId = Guid.NewGuid();
    private Guid existingImageGroupId = Guid.NewGuid();
    private Snapshot existingSnapshot;
    private VM existingVm = null;
    private SnapshotsManager snapshotsManager;
    private DiskImage existingDiskImage;

    private static final String EXISTING_VM_NAME = "Dummy configuration";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        existingSnapshot = createSnapshot(existingSnapshotId);
        existingVm = createVm(existingVmId);
        existingSnapshot.setVmConfiguration(EXISTING_VM_NAME); // Dummy configuration
        existingDiskImage = createDiskImage(existingImageId, existingImageGroupId);
        setUpDAOMocks();
    }

    private VM createVm(Guid existingVmId) {
        VM vm = new VM();
        vm.setId(existingVmId);
        return vm;
    }

    private Snapshot createSnapshot(Guid existingSnapshotId) {
        Snapshot snapshot = new Snapshot();
        snapshot.setId(existingSnapshotId);
        snapshot.setVmId(existingVmId);
        snapshot.setVmConfiguration(EXISTING_VM_NAME);
        return snapshot;
    }

    private DiskImage createDiskImage(Guid diskImageId, Guid imageGroupId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setImageId(diskImageId);
        diskImage.setId(imageGroupId);
        return diskImage;
    }

    private void setUpDAOMocks() {
        // Mock the DAOs
        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        snapshotDaoMock = mock(SnapshotDao.class);
        when(dbFacadeMock.getSnapshotDao()).thenReturn(snapshotDaoMock);
        diskImageDaoMock = mock(DiskImageDAO.class);
        when(dbFacadeMock.getDiskImageDao()).thenReturn(diskImageDaoMock);
        when(snapshotDaoMock.get(existingSnapshotId, getUser().getUserId(), getQueryParameters().isFiltered())).thenReturn(existingSnapshot);
        when(diskImageDaoMock.get(existingImageId)).thenReturn(existingDiskImage);
    }

    @Override
    protected GetVmConfigurationBySnapshotQuery<GetVmConfigurationBySnapshotQueryParams> setUpSpyQuery(GetVmConfigurationBySnapshotQueryParams params)
            throws Exception {
        GetVmConfigurationBySnapshotQuery<GetVmConfigurationBySnapshotQueryParams> result = super.setUpSpyQuery(params);
        snapshotsManager = mock(SnapshotsManager.class);
        when(result.getSnapshotManager()).thenReturn(snapshotsManager);
        return result;
    }

    @Test
    public void testQuery() throws Exception {
        GetVmConfigurationBySnapshotQuery<GetVmConfigurationBySnapshotQueryParams> query =
                setupQueryBySnapshotId(existingSnapshotId);
        VM vm = new VM();
        doReturn(vm).when(query).getVmFromConfiguration(any(String.class));
        query.execute();
        VdcQueryReturnValue returnValue = query.getQueryReturnValue();
        assertNotNull("Return value from query cannot be null", returnValue);
        VM returnedVm = (VM) returnValue.getReturnValue();
        assertEquals(vm, returnedVm);
    }

    @Test
    public void testNonExistingSnapshotQuery() throws Exception {
        GetVmConfigurationBySnapshotQuery<GetVmConfigurationBySnapshotQueryParams> query =
                setupQueryBySnapshotId(Guid.NewGuid());
        when(snapshotDaoMock.get(any(Guid.class))).thenReturn(null);
        VdcQueryReturnValue returnValue = query.getQueryReturnValue();
        VM returnedVm = (VM) returnValue.getReturnValue();
        assertNull("Return value from non existent query should be null", returnedVm);
    }

    @Test
    public void testIllegalImageReturnedByQuery() throws Exception {
        GetVmConfigurationBySnapshotQuery<GetVmConfigurationBySnapshotQueryParams> query =
                setupQueryBySnapshotId(existingSnapshotId);
        existingVm.getDiskMap().put(existingDiskImage.getId(), existingDiskImage);
        existingVm.getImages().add(existingDiskImage);
        doReturn(existingVm).when(query).getVmFromConfiguration(anyString());
        query.execute();
        VdcQueryReturnValue returnValue = query.getQueryReturnValue();
        assertNotNull("Return value from query cannot be null", returnValue);
        VM vm = (VM) returnValue.getReturnValue();
        for (Disk diskImage : vm.getDiskMap().values()) {
            assertEquals(((DiskImage)diskImage).getImageStatus(), ImageStatus.ILLEGAL);
        }
    }

    private GetVmConfigurationBySnapshotQuery<GetVmConfigurationBySnapshotQueryParams> setupQueryBySnapshotId(Guid snapshotId) {
        GetVmConfigurationBySnapshotQueryParams queryParams = getQueryParameters();
        when(queryParams.getSnapshotId()).thenReturn(snapshotId);
        GetVmConfigurationBySnapshotQuery<GetVmConfigurationBySnapshotQueryParams> query = getQuery();
        return query;
    }

}
