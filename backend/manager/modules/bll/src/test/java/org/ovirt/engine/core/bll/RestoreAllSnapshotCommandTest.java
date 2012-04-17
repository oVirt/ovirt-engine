package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Backend.class, Config.class })
public class RestoreAllSnapshotCommandTest {

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private VmDynamicDAO vmDynamicDAO;

    @Mock
    private BackendInternal backend;

    @Mock
    private DiskImageDAO diskImageDAO;

    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private DbFacade dbFacade;

    private Guid vmId = Guid.NewGuid();
    private Guid diskImageId = Guid.NewGuid();
    private Guid storageDomainId = Guid.NewGuid();
    private Guid dstSnapshotId = Guid.NewGuid();
    private VmDynamic mockDynamicVm;
    private Snapshot mockSnapshot;
    private RestoreAllSnapshotsCommand<RestoreAllSnapshotsParameters> spyCommand;

    public RestoreAllSnapshotCommandTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(Backend.class);
        mockStatic(Config.class);
    }

    @Before
    public void setupCommand() {
        when(Backend.getInstance()).thenReturn(backend);
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
        mockConfig();
        initSpyCommand();
        mockDaos();
        mockVm(spyCommand);

        mockIsValidVdsCommand();
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
        disk.setId(diskImageId);
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

    /**
     * Returns image is valid.
     */
    private void mockIsValidVdsCommand() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(Boolean.TRUE);
        when(backend.getResourceManager().RunVdsCommand(eq(VDSCommandType.IsValid),
                any(IrsBaseVDSCommandParameters.class))).thenReturn(returnValue);
    }

    private void mockConfig() {
        when(Config.<Object> GetValue(ConfigValues.FreeSpaceLow)).thenReturn(new Integer("5"));
        when(Config.<Object> GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(new Integer("5"));
    }

    private void initSpyCommand() {
        RestoreAllSnapshotsParameters parameters = new RestoreAllSnapshotsParameters(vmId, dstSnapshotId);
        List<DiskImage> diskImageList = createDiskImageList();
        parameters.setImagesList(diskImageList);
        spyCommand = spy(new RestoreAllSnapshotsCommand<RestoreAllSnapshotsParameters>(parameters));
    }

    private void mockDaos() {
        when(DbFacade.getInstance()).thenReturn(dbFacade);
        mockDiskImageDao();
        mockStorageDomainDao();
        mockDynamicVmDao();
        doReturn(snapshotDao).when(spyCommand).getSnapshotDao();
    }

    private void mockDynamicVmDao() {
        mockDynamicVm = getVmDynamic();
        when(dbFacade.getVmDynamicDAO()).thenReturn(vmDynamicDAO);
        when(vmDynamicDAO.get(vmId)).thenReturn(mockDynamicVm);
    }

    /**
     * Mock disk image Dao.
     */
    private void mockDiskImageDao() {
        List<DiskImage> diskImageList = new ArrayList<DiskImage>();
        DiskImage diskImage = new DiskImage();
        diskImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(Guid.NewGuid())));
        diskImageList.add(diskImage);
        when(dbFacade.getDiskImageDAO()).thenReturn(diskImageDAO);
        when(diskImageDAO.getAllForVm(vmId)).thenReturn(diskImageList);
    }

    private void mockStorageDomainDao() {
        storage_domains storageDomains = new storage_domains();
        storageDomains.setstatus(StorageDomainStatus.Active);

        // Variables only for passing the available size check.
        storageDomains.setavailable_disk_size(new Integer("10000000"));
        storageDomains.setused_disk_size(new Integer("10"));
        when(dbFacade.getStorageDomainDAO()).thenReturn(storageDomainDAO);
        when(storageDomainDAO.getForStoragePool(storageDomainId, Guid.Empty)).thenReturn(storageDomains);
        when(storageDomainDAO.get(storageDomainId)).thenReturn(storageDomains);
    }

    /**
     * Mock a VM.
     */
    private VM mockVm(RestoreAllSnapshotsCommand<RestoreAllSnapshotsParameters> restoreAllSnapshotsCommand) {
        VM vm = new VM();
        vm.setId(vmId);
        vm.setstatus(VMStatus.Down);
        AuditLogableBaseMockUtils.mockVmDao(restoreAllSnapshotsCommand, vmDAO);
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
