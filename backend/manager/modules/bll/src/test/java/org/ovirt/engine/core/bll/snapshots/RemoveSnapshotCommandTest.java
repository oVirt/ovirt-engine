package org.ovirt.engine.core.bll.snapshots;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

/** A test case for the {@link RemoveSnapshotCommand} class. */
@MockitoSettings(strictness = Strictness.LENIENT)
public class RemoveSnapshotCommandTest extends BaseCommandTest {

    /** The command to test */
    @Spy
    @InjectMocks
    private RemoveSnapshotCommand<RemoveSnapshotParameters> cmd =
            new RemoveSnapshotCommand<>(new RemoveSnapshotParameters(Guid.newGuid(), Guid.newGuid()), null);

    @Mock
    private VmTemplateDao vmTemplateDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private StoragePoolDao spDao;

    @Mock
    private SnapshotDao snapshotDao;

    @Spy
    private SnapshotsValidator snapshotValidator;

    private MultipleStorageDomainsValidator storageDomainsValidator;

    private static final Guid STORAGE_DOMAIN_ID = Guid.newGuid();
    private static final Guid STORAGE_POOL_ID = Guid.newGuid();

    @BeforeEach
    public void setUp() {
        mockVm();
        mockStorageDomain();
        VmValidator vmValidator = spy(new VmValidator(cmd.getVm()));
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotHavingDeviceSnapshotsAttachedToOtherVms(anyBoolean());
        doReturn(vmValidator).when(cmd).createVmValidator(any());
        doReturn(STORAGE_POOL_ID).when(cmd).getStoragePoolId();
        mockSnapshot(SnapshotType.REGULAR);
        spySdValidator();
    }

    private void mockStorageDomain(){
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setStorageType(StorageType.NFS);
        doReturn(storageDomain).when(cmd).getStorageDomain();
        doReturn(storageDomain).when(cmd).getStorageDomain();
    }

    private void mockVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(STORAGE_POOL_ID);
        doReturn(vm).when(cmd).getVm();
    }

    private void mockCluster(Version compatabilityVersion) {
        Cluster cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setCompatibilityVersion(compatabilityVersion);
        doReturn(cluster).when(cmd).getCluster();
    }

    private void mockSnapshot(SnapshotType snapshotType) {
        Snapshot snapshot = new Snapshot();
        snapshot.setId(cmd.getParameters().getSnapshotId());
        snapshot.setType(snapshotType);
        doReturn(snapshot).when(snapshotDao).get(snapshot.getId());
    }

    private void spySdValidator() {
        Set<Guid> sdIds = new HashSet<>(Collections.singletonList(STORAGE_DOMAIN_ID));
        storageDomainsValidator = spy(new MultipleStorageDomainsValidator(STORAGE_POOL_ID, sdIds));
        doReturn(storageDomainsValidator).when(cmd).getStorageDomainsValidator(any(), any());
        doReturn(sdIds).when(cmd).getStorageDomainsIds();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsExistAndActive();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).allDomainsHaveSpaceForMerge(any(), any());
        doReturn(ValidationResult.VALID).when(storageDomainsValidator).isSupportedByManagedBlockStorageDomains(ActionType.Unknown);
    }

    @Test
    public void testValidateImageNotInTemplateTrue() {
        when(vmTemplateDao.get(mockSourceImageAndGetId())).thenReturn(null);
        assertTrue(cmd.validateImageNotInTemplate(), "validation should succeed");
    }

    @Test
    public void testValidateImageNotInTemplateFalse() {
        when(vmTemplateDao.get(mockSourceImageAndGetId())).thenReturn(new VmTemplate());
        assertFalse(cmd.validateImageNotInTemplate(), "validation should succeed");
    }

    @Test
    public void testValidateSnapshotNotActiveTrue() {
        mockSnapshot(SnapshotType.REGULAR);
        assertTrue(cmd.validateSnapshotType(), "validation should succeed");
    }

    @Test
    public void testValidateSnapshotNotActiveFalse() {
        mockSnapshot(SnapshotType.ACTIVE);
        assertFalse(cmd.validateSnapshotType(), "validation should fail");
    }

    @Test
    public void testValidateEnoughSpace() {
        prepareForVmValidatorTests();
        spySdValidator();
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmSnapshotDisksNotDuringMerge(any(), any());
        cmd.getVm().setStatus(VMStatus.Up);
        List<DiskImage> parentSnapshots = mockDisksList(2);
        doReturn(parentSnapshots).when(cmd).getSourceImages();

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateNotEnoughSpace() {
        prepareForVmValidatorTests();
        spySdValidator();
        cmd.getVm().setStatus(VMStatus.Up);
        mockDisksList(2);

        doReturn(ValidationResult.VALID).when(snapshotValidator).vmSnapshotDisksNotDuringMerge(any(), any());
        when(storageDomainsValidator.allDomainsHaveSpaceForMerge(any(), any()))
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }


    private void prepareForVmValidatorTests() {
        StoragePool sp = new StoragePool();
        sp.setId(STORAGE_POOL_ID);
        sp.setStatus(StoragePoolStatus.Up);

        cmd.setSnapshotName("someSnapshot");
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmNotDuringSnapshot(any());
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmNotInPreview(any());
        doReturn(ValidationResult.VALID).when(snapshotValidator).snapshotExists(any(), any());
        doReturn(true).when(cmd).validateImages();
        doReturn(sp).when(spDao).get(STORAGE_POOL_ID);
        doReturn(Collections.emptyList()).when(cmd).getSourceImages();
    }

    @Test
    public void testValidateVmUpHostCapable() {
        prepareForVmValidatorTests();
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmSnapshotDisksNotDuringMerge(any(), any());
        cmd.getVm().setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateVmDown() {
        prepareForVmValidatorTests();
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmSnapshotDisksNotDuringMerge(any(), any());
        cmd.getVm().setStatus(VMStatus.Down);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateVmMigrating() {
        prepareForVmValidatorTests();
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmSnapshotDisksNotDuringMerge(any(), any());
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
        ArrayList<Guid> sdIds = new ArrayList<>(Collections.singletonList(storageDomainId));
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
        return disksList;
    }
}
