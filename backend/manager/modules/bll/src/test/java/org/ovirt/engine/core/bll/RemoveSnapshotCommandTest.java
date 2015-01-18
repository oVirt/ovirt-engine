package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

/** A test case for the {@link RemoveSnapshotCommand} class. */
@RunWith(MockitoJUnitRunner.class)
public class RemoveSnapshotCommandTest {

    /** The command to test */
    private RemoveSnapshotCommand<RemoveSnapshotParameters> cmd;

    @Rule
    public MockConfigRule mcr =
            new MockConfigRule(mockConfig(ConfigValues.LiveMergeSupported, Version.v3_5.toString(), true));

    @Mock
    private VmTemplateDAO vmTemplateDAO;

    @Mock
    StorageDomainDAO sdDAO;

    @Mock
    private DiskImageDAO diskImageDAO;

    @Mock
    private StoragePoolDAO spDao;

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
        cmd = spy(new RemoveSnapshotCommand<RemoveSnapshotParameters>(params));
        doReturn(spDao).when(cmd).getStoragePoolDAO();
        doReturn(vmTemplateDAO).when(cmd).getVmTemplateDAO();
        doReturn(diskImageDAO).when(cmd).getDiskImageDao();
        doReturn(sdDAO).when(cmd).getStorageDomainDAO();
        doReturn(snapshotDao).when(cmd).getSnapshotDao();
        mockVm();
        vmValidator = spy(new VmValidator(cmd.getVm()));
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotHavingDeviceSnapshotsAttachedToOtherVms(anyBoolean());
        doReturn(vmValidator).when(cmd).createVmValidator(any(VM.class));
        doReturn(STORAGE_POOL_ID).when(cmd).getStoragePoolId();
        mockSnapshot(SnapshotType.REGULAR);
        snapshotValidator = spy(new SnapshotsValidator());
        doReturn(snapshotValidator).when(cmd).createSnapshotValidator();
        mockConfigSizeDefaults();
        spySdValidator();
    }

    private void mockVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(STORAGE_POOL_ID);
        vm.setVdsGroupCompatibilityVersion(Version.v3_5);
        doReturn(vm).when(cmd).getVm();
    }

    private void mockSnapshot(SnapshotType snapshotType) {
        Snapshot snapshot = new Snapshot();
        snapshot.setId(cmd.getParameters().getSnapshotId());
        snapshot.setType(snapshotType);
        doReturn(snapshot).when(snapshotDao).get(snapshot.getId());
    }

    private void mockConfigSizeRequirements(int requiredSpaceBufferInGB) {
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, requiredSpaceBufferInGB);
    }

    private void mockConfigSizeDefaults() {
        int requiredSpaceBufferInGB = 5;
        mockConfigSizeRequirements(requiredSpaceBufferInGB);
    }

    private void spySdValidator() {
        Set<Guid> sdIds = new HashSet<>(Arrays.asList(STORAGE_DOMAIN_ID));
        storageDomainsValidator = spy(new MultipleStorageDomainsValidator(STORAGE_POOL_ID, sdIds));
        doReturn(storageDomainsValidator).when(cmd).getStorageDomainsValidator(any(Guid.class), anySet());
        doReturn(sdDAO).when(storageDomainsValidator).getStorageDomainDAO();
        doReturn(sdIds).when(cmd).getStorageDomainsIds();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsExistAndActive();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsHaveSpaceForClonedDisks(anyList());
    }

    @Test
    public void testValidateImageNotInTemplateTrue() {
        when(vmTemplateDAO.get(mockSourceImageAndGetId())).thenReturn(null);
        assertTrue("validation should succeed", cmd.validateImageNotInTemplate());
    }

    @Test
    public void testValidateImageNotInTemplateFalse() {
        when(vmTemplateDAO.get(mockSourceImageAndGetId())).thenReturn(new VmTemplate());
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
    public void testCanDoActionEnoughSpace() {
        prepareForVmValidatorTests();
        spySdValidator();
        cmd.getVm().setStatus(VMStatus.Up);
        doReturn(ValidationResult.VALID).when(vmValidator).vmHostCanLiveMerge();

        mockDisksList(4);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testCanDoActionNotEnoughSpace() {
        prepareForVmValidatorTests();
        spySdValidator();
        cmd.getVm().setStatus(VMStatus.Up);
        doReturn(ValidationResult.VALID).when(vmValidator).vmHostCanLiveMerge();

        List<DiskImage> imagesDisks = mockDisksList(4);
        when(storageDomainsValidator.allDomainsHaveSpaceForClonedDisks(imagesDisks)).thenReturn(
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
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
    public void testCanDoActionVmUpHostCapable() {
        prepareForVmValidatorTests();
        doReturn(ValidationResult.VALID).when(vmValidator).vmHostCanLiveMerge();
        cmd.getVm().setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testCanDoActionVmUpHostNotCapable() {
        prepareForVmValidatorTests();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_HOST_CANNOT_LIVE_MERGE))
                .when(vmValidator).vmHostCanLiveMerge();
        cmd.getVm().setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.ACTION_TYPE_FAILED_VM_HOST_CANNOT_LIVE_MERGE);
    }

    @Test
    public void testCanDoActionVmDown() {
        prepareForVmValidatorTests();
        cmd.getVm().setStatus(VMStatus.Down);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testCanDoActionVmMigrating() {
        prepareForVmValidatorTests();
        cmd.getVm().setStatus(VMStatus.MigratingTo);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP);
    }

    @Test
    public void vmHasPluggedDdeviceSnapshotsAttachedToOtherVms() {
        prepareForVmValidatorTests();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_IS_ATTACHED_TO_ANOTHER_VM)).when(vmValidator)
                .vmNotHavingDeviceSnapshotsAttachedToOtherVms(false);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_IS_ATTACHED_TO_ANOTHER_VM);
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
        when(diskImageDAO.get(image.getImageId())).thenReturn(image);

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
        doReturn(disksList).when(cmd).getSnapshotsDummiesForStorageAllocations();
        return disksList;
    }
}
