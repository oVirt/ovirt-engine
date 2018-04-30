package org.ovirt.engine.core.bll.snapshots;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RestoreAllSnapshotsCommandTest extends BaseCommandTest {
    @Mock
    private VmDao vmDao;

    @Mock
    private VmDynamicDao vmDynamicDao;

    @Mock
    private DiskDao diskDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    protected SnapshotDao snapshotDao;

    @Mock
    private MultipleStorageDomainsValidator storageValidator;

    @Spy
    @InjectMocks
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private VmValidator vmValidator;

    private Guid vmId = Guid.newGuid();
    private Guid diskImageId = Guid.newGuid();
    private Guid storageDomainId = Guid.newGuid();
    private Guid spId = Guid.newGuid();
    private Snapshot mockSnapshot;

    @Spy
    @InjectMocks
    private RestoreAllSnapshotsCommand<RestoreAllSnapshotsParameters> spyCommand = createCommand();

    private RestoreAllSnapshotsCommand<RestoreAllSnapshotsParameters> createCommand() {
        RestoreAllSnapshotsParameters parameters = new RestoreAllSnapshotsParameters(vmId, SnapshotActionEnum.COMMIT);
        List<DiskImage> diskImageList = createDiskImageList();
        parameters.setImages(diskImageList);
        return new RestoreAllSnapshotsCommand<>(parameters, null);
    }

    @BeforeEach
    public void setupCommand() {
        initSpyCommand();
        mockDaos();
        mockVm();
    }

    @Test
    public void validateFailsOnSnapshotNotExists() {
        when(snapshotDao.exists(any(Guid.class), any(Guid.class))).thenReturn(false);
        ValidateTestUtils.runAndAssertValidateFailure
                (spyCommand, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void validateFailsOnSnapshotTypeRegularNotInPreview() {
        mockSnapshotExists();
        mockSnapshot = new Snapshot();
        when(snapshotDao.exists(nullable(Guid.class), nullable(Guid.class))).thenReturn(true);
        mockSnapshotFromDb();
        mockSnapshot.setType(SnapshotType.REGULAR);
        mockSnapshot.setStatus(SnapshotStatus.OK);
        ValidateTestUtils.runAndAssertValidateFailure
                (spyCommand, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_NOT_IN_PREVIEW);
    }

    private List<DiskImage> createDiskImageList() {
        DiskImage disk = new DiskImage();
        disk.setImageId(diskImageId);
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomainId)));
        return Collections.singletonList(disk);
    }

    private void mockSnapshotExists() {
        when(snapshotDao.exists(any(Guid.class), any(Guid.class))).thenReturn(true);
    }

    private void mockSnapshotFromDb() {
        mockSnapshot = new Snapshot();
        mockSnapshot.setType(SnapshotType.STATELESS);
        when(snapshotDao.get(any(Guid.class), any(SnapshotType.class), any(SnapshotStatus.class))).thenReturn(mockSnapshot);
        when(snapshotDao.get(any(Guid.class), any(SnapshotType.class))).thenReturn(mockSnapshot);
        when(snapshotDao.get(any(Guid.class), any(SnapshotStatus.class))).thenReturn(mockSnapshot);
    }

    private void initSpyCommand() {
        doReturn(true).when(spyCommand).performImagesChecks();
        doReturn(storageValidator).when(spyCommand).createStorageDomainValidator();
        doReturn(vmValidator).when(spyCommand).createVmValidator(any());
    }

    private void mockDaos() {
        mockDiskImageDao();
        mockStorageDomainDao();
        mockStoragePoolDao();
        mockDynamicVmDao();
    }

    private void mockDynamicVmDao() {
        VmDynamic mockDynamicVm = getVmDynamic();
        when(vmDynamicDao.get(vmId)).thenReturn(mockDynamicVm);
    }

    /**
     * Mock disk image Dao.
     */
    private void mockDiskImageDao() {
        List<Disk> diskImageList = new ArrayList<>();
        DiskImage diskImage = new DiskImage();
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(Guid.newGuid())));
        diskImageList.add(diskImage);
        when(diskDao.getAllForVm(vmId)).thenReturn(diskImageList);
    }

    private void mockStorageDomainDao() {
        StorageDomain storageDomains = new StorageDomain();
        storageDomains.setStatus(StorageDomainStatus.Active);

        // Variables only for passing the available size check.
        storageDomains.setAvailableDiskSize(10000000);
        storageDomains.setUsedDiskSize(10);
    }

    private void mockStoragePoolDao() {
        StoragePool sp = new StoragePool();
        sp.setId(spId);
        sp.setStatus(StoragePoolStatus.Up);
        when(storagePoolDao.get(spId)).thenReturn(sp);
    }

    /**
     * Mock a VM.
     */
    private VM mockVm() {
        VM vm = new VM();
        vm.setId(vmId);
        vm.setStoragePoolId(spId);
        vm.setStatus(VMStatus.Down);
        when(vmDao.get(vmId)).thenReturn(vm);
        return vm;
    }

    /**
     * Mock a VM.
     */
    private VmDynamic getVmDynamic() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(vmId);
        vmDynamic.setStatus(VMStatus.Down);
        return vmDynamic;
    }
}
