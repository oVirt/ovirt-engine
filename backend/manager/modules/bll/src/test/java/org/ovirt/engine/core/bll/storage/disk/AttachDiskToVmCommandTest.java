package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

@RunWith(MockitoJUnitRunner.class)
public class AttachDiskToVmCommandTest {

    private Guid vmId = Guid.newGuid();
    private Guid diskId = Guid.newGuid();
    private Guid storageId = Guid.newGuid();

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private DiskValidator diskValidator;

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

    @Before
    public void initTest() {
        initialSetup();
        initCommand();
    }

    private void initialSetup() {
        mockValidators();
        doNothing().when(command).updateDisksFromDb();
        doReturn(mockVm()).when(command).getVm();

        doReturn(createDiskImage()).when(diskHandler).loadActiveDisk(any(Guid.class));
        doReturn(createDiskImage()).when(diskHandler).loadDiskFromSnapshot(any(Guid.class), any(Guid.class));

        doReturn(true).when(command).isDiskPassPciAndIdeLimit();
        doReturn(true).when(command).checkDiskUsedAsOvfStore(diskValidator);
        doReturn(false).when(command).isOperationPerformedOnDiskSnapshot();

        doReturn(vmDeviceDao).when(command).getVmDeviceDao();
        doReturn(storageDomainDao).when(command).getStorageDomainDao();
        doReturn(storagePoolIsoMapDao).when(command).getStoragePoolIsoMapDao();
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
        verify(diskValidator).isReadOnlyPropertyCompatibleWithInterface(parameters.getDiskVmElement());
    }

    @Test
    public void testValidateFailReadOnlyOnInterface() {
        when(diskValidator.isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class))).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR);
        verify(diskValidator).isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class));
    }

    private AttachDetachVmDiskParameters createParameters() {
        AttachDetachVmDiskParameters parameters = new AttachDetachVmDiskParameters(new DiskVmElement(diskId, vmId));
        parameters.setReadOnly(true);
        return parameters;
    }

    private VM mockVm() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(Guid.newGuid());
        vm.setId(vmId);
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);

        return vm;
    }

    private void mockStorageDomainValidator() {
        doReturn(storageDomainValidator).when(command).getStorageDomainValidator(any(StorageDomain.class));
        when(storageDomainValidator.isDomainExistAndActive()).thenReturn(ValidationResult.VALID);
    }

    private void mockDiskValidator() {
        doReturn(diskValidator).when(command).getDiskValidator(any(Disk.class));
        when(diskValidator.isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class))).thenReturn(ValidationResult.VALID);
        when(diskValidator.isVirtIoScsiValid(any(VM.class), any(DiskVmElement.class))).thenReturn(ValidationResult.VALID);
        when(diskValidator.isDiskInterfaceSupported(any(VM.class), any(DiskVmElement.class))).thenReturn(ValidationResult.VALID);
    }

    private void mockSnapshotsValidator() {
        doReturn(snapshotsValidator).when(command).getSnapshotsValidator();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));
    }

    private void mockValidators() {
        mockStorageDomainValidator();
        mockDiskValidator();
        mockSnapshotsValidator();
    }

    private void mockStoragePoolIsoMap() {
        StoragePoolIsoMap spim = new StoragePoolIsoMap();
        when(storagePoolIsoMapDao.get(any(StoragePoolIsoMapId.class))).thenReturn(spim);
    }

    private DiskImage createDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setId(diskId);
        Collections.singletonList(storageId);
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(storageId)));
        return disk;
    }
}
