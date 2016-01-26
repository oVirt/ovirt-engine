package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

/** A test class for the {@link MultipleStorageDomainsValidator} class. */
@RunWith(MockitoJUnitRunner.class)
public class MultipleStorageDomainsValidatorTest {

    private static final int CRITICAL_SPACE_THRESHOLD = 5;

    @Mock
    private StorageDomainDao dao;

    private Guid spId;

    private Guid sdId1;
    private Guid sdId2;
    private Guid sdId3;

    private StorageDomain domain1;
    private StorageDomain domain2;

    private MultipleStorageDomainsValidator validator;

    private static final int NUM_DISKS = 3;
    private static final int NUM_DOMAINS = 2;

    @Before
    public void setUp() {
        spId = Guid.newGuid();

        sdId1 = Guid.newGuid();
        sdId2 = Guid.newGuid();
        sdId3 = Guid.newGuid();

        domain1 = new StorageDomain();
        domain1.setId(sdId1);

        domain2 = new StorageDomain();
        domain2.setId(sdId2);

        when(dao.getForStoragePool(sdId1, spId)).thenReturn(domain1);
        when(dao.getForStoragePool(sdId2, spId)).thenReturn(domain2);

        validator = spy(new MultipleStorageDomainsValidator(spId, Arrays.asList(sdId1, sdId2)));
        doReturn(dao).when(validator).getStorageDomainDao();
    }

    @Test
    public void testAllDomainsExistAndActiveAllActive() {
        domain1.setStatus(StorageDomainStatus.Active);
        domain2.setStatus(StorageDomainStatus.Active);
        assertTrue("Both domains should be active", validator.allDomainsExistAndActive().isValid());
    }

    @Test
    public void testAllDomainsExistAndActiveOneInactive() {
        domain1.setStatus(StorageDomainStatus.Active);
        domain2.setStatus(StorageDomainStatus.Inactive);
        ValidationResult result = validator.allDomainsExistAndActive();
        assertFalse("One domain should not be active", result.isValid());
        assertEquals("Wrong validation error",
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                result.getMessage());
    }

    @Test
    public void testAllDomainsWithinThresholdAllOk() {
        domain1.getStorageDynamicData().setAvailableDiskSize(CRITICAL_SPACE_THRESHOLD +1);
        domain2.getStorageDynamicData().setAvailableDiskSize(CRITICAL_SPACE_THRESHOLD);
        domain1.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        domain2.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        assertTrue("Both domains should be within space threshold", validator.allDomainsWithinThresholds().isValid());
    }

    @Test
    public void testAllDomainsWithinThresholdsOneLacking() {
        domain1.getStorageDynamicData().setAvailableDiskSize(CRITICAL_SPACE_THRESHOLD + 1);
        domain2.getStorageDynamicData().setAvailableDiskSize(CRITICAL_SPACE_THRESHOLD - 1);
        domain1.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        domain2.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        ValidationResult result = validator.allDomainsWithinThresholds();
        assertFalse("domain2 should not be within thresholds", result.isValid());
        assertEquals("Wrong validation error",
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                result.getMessage());
    }

    @Test
    public void testAllDomainsHaveSpaceForNewDisksSuccess(){
        List<Guid> sdIds = Arrays.asList(sdId1, sdId2);
        List<DiskImage> disksList = generateDisksList(NUM_DISKS, sdIds);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForNewDisks(anyList());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any(Map.Entry.class));

        assertTrue(validator.allDomainsHaveSpaceForNewDisks(disksList).isValid());
        verify(storageDomainValidator, times(NUM_DOMAINS)).hasSpaceForNewDisks(anyList());
    }

    @Test
    public void testAllDomainsHaveSpaceForNewDisksFail(){
        List<Guid> sdIds = Arrays.asList(sdId1, sdId2);
        List<DiskImage> disksList = generateDisksList(NUM_DISKS, sdIds);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForNewDisks(anyList());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any(Map.Entry.class));

        ValidationResult result = validator.allDomainsHaveSpaceForNewDisks(disksList);
        assertFalse(result.isValid());
        assertEquals("Wrong validation error",
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                result.getMessage());
    }

    @Test
    public void testAllDomainsHaveSpaceForClonedDisksSuccess(){
        List<Guid> sdIds = Arrays.asList(sdId1, sdId2);
        List<DiskImage> disksList = generateDisksList(NUM_DISKS, sdIds);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any(Map.Entry.class));

        assertTrue(validator.allDomainsHaveSpaceForClonedDisks(disksList).isValid());
        verify(storageDomainValidator, times(NUM_DOMAINS)).hasSpaceForClonedDisks(anyList());
    }

    @Test
    public void testAllDomainsHaveSpaceForClonedDisksFail(){
        List<Guid> sdIds = Arrays.asList(sdId1, sdId2);
        List<DiskImage> disksList = generateDisksList(NUM_DISKS, sdIds);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any(Map.Entry.class));

        ValidationResult result = validator.allDomainsHaveSpaceForClonedDisks(disksList);
        assertFalse(result.isValid());
        assertEquals("Wrong validation error",
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                result.getMessage());
    }

    @Test
    public void testAllDomainsHaveSpaceForAllDisksSuccess(){
        List<Guid> sdIdsForNew = Arrays.asList(sdId1, sdId2);
        List<Guid> sdIdsForCloned = Arrays.asList(sdId2, sdId3);
        List<DiskImage> disksListForNew = generateDisksList(NUM_DISKS, sdIdsForNew);
        List<DiskImage> disksListForCloned = generateDisksList(NUM_DISKS, sdIdsForCloned);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForAllDisks(anyList(), anyList());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any(Map.Entry.class));

        assertTrue(validator.allDomainsHaveSpaceForAllDisks(disksListForNew, disksListForCloned).isValid());
        verify(storageDomainValidator, times(NUM_DOMAINS)).hasSpaceForAllDisks(anyList(), anyList());
    }

    @Test
    public void testAllDomainsHaveSpaceForAllDisksFail(){
        List<Guid> sdIdsForNew = Arrays.asList(sdId1, sdId2);
        List<Guid> sdIdsForCloned = Arrays.asList(sdId2, sdId3);
        List<DiskImage> disksListForNew = generateDisksList(NUM_DISKS, sdIdsForNew);
        List<DiskImage> disksListForCloned = generateDisksList(NUM_DISKS, sdIdsForCloned);

        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
               when(storageDomainValidator).hasSpaceForAllDisks(anyList(), anyList());
        doReturn(storageDomainValidator).when(validator).getStorageDomainValidator(any(Map.Entry.class));

        ValidationResult result = validator.allDomainsHaveSpaceForAllDisks(disksListForNew, disksListForCloned);
        assertFalse(result.isValid());
        assertEquals("Wrong validation error",
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                result.getMessage());
    }

    private List<DiskImage> generateDisksList(int size, List<Guid> sdIds) {
        List<DiskImage> disksList = new ArrayList<>();
        ArrayList<Guid> _sdIds = new ArrayList<>(sdIds);
        for (int i = 0; i < size; ++i) {
            DiskImage diskImage = new DiskImage();
            diskImage.setImageId(Guid.newGuid());
            diskImage.setStorageIds(_sdIds);
            disksList.add(diskImage);
        }
        return disksList;
    }
}
