package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

/** A test class for the {@link MultipleStorageDomainsValidator} class. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MultipleStorageDomainsValidatorTest {

    private static final int CRITICAL_SPACE_THRESHOLD = 5;

    @Mock
    private StorageDomainDao dao;

    private Guid sdId1;
    private Guid sdId2;
    private Guid sdId3;

    private StorageDomain domain1;
    private StorageDomain domain2;
    private StorageDomain domain3;

    private MultipleStorageDomainsValidator validator;

    private static final int NUM_DISKS = 3;
    private static final int NUM_DOMAINS = 3;

    @BeforeEach
    public void setUp() {
        Guid spId = Guid.newGuid();

        sdId1 = Guid.newGuid();
        sdId2 = Guid.newGuid();
        sdId3 = Guid.newGuid();

        domain1 = new StorageDomain();
        domain1.setId(sdId1);
        domain1.setStoragePoolId(spId);

        domain2 = new StorageDomain();
        domain2.setId(sdId2);
        domain2.setStoragePoolId(spId);

        domain3 = new StorageDomain();
        domain3.setId(sdId3);
        domain3.setStoragePoolId(spId);

        when(dao.getForStoragePool(sdId1, spId)).thenReturn(domain1);
        when(dao.getForStoragePool(sdId2, spId)).thenReturn(domain2);
        when(dao.getForStoragePool(sdId3, spId)).thenReturn(domain3);

        validator = spy(new MultipleStorageDomainsValidator(spId, Arrays.asList(sdId1, sdId2, sdId3)));
        doReturn(dao).when(validator).getStorageDomainDao();
    }

    @Test
    public void testAllDomainsExistAndActiveAllActive() {
        domain1.setStatus(StorageDomainStatus.Active);
        domain2.setStatus(StorageDomainStatus.Active);
        domain3.setStatus(StorageDomainStatus.Active);
        assertTrue(validator.allDomainsExistAndActive().isValid(), "Both domains should be active");
    }

    @Test
    public void testAllDomainsExistAndActiveOneInactive() {
        domain1.setStatus(StorageDomainStatus.Active);
        domain2.setStatus(StorageDomainStatus.Inactive);
        domain3.setStatus(StorageDomainStatus.Active);
        ValidationResult result = validator.allDomainsExistAndActive();
        assertThat("One domain should not be active", result, failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2));
    }

    @Test
    public void testAllDomainsNotBackupDomainsAllOK() {
        assertThat("None of the domains should be in backup mode", validator.allDomainsNotBackupDomains(), isValid());
    }

    @Test
    public void testAllDomainsNotBackupDomainsOneBackup() {
        domain2.setBackup(true);
        ValidationResult result = validator.allDomainsNotBackupDomains();
        assertThat("One domain is in backup mode", result, failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_DISKS_ON_BACKUP_STORAGE));
    }

    @Test
    public void testAllDomainsWithinThresholdAllOk() {
        domain1.getStorageDynamicData().setAvailableDiskSize(CRITICAL_SPACE_THRESHOLD +1);
        domain2.getStorageDynamicData().setAvailableDiskSize(CRITICAL_SPACE_THRESHOLD);
        domain1.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        domain2.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        assertTrue(validator.allDomainsWithinThresholds().isValid(), "Both domains should be within space threshold");
    }

    @Test
    public void testAllDomainsWithinThresholdsOneLacking() {
        domain1.getStorageDynamicData().setAvailableDiskSize(CRITICAL_SPACE_THRESHOLD + 1);
        domain2.getStorageDynamicData().setAvailableDiskSize(CRITICAL_SPACE_THRESHOLD - 1);
        domain1.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        domain2.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        ValidationResult result = validator.allDomainsWithinThresholds();
        assertThat("domain2 should not be within thresholds", result, failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    @Test
    public void testAllDomainsHaveSpaceForNewDisksSuccess(){
        List<Guid> sdIds = Arrays.asList(sdId1, sdId2);
        List<DiskImage> disksList = generateDisksList(NUM_DISKS, sdIds);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        assertTrue(validator.allDomainsHaveSpaceForNewDisks(disksList).isValid());
        verify(storageDomainValidator, times(NUM_DOMAINS)).hasSpaceForNewDisks(any());
    }

    @Test
    public void testAllDomainsHaveSpaceForNewDisksFail(){
        List<Guid> sdIds = Arrays.asList(sdId1, sdId2);
        List<DiskImage> disksList = generateDisksList(NUM_DISKS, sdIds);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForNewDisks(any());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        ValidationResult result = validator.allDomainsHaveSpaceForNewDisks(disksList);
        assertThat(result, failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    @Test
    public void testAllDomainsHaveSpaceForClonedDisksSuccess(){
        List<Guid> sdIds = Arrays.asList(sdId1, sdId2);
        List<DiskImage> disksList = generateDisksList(NUM_DISKS, sdIds);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        assertTrue(validator.allDomainsHaveSpaceForClonedDisks(disksList).isValid());
        verify(storageDomainValidator, times(NUM_DOMAINS)).hasSpaceForClonedDisks(any());
    }

    @Test
    public void testAllDomainsHaveSpaceForClonedDisksFail(){
        List<Guid> sdIds = Arrays.asList(sdId1, sdId2);
        List<DiskImage> disksList = generateDisksList(NUM_DISKS, sdIds);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(any());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        ValidationResult result = validator.allDomainsHaveSpaceForClonedDisks(disksList);
        assertThat(result, failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    @Test
    public void testAllDomainsHaveSpaceForAllDisksSuccess(){
        List<Guid> sdIdsForNew = Arrays.asList(sdId1, sdId2);
        List<Guid> sdIdsForCloned = Arrays.asList(sdId2, sdId3);
        List<DiskImage> disksListForNew = generateDisksList(NUM_DISKS, sdIdsForNew);
        List<DiskImage> disksListForCloned = generateDisksList(NUM_DISKS, sdIdsForCloned);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        assertTrue(validator.allDomainsHaveSpaceForAllDisks(disksListForNew, disksListForCloned).isValid());
        verify(storageDomainValidator, times(NUM_DOMAINS)).hasSpaceForAllDisks(any(), any());
    }

    @Test
    public void testAllDomainsHaveSpaceForAllDisksFail(){
        List<Guid> sdIdsForNew = Arrays.asList(sdId1, sdId2);
        List<Guid> sdIdsForCloned = Arrays.asList(sdId2, sdId3);
        List<DiskImage> disksListForNew = generateDisksList(NUM_DISKS, sdIdsForNew);
        List<DiskImage> disksListForCloned = generateDisksList(NUM_DISKS, sdIdsForCloned);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
               when(storageDomainValidator).hasSpaceForAllDisks(any(), any());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        ValidationResult result = validator.allDomainsHaveSpaceForAllDisks(disksListForNew, disksListForCloned);
        assertThat(result, failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    @Test
    public void testAllDomainsHaveSpaceForMergeSuccess(){
        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        List<SubchainInfo> subchain = createSubchain(Collections.singletonList(sdId1));
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        assertTrue(validator.allDomainsHaveSpaceForMerge(subchain, ActionType.RemoveSnapshotSingleDiskLive).isValid());
        verify(storageDomainValidator, times(NUM_DOMAINS)).hasSpaceForMerge(any(), any());
    }

    @Test
    public void testAllDomainsHaveSpaceForMergeFail(){
        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        List<SubchainInfo> subchain = createSubchain(Collections.singletonList(sdId1));
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForMerge(subchain, ActionType.RemoveSnapshotSingleDiskLive);
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        ValidationResult result = validator.allDomainsHaveSpaceForMerge(subchain, ActionType.RemoveSnapshotSingleDiskLive);
        assertThat(result, failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    @Test
    public void testAllDomainsHaveSpaceForMergeBrokenSubchainFail(){
        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        List<SubchainInfo> subchain = new ArrayList<>();
        doReturn(new ValidationResult(EngineMessage.ERROR_CANNOT_REMOVE_SNAPSHOT_ILLEGAL_IMAGE)).
                when(storageDomainValidator).hasSpaceForMerge(subchain, ActionType.RemoveSnapshotSingleDiskLive);
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any());

        ValidationResult result = validator.allDomainsHaveSpaceForMerge(subchain, ActionType.RemoveSnapshotSingleDiskLive);
        assertThat(result, failsWith(EngineMessage.ERROR_CANNOT_REMOVE_SNAPSHOT_ILLEGAL_IMAGE));
    }

    private List<SubchainInfo> createSubchain(List<Guid> sdIds) {
        Guid imageGroup = Guid.newGuid();
        DiskImage base = new DiskImage();
        base.setId(imageGroup);
        base.setImageId(Guid.newGuid());
        base.setStorageIds(sdIds);

        DiskImage top = new DiskImage();
        top.setId(imageGroup);
        top.setImageId(Guid.newGuid());
        top.setParentId(base.getImageId());
        top.setStorageIds(sdIds);

        SubchainInfo subchainInfo = new SubchainInfo(sdIds.get(0), base, top);

        return Collections.singletonList(subchainInfo);
    }

    private List<DiskImage> generateDisksList(int size, List<Guid> sdIds) {
        List<DiskImage> disksList = new ArrayList<>();
        ArrayList<Guid> _sdIds = new ArrayList<>(sdIds);
        for (int i = 0; i < size; ++i) {
            DiskImage diskImage = new DiskImage();
            diskImage.setImageId(Guid.newGuid());
            diskImage.setId(Guid.newGuid());
            diskImage.setStorageIds(_sdIds);
            disksList.add(diskImage);
        }
        return disksList;
    }
}
