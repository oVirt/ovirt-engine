package org.ovirt.engine.core.bll.snapshots;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

/** A test case for the {@link RemoveSnapshotCommand} class. */
public class RemoveSnapshotCommandTest extends BaseCommandTest {

    /** The command to test */
    private RemoveSnapshotCommand<RemoveSnapshotParameters> cmd;

    @Mock
    private VmTemplateDao vmTemplateDao;

    @Mock
    StorageDomainDao sdDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private StoragePoolDao spDao;

    @Mock
    private SnapshotDao snapshotDao;

    private SnapshotsValidator snapshotValidator;

    private VmValidator vmValidator;

    private MultipleStorageDomainsValidator storageDomainsValidator;

    private static final Guid STORAGE_DOMAIN_ID = Guid.newGuid();
    private static final Guid STORAGE_POOL_ID = Guid.newGuid();

    @Before
    public void setUp() {
        Guid vmGuid = Guid.newGuid();
        Guid snapGuid = Guid.newGuid();

        RemoveSnapshotParameters params = new RemoveSnapshotParameters(snapGuid, vmGuid);
        cmd = spy(new RemoveSnapshotCommand<>(params, null));
        doReturn(spDao).when(cmd).getStoragePoolDao();
        doReturn(vmTemplateDao).when(cmd).getVmTemplateDao();
        doReturn(diskImageDao).when(cmd).getDiskImageDao();
        doReturn(sdDao).when(cmd).getStorageDomainDao();
        doReturn(snapshotDao).when(cmd).getSnapshotDao();
        mockVm();
        vmValidator = spy(new VmValidator(cmd.getVm()));
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotHavingDeviceSnapshotsAttachedToOtherVms(anyBoolean());
        doReturn(vmValidator).when(cmd).createVmValidator(any(VM.class));
        doReturn(STORAGE_POOL_ID).when(cmd).getStoragePoolId();
        mockSnapshot(SnapshotType.REGULAR);
        snapshotValidator = spy(new SnapshotsValidator());
        doReturn(snapshotValidator).when(cmd).createSnapshotValidator();
        spySdValidator();
    }

    private void mockVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(STORAGE_POOL_ID);
        doReturn(vm).when(cmd).getVm();
    }

    private void mockSnapshot(SnapshotType snapshotType) {
        Snapshot snapshot = new Snapshot();
        snapshot.setId(cmd.getParameters().getSnapshotId());
        snapshot.setType(snapshotType);
        doReturn(snapshot).when(snapshotDao).get(snapshot.getId());
    }

    private void spySdValidator() {
        Set<Guid> sdIds = new HashSet<>(Arrays.asList(STORAGE_DOMAIN_ID));
        storageDomainsValidator = spy(new MultipleStorageDomainsValidator(STORAGE_POOL_ID, sdIds));
        doReturn(storageDomainsValidator).when(cmd).getStorageDomainsValidator(any(Guid.class), anySet());
        doReturn(sdDao).when(storageDomainsValidator).getStorageDomainDao();
        doReturn(sdIds).when(cmd).getStorageDomainsIds();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsExistAndActive();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsHaveSpaceForClonedDisks(anyList());
    }

    @Test
    public void testValidateImageNotInTemplateTrue() {
        when(vmTemplateDao.get(mockSourceImageAndGetId())).thenReturn(null);
        assertTrue("validation should succeed", cmd.validateImageNotInTemplate());
    }

    @Test
    public void testValidateImageNotInTemplateFalse() {
        when(vmTemplateDao.get(mockSourceImageAndGetId())).thenReturn(new VmTemplate());
        assertFalse("validation should succeed", cmd.validateImageNotInTemplate());
    }

    @Test
    public void testValidateSnapshotNotActiveTrue() {
        mockSnapshot(SnapshotType.REGULAR);
        assertTrue("validation should succeed", cmd.validateSnapshotType());
    }

    @Test
    public void testValidateSnapshotNotActiveFalse() {
        mockSnapshot(SnapshotType.ACTIVE);
        assertFalse("validation should fail", cmd.validateSnapshotType());
    }

    @Test
    public void testValidateEnoughSpace() {
        prepareForVmValidatorTests();
        spySdValidator();
        cmd.getVm().setStatus(VMStatus.Up);

        mockDisksList(4);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateNotEnoughSpace() {
        prepareForVmValidatorTests();
        spySdValidator();
        cmd.getVm().setStatus(VMStatus.Up);

        List<DiskImage> imagesDisks = mockDisksList(4);
        when(storageDomainsValidator.allDomainsHaveSpaceForClonedDisks(imagesDisks)).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    private void prepareForVmValidatorTests() {
        StoragePool sp = new StoragePool();
        sp.setId(STORAGE_POOL_ID);
        sp.setStatus(StoragePoolStatus.Up);

        cmd.setSnapshotName("someSnapshot");
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmNotInPreview(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotValidator).snapshotExists(any(Guid.class), any(Guid.class));
        doReturn(true).when(cmd).validateImages();
        doReturn(sp).when(spDao).get(STORAGE_POOL_ID);
        doReturn(Collections.emptyList()).when(cmd).getSourceImages();
    }

    @Test
    public void testValidateVmUpHostCapable() {
        prepareForVmValidatorTests();
        cmd.getVm().setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateVmDown() {
        prepareForVmValidatorTests();
        cmd.getVm().setStatus(VMStatus.Down);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateVmMigrating() {
        prepareForVmValidatorTests();
        cmd.getVm().setStatus(VMStatus.MigratingTo);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP);
    }

    /** Mocks a call to {@link RemoveSnapshotCommand#getSourceImages()} and returns its image guid */
    private Guid mockSourceImageAndGetId() {
        return mockSourceImage().getImageId();
    }

    private DiskImage createDiskImage(Guid storageDomainId) {
        DiskImage image = new DiskImage();
        image.setImageId(Guid.newGuid());
        ArrayList<Guid> sdIds = new ArrayList<>(Arrays.asList(storageDomainId));
        image.setStorageIds(sdIds);
        return image;
    }

    /** Mocks a call to {@link RemoveSnapshotCommand#getSourceImages()} and returns the DiskImage */
    private DiskImage mockSourceImage() {
        DiskImage image = createDiskImage(STORAGE_DOMAIN_ID);
        doReturn(Collections.singletonList(image)).when(cmd).getSourceImages();
        when(diskImageDao.get(image.getImageId())).thenReturn(image);

        return image;
    }

    /** Mocks a call to {@link RemoveSnapshotCommand#getSourceImages()} and returns list of images */
    private List<DiskImage> mockDisksList(int numberOfDisks) {
        List<DiskImage> disksList = new ArrayList<>(numberOfDisks);
        for (int index = 0; index < numberOfDisks; index++) {
            DiskImage image =createDiskImage(STORAGE_DOMAIN_ID);
            disksList.add(image);
        }
        doReturn(disksList).when(cmd).getSourceImages();
        doReturn(disksList).when(cmd).getDisksListForStorageAllocations();
        return disksList;
    }
}
