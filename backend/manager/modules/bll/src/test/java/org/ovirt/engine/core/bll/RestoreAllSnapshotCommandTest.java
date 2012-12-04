package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class RestoreAllSnapshotCommandTest {
    @ClassRule
    public static MockConfigRule mcr =
            new MockConfigRule(
                    mockConfig(ConfigValues.FreeSpaceLow, 5),
                    mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, 5)
            );

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private VmDynamicDAO vmDynamicDAO;

    @Mock
    private BackendInternal backend;

    @Mock
    private DiskDao diskDao;

    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private VmNetworkInterfaceDAO vmNetworkInterfaceDAO;

    private Guid vmId = Guid.NewGuid();
    private Guid diskImageId = Guid.NewGuid();
    private Guid storageDomainId = Guid.NewGuid();
    private Guid dstSnapshotId = Guid.NewGuid();
    private VmDynamic mockDynamicVm;
    private Snapshot mockSnapshot;
    private RestoreAllSnapshotsCommand<RestoreAllSnapshotsParameters> spyCommand;

    @Before
    public void setupCommand() {
        initSpyCommand();
        mockBackend();
        mockDaos();
        mockVm();
    }

    protected void mockBackend() {
        doReturn(backend).when(spyCommand).getBackend();
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
    }

    @Test
    public void canDoActionFailsOnSnapshotNotExists() {
        when(snapshotDao.exists(any(Guid.class), any(Guid.class))).thenReturn(false);
        assertFalse(spyCommand.canDoAction());
        assertTrue(spyCommand.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionFailsOnSnapshotTypeRegularNotInPreview() {
        mockSnapshotExists();
        mockSnapshotFromDb();
        mockSnapshot.setType(SnapshotType.REGULAR);
        mockSnapshot.setStatus(SnapshotStatus.OK);
        assertFalse(spyCommand.canDoAction());
        assertTrue(spyCommand.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_NOT_IN_PREVIEW.toString()));
    }

    private List<DiskImage> createDiskImageList() {
        DiskImage disk = new DiskImage();
        disk.setImageId(diskImageId);
        disk.setstorage_ids(new ArrayList<Guid>(Arrays.asList(storageDomainId)));
        List<DiskImage> diskImageList = new ArrayList<DiskImage>();
        diskImageList.add(disk);
        return diskImageList;
    }

    private void mockSnapshotExists() {
        when(snapshotDao.exists(any(Guid.class), any(Guid.class))).thenReturn(true);
    }

    private void mockSnapshotFromDb() {
        mockSnapshot = new Snapshot();
        mockSnapshot.setType(SnapshotType.STATELESS);
        when(snapshotDao.get(any(Guid.class))).thenReturn(mockSnapshot);
    }

    private void initSpyCommand() {
        RestoreAllSnapshotsParameters parameters = new RestoreAllSnapshotsParameters(vmId, dstSnapshotId);
        List<DiskImage> diskImageList = createDiskImageList();
        parameters.setImagesList(diskImageList);
        spyCommand = spy(new RestoreAllSnapshotsCommand<RestoreAllSnapshotsParameters>(parameters));
        doReturn(true).when(spyCommand).performImagesChecks();
    }

    private void mockDaos() {
        mockDiskImageDao();
        mockStorageDomainDao();
        mockDynamicVmDao();
        doReturn(snapshotDao).when(spyCommand).getSnapshotDao();
        doReturn(vmNetworkInterfaceDAO).when(spyCommand).getVmNetworkInterfaceDao();
    }

    private void mockDynamicVmDao() {
        mockDynamicVm = getVmDynamic();
        doReturn(vmDynamicDAO).when(spyCommand).getVmDynamicDao();
        when(vmDynamicDAO.get(vmId)).thenReturn(mockDynamicVm);
    }

    /**
     * Mock disk image Dao.
     */
    private void mockDiskImageDao() {
        List<Disk> diskImageList = new ArrayList<Disk>();
        DiskImage diskImage = new DiskImage();
        diskImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(Guid.NewGuid())));
        diskImageList.add(diskImage);
        doReturn(diskDao).when(spyCommand).getDiskDao();
        when(diskDao.getAllForVm(vmId)).thenReturn(diskImageList);
    }

    private void mockStorageDomainDao() {
        storage_domains storageDomains = new storage_domains();
        storageDomains.setstatus(StorageDomainStatus.Active);

        // Variables only for passing the available size check.
        storageDomains.setavailable_disk_size(10000000);
        storageDomains.setused_disk_size(10);
        doReturn(storageDomainDAO).when(spyCommand).getStorageDomainDAO();
        when(storageDomainDAO.getForStoragePool(storageDomainId, Guid.Empty)).thenReturn(storageDomains);
        when(storageDomainDAO.get(storageDomainId)).thenReturn(storageDomains);
    }

    /**
     * Mock a VM.
     */
    private VM mockVm() {
        VM vm = new VM();
        vm.setId(vmId);
        vm.setstatus(VMStatus.Down);
        AuditLogableBaseMockUtils.mockVmDao(spyCommand, vmDAO);
        when(vmDAO.get(vmId)).thenReturn(vm);
        return vm;
    }

    /**
     * Mock a VM.
     */
    private VmDynamic getVmDynamic() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(vmId);
        vmDynamic.setstatus(VMStatus.Down);
        return vmDynamic;
    }
}
