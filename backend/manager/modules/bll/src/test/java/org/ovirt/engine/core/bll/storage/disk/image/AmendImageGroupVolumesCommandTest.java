package org.ovirt.engine.core.bll.storage.disk.image;

import static org.mockito.Mockito.doReturn;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AmendImageGroupVolumesCommandParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class AmendImageGroupVolumesCommandTest extends BaseCommandTest {

    @Mock
    private DiskDao diskDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    private Guid diskId = Guid.newGuid();
    private Guid storagePoolId = Guid.newGuid();
    private Guid storageDomainId = Guid.newGuid();
    private StoragePool storagePool;
    private StorageDomain storageDomain;
    private DiskImage diskImage;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.QcowCompatSupported, Version.v4_2, true));

    @Spy
    @InjectMocks
    private AmendImageGroupVolumesCommand<AmendImageGroupVolumesCommandParameters> command =
            new AmendImageGroupVolumesCommand<>(createParameters(), CommandContext.createContext(""));

    @Before
    public void setup() {
        diskImage = new DiskImage();
        diskImage.setStoragePoolId(storagePoolId);
        diskImage.setVmEntityType(VmEntityType.VM);
        diskImage.setId(diskId);
        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(storageDomainId);
        diskImage.setStorageIds(storageIds);
        doReturn(diskImage).when(diskDao).get(diskId);
        mockRunningStoragePool();
        mockRunningStorageDomain();
    }

    @Test
    public void testValidationPasses() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidationFailsDiskNotExists() {
        doReturn(null).when(diskDao).get(diskId);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testValidationFailsDiskConnectedToRunningVm() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Up);
        VmDevice vmDevice = new VmDevice();
        vmDevice.setPlugged(true);
        List<Pair<VM, VmDevice>> vms = Collections.singletonList(new Pair<>(vm, vmDevice));
        doReturn(vms).when(vmDao).getVmsWithPlugInfo(diskId);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void testValidationFailsStoragePoolNotExists() {
        doReturn(null).when(storagePoolDao).get(storagePoolId);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }

    @Test
    public void testValidationFailsStoragePoolNotUp() {
        storagePool.setStatus(StoragePoolStatus.NotOperational);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }

    @Test
    public void testValidationFailsStorageDomainNotExists() {
        doReturn(null).when(storageDomainDao).getForStoragePool(storageDomainId, storagePoolId);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testValidationFailsStorageDomainNotUp() {
        storageDomain.setStatus(StorageDomainStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testValidationFailsAmendNotSupported() {
        mcr.mockConfigValue(ConfigValues.QcowCompatSupported, Version.v4_2, false);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_AMEND_NOT_SUPPORTED_BY_DC_VERSION);
    }

    @Test
    public void testValidationFailsForTemplateDisk() {
        diskImage.setVmEntityType(VmEntityType.TEMPLATE);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_CANT_AMEND_TEMPLATE_DISK);
    }

    @Test
    public void testValidationFailsForIllegalDisk() {
        diskImage.setImageStatus(ImageStatus.ILLEGAL);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL);
    }

    @Test
    public void testValidationFailsForLockedDisk() {
        diskImage.setImageStatus(ImageStatus.LOCKED);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    private void mockRunningStoragePool() {
        storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        storagePool.setId(storagePoolId);
        storagePool.setCompatibilityVersion(Version.v4_2);
        doReturn(storagePool).when(storagePoolDao).get(storagePool.getId());
    }

    private void mockRunningStorageDomain() {
        storageDomain = new StorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Active);
        doReturn(storageDomain).when(storageDomainDao).getForStoragePool(storageDomainId, storagePoolId);
    }

    private AmendImageGroupVolumesCommandParameters createParameters() {
        return new AmendImageGroupVolumesCommandParameters(diskId, QcowCompat.QCOW2_V2);
    }
}
