package org.ovirt.engine.core.bll.storage.disk.image;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.ConvertDiskCommandParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ConvertDiskCommandTest extends BaseCommandTest {

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private VmDao vmDao;

    private final Guid storageDomainId = Guid.newGuid();
    private DiskImage diskImage = createDiskImage();
    private StorageDomain storageDomain = createStorageDomain();

    @Spy
    @InjectMocks
    private ConvertDiskCommand<ConvertDiskCommandParameters> command = createCommand();

    @Test
    public void validateStorageDomainMissing() {
        setCommandParameters(VolumeFormat.RAW, VolumeType.Preallocated);
        DiskImage diskImage = createDiskImage();

        doReturn(diskImage).when(diskImageDao).get(any());
        doReturn(null).when(storageDomainDao).get(any());
        command.init();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.STORAGE_DOMAIN_DOES_NOT_EXIST);
    }

    @Test
    public void validateConflictingDiskConfiguration() {
        setCommandParameters(VolumeFormat.RAW, VolumeType.Sparse);
        initMocks();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED);
    }

    @Test
    public void validateFailsWithRunningVm() {
        VM vm = createVm();
        Map<Boolean, List<VM>> vms = new HashMap<>();
        vms.put(Boolean.TRUE, Arrays.asList(vm));

        doReturn(vms).when(vmDao).getForDisk(diskImage.getId(), true);
        initMocks();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_RUNNING);
    }

    private StorageDomain createStorageDomain() {
        StorageDomain sd = new StorageDomain();
        sd.setId(storageDomainId);
        sd.setStorageType(StorageType.ISCSI);
        return sd;
    }

    private DiskImage createDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setImageId(Guid.newGuid());
        diskImage.setStorageIds(Arrays.asList(storageDomainId));
        return diskImage;
    }

    private VM createVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setStatus(VMStatus.Up);

        return vm;
    }

    private void setCommandParameters(VolumeFormat format, VolumeType preallocation) {
        ConvertDiskCommandParameters params = command.getParameters();
        params.setVolumeFormat(format);
        params.setPreallocation(preallocation);

        VdsmImageLocationInfo locationInfo = new VdsmImageLocationInfo();
        locationInfo.setStorageDomainId(storageDomainId);
        locationInfo.setImageGroupId(diskImage.getId());
        locationInfo.setImageId(diskImage.getImageId());
        params.setLocationInfo(locationInfo);
    }

    private void initMocks() {
        doReturn(diskImage).when(diskImageDao).get(any());
        doReturn(storageDomain).when(storageDomainDao).get(any());
        command.init();
    }

    private ConvertDiskCommand<ConvertDiskCommandParameters> createCommand() {
        return new ConvertDiskCommand<>(new ConvertDiskCommandParameters(), null);
    }
}
