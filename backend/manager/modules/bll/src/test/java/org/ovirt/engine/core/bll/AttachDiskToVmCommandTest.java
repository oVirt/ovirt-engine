package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;

@RunWith(MockitoJUnitRunner.class)
public class AttachDiskToVmCommandTest {

    private Guid vmId;
    private Guid diskId;
    private Guid storageId;

    @Mock
    private DiskDao diskDao;

    @Mock
    private VmDeviceDAO vmDeviceDAO;

    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private StoragePoolIsoMapDAO storagePoolIsoMapDAO;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private DiskValidator diskValidator;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Before
    public void initTest() {
        initEntitiesIds();
        initCommand();
        initialSetup();
    }

    private void initEntitiesIds() {
        vmId = Guid.newGuid();
        diskId = Guid.newGuid();
        storageId = Guid.newGuid();
    }

    private void initialSetup() {
        mockValidators();
        doNothing().when(command).updateDisksFromDb();
        doReturn(mockVm()).when(command).getVm();

        doReturn(true).when(command).isDiskPassPciAndIdeLimit(any(Disk.class));
        doReturn(true).when(command).checkDiskUsedAsOvfStore(diskValidator);
        doReturn(false).when(command).isOperationPerformedOnDiskSnapshot();

        doReturn(vmDeviceDAO).when(command).getVmDeviceDao();
        doReturn(storageDomainDAO).when(command).getStorageDomainDAO();
        doReturn(storagePoolIsoMapDAO).when(command).getStoragePoolIsoMapDao();
        mockStoragePoolIsoMap();
    }

    private void initCommand() {
        when(diskDao.get(diskId)).thenReturn(createDiskImage());
        AttachDetachVmDiskParameters parameters = createParameters();
        command = spy(new AttachDiskToVmCommand<AttachDetachVmDiskParameters>(parameters) {
            // Overridden here and not during spying, since it's called in the constructor
            @SuppressWarnings("synthetic-access")
            @Override
            public DiskDao getDiskDao() {
                return diskDao;
            }
        });
    }

    /**
     * The command under test.
     */
    private AttachDiskToVmCommand<AttachDetachVmDiskParameters> command;

    @Test
    public void testCanDoSucceed() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void testCanDoSucceedReadOnlyWithInterface() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
        verify(diskValidator).isReadOnlyPropertyCompatibleWithInterface();
    }

    @Test
    public void testCanDoFailReadOnlyOnInterface() {
        when(diskValidator.isReadOnlyPropertyCompatibleWithInterface()).thenReturn(
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR);
        verify(diskValidator).isReadOnlyPropertyCompatibleWithInterface();
    }

    private AttachDetachVmDiskParameters createParameters() {
        AttachDetachVmDiskParameters parameters = new AttachDetachVmDiskParameters(vmId, diskId);
        parameters.setReadOnly(true);
        return parameters;
    }

    private VM mockVm() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(Guid.newGuid());
        vm.setId(vmId);
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(vm);

        return vm;
    }

    private void mockStorageDomainValidator() {
        doReturn(storageDomainValidator).when(command).getStorageDomainValidator(any(StorageDomain.class));
        when(storageDomainValidator.isDomainExistAndActive()).thenReturn(ValidationResult.VALID);
    }

    private void mockDiskValidator() {
        doReturn(diskValidator).when(command).getDiskValidator(any(Disk.class));
        when(diskValidator.isReadOnlyPropertyCompatibleWithInterface()).thenReturn(ValidationResult.VALID);
        when(diskValidator.isVirtIoScsiValid(any(VM.class))).thenReturn(ValidationResult.VALID);
        when(diskValidator.isDiskInterfaceSupported(any(VM.class))).thenReturn(ValidationResult.VALID);
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
        when(storagePoolIsoMapDAO.get(any(StoragePoolIsoMapId.class))).thenReturn(spim);
    }

    private DiskImage createDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setId(diskId);
        Collections.singletonList(storageId);
        disk.setStorageIds(new ArrayList<Guid>(Collections.singletonList(storageId)));
        return disk;
    }
}
