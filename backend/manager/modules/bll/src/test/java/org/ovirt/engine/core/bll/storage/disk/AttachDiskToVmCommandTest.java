package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttachDiskToVmCommandTest {

    private Guid vmId = Guid.newGuid();
    private Guid diskId = Guid.newGuid();
    private Guid storageId = Guid.newGuid();
    private DiskImage disk = createDiskImage();

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Mock
    private DiskVmElementValidator diskVmElementValidator;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private DiskHandler diskHandler;

    private AttachDetachVmDiskParameters parameters = createParameters();

    @Spy
    @InjectMocks
    private AttachDiskToVmCommand<AttachDetachVmDiskParameters> command = new AttachDiskToVmCommand<>(parameters, null);

    @BeforeEach
    public void initTest() {
        initialSetup();
        initCommand();
    }

    private void initialSetup() {
        mockValidators();
        doNothing().when(command).updateDisksFromDb();
        doReturn(mockVm()).when(command).getVm();

        doReturn(disk).when(diskHandler).loadActiveDisk(any());
        doReturn(disk).when(diskHandler).loadDiskFromSnapshot(any(), any());

        doReturn(true).when(command).isDiskPassPciAndIdeLimit();
        doReturn(false).when(command).isOperationPerformedOnDiskSnapshot();
        doReturn(ActionType.AttachDiskToVm).when(command).getActionType();

        mockStoragePoolIsoMap();
    }

    private void initCommand() {
        command.init();
    }

    @Test
    public void testValidateSucceed() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateSucceedReadOnlyWithInterface() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        verify(diskVmElementValidator).isReadOnlyPropertyCompatibleWithInterface();
    }

    @Test
    public void testValidateFailReadOnlyOnInterface() {
        when(diskVmElementValidator.isReadOnlyPropertyCompatibleWithInterface()).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR);
        verify(diskVmElementValidator).isReadOnlyPropertyCompatibleWithInterface();
    }

    @Test
    public void testValidateFailsWhenDiscardIsNotSupported() {
        when(diskVmElementValidator.isPassDiscardSupported(any())).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE);
    }

    @Test
    public void testValidateFailsWhenContentTypeNotSupported() {
        disk.setContentType(DiskContentType.OVF_STORE);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DISK_CONTENT_TYPE_NOT_SUPPORTED_FOR_OPERATION);
    }

    @Test
    public void testValidateFailsWhenDiskIsOnBackupStorageDomain() {
        StorageDomain sd = mock(StorageDomain.class);
        when(sd.isBackup()).thenReturn(true);
        when(command.getStorageDomainValidator(any())).thenReturn(new StorageDomainValidator(sd));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_DISKS_ON_BACKUP_STORAGE);
    }

    private AttachDetachVmDiskParameters createParameters() {
        DiskVmElement dve = new DiskVmElement(diskId, vmId);
        dve.setReadOnly(true);
        return new AttachDetachVmDiskParameters(dve);
    }

    private VM mockVm() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(Guid.newGuid());
        vm.setId(vmId);

        return vm;
    }

    private void mockStorageDomainValidator() {
        doReturn(storageDomainValidator).when(command).getStorageDomainValidator(any());
    }

    private void mockDiskVmElementValidator() {
        doReturn(diskVmElementValidator).when(command).getDiskVmElementValidator(any(), any());
    }

    private void mockManagedBlockSupportedValidation() {
        doReturn(true).when(command).isSupportedByManagedBlockStorageDomain(any());
    }

    private void mockValidators() {
        mockStorageDomainValidator();
        mockDiskVmElementValidator();
        mockManagedBlockSupportedValidation();
    }

    private void mockStoragePoolIsoMap() {
        StoragePoolIsoMap spim = new StoragePoolIsoMap();
        when(storagePoolIsoMapDao.get(any())).thenReturn(spim);
    }

    private DiskImage createDiskImage() {
        disk = new DiskImage();
        disk.setId(diskId);
        Collections.singletonList(storageId);
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(storageId)));
        return disk;
    }
}
