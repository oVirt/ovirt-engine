package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.MockConfigExtension;

/**
 * A test case for the {@link StorageDomainValidator} class.
 * The hasSpaceForClonedDisk() and hasSpaceForNewDisk() methods are covered separately in
 * {@link StorageDomainValidatorFreeSpaceTest}.
 */
@ExtendWith({MockitoExtension.class, MockConfigExtension.class, InjectorExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class StorageDomainValidatorTest {
    private StorageDomain domain;
    private StorageDomainValidator validator;
    private static final int CRITICAL_SPACE_THRESHOLD = 5;

    @Mock
    private VmHandler vmHandler;

    @Mock
    private VmDao vmDao;

    @Mock
    private VmDynamicDao vmDynamicDao;

    @Mock
    @InjectedMock
    public BlockStorageDiscardFunctionalityHelper discardFunctionalityHelper;

    @BeforeEach
    public void setUp() {
        domain = new StorageDomain();
        validator = spy(new StorageDomainValidator(domain));
        doReturn(vmDao).when(validator).getVmDao();
        doReturn(vmDynamicDao).when(validator).getVmDynamicDao();
    }

    @Test
    public void testIsDomainExistAndActiveDomainNotExists() {
        validator = new StorageDomainValidator(null);
        assertThat("Wrong failure for null domain",
                validator.isDomainExistAndActive(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST));
    }

    @Test
    public void testIsBackupDomain() {
        domain.setBackup(false);
        assertThat("Backup domain is not backup, should be valid.", validator.isNotBackupDomain(), isValid());
    }

    @Test
    public void testInvalidIsBackupDomain() {
        domain.setBackup(true);
        assertThat("Backup domain is backup, should be invalid.",
                validator.isNotBackupDomain(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_DISKS_ON_BACKUP_STORAGE));
    }

    @Test
    public void testIsDomainExistAndActiveDomainNotUp() {
        domain.setStatus(StorageDomainStatus.Inactive);
        assertThat("Wrong failure for inactive domain",
                validator.isDomainExistAndActive(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2));
    }

    @Test
    public void testIsDomainExistAndActiveDomainUp() {
        domain.setStatus(StorageDomainStatus.Active);
        assertThat("domain should be up", validator.isDomainExistAndActive(), isValid());
    }

    @Test
    public void testDomainWithNotEnoughSpace() {
        validator = new StorageDomainValidator(mockStorageDomain(3, 756, StorageType.NFS));
        assertThat("Wrong failure for not enough space",
                validator.isDomainWithinThresholds(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    @Test
    public void testDomainWithEnoughSpace() {
        validator = new StorageDomainValidator(mockStorageDomain(6, 756, StorageType.NFS));
        assertThat("Domain should have more space then threshold", validator.isDomainWithinThresholds(), isValid());
    }

    @Test
    public void testIsDataDomainValid() {
        domain.setStorageDomainType(StorageDomainType.Data);
        assertThat(validator.isDataDomain(), isValid());
    }

    @Test
    public void testIsDataDomainFails() {
        domain.setStorageDomainType(StorageDomainType.ISO);
        assertThat(validator.isDataDomain(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_ACTION_IS_SUPPORTED_ONLY_FOR_DATA_DOMAINS));
    }

    @Test
    public void discardAfterDeleteDisabled() {
        assertDiscardAfterDeleteUpdate(false, StorageType.ISCSI, false, ValidationResult.VALID);
    }

    @Test
    public void discardAfterDeleteNotSupportedByFileDomains() {
        assertDiscardAfterDeleteUpdate(true, StorageType.NFS, false, new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_DISCARD_AFTER_DELETE_SUPPORTED_ONLY_BY_BLOCK_DOMAINS));
    }

    @Test
    public void discardAfterDeleteNotSupportedByUnderlyingStorage() {
        ValidationResult result = new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_DISCARD_AFTER_DELETE_NOT_SUPPORTED_BY_UNDERLYING_STORAGE,
                String.format("$storageDomainName %s", domain.getName()));

        assertDiscardAfterDeleteUpdate(true, StorageType.ISCSI, false, result);
    }

    @Test
    public void discardAfterDeleteLegal() {
        assertDiscardAfterDeleteUpdate(true, StorageType.ISCSI, true, ValidationResult.VALID);
    }

    @Test
    public void discardAfterDeleteLegalForExistingStorageDomainPredicateTrue() {
        domain.setSupportsDiscard(true);
        assertTrue(validator.discardAfterDeleteLegalForExistingStorageDomainPredicate());
    }

    @Test
    public void discardAfterDeleteLegalForExistingStorageDomainPredicateFalse() {
        domain.setSupportsDiscard(false);
        assertFalse(validator.discardAfterDeleteLegalForExistingStorageDomainPredicate());
    }

    @Test
    public void discardAfterDeleteLegalForExistingStorageDomainPredicateNull() {
        domain.setSupportsDiscard(null);
        assertFalse(validator.discardAfterDeleteLegalForExistingStorageDomainPredicate());
    }

    @Test
    public void getDiscardAfterDeleteLegalForNewBlockStorageDomainPredicateWithSupportiveLuns() {
        assertGetDiscardAfterDeleteLegalForNewBlockStorageDomainPredicate(true);
    }

    @Test
    public void getDiscardAfterDeleteLegalForNewBlockStorageDomainPredicateWithUnsupportiveLuns() {
        assertGetDiscardAfterDeleteLegalForNewBlockStorageDomainPredicate(false);
    }

    @Test
    public void validRunningVmsOrVmLeasesForBackupDomain() {
        QueryReturnValue ret = new QueryReturnValue();
        ret.setReturnValue(new ArrayList<VmBase>());
        ret.setSucceeded(true);
        doReturn(ret).when(validator).getEntitiesWithLeaseIdForStorageDomain(any());
        assertThat(validator.isRunningVmsOrVmLeasesForBackupDomain(vmHandler), isValid());
    }

    @Test
    public void invalidRunningVmsAndVmLeasesForBackupDomain() {
        QueryReturnValue ret = new QueryReturnValue();
        List<VmBase> vmLeases = new ArrayList<>();
        VM vmWithLease = new VM();
        vmWithLease.setName("firstVM");
        vmLeases.add(vmWithLease.getStaticData());
        ret.setReturnValue(vmLeases);
        ret.setSucceeded(true);

        // VM1
        VM vm1 = new VM();
        vm1.setName("firstVM");
        Map<Guid, Disk> attachedDisksForVm1 = new HashMap<>();
        DiskImage diskVm1 = new DiskImage();
        diskVm1.setStorageIds(new ArrayList<>(Collections.singletonList(domain.getId())));
        diskVm1.setPlugged(true);
        attachedDisksForVm1.put(Guid.newGuid(), diskVm1);
        vm1.setDiskMap(attachedDisksForVm1);

        // VM2
        VM vm2 = new VM();
        vm2.setName("secondVM");
        Map<Guid, Disk> attachedDisksForVm2 = new HashMap<>();
        DiskImage diskVm2 = new DiskImage();
        diskVm2.setStorageIds(new ArrayList<>(Collections.singletonList(domain.getId())));
        diskVm2.setPlugged(true);
        attachedDisksForVm2.put(Guid.newGuid(), diskVm2);
        vm2.setDiskMap(attachedDisksForVm2);

        List<VM> runningVMs = new ArrayList<>();
        runningVMs.add(vm1);
        runningVMs.add(vm2);
        when(vmDao.getAllActiveForStorageDomain(any())).thenReturn(runningVMs);
        doReturn(ret).when(validator).getEntitiesWithLeaseIdForStorageDomain(any());
        assertThat(validator.isRunningVmsOrVmLeasesForBackupDomain(vmHandler),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_RUNNING_VM_OR_VM_LEASES_PRESENT_ON_STORAGE_DOMAIN));
    }

    @Test
    public void invalidVmLeasesQueryForBackupDomain() {
        QueryReturnValue ret = new QueryReturnValue();
        ret.setSucceeded(false);
        doReturn(ret).when(validator).getEntitiesWithLeaseIdForStorageDomain(any());
        assertThat(validator.isRunningVmsOrVmLeasesForBackupDomain(vmHandler),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_RETRIEVE_VMS_FOR_WITH_LEASES));
    }

    @Test
    public void invalidVmLeasesForBackupDomain() {
        QueryReturnValue ret = new QueryReturnValue();
        List<VmBase> vmLeases = new ArrayList<>();
        VM vm1 = new VM();
        vm1.setName("firstVM");
        vm1.setStatus(VMStatus.PoweringUp);
        vmLeases.add(vm1.getStaticData());
        ret.setReturnValue(vmLeases);
        ret.setSucceeded(true);
        doReturn(ret).when(validator).getEntitiesWithLeaseIdForStorageDomain(any());
        when(vmDynamicDao.get(vm1.getId())).thenReturn(vm1.getDynamicData());
        assertThat(validator.isRunningVmsOrVmLeasesForBackupDomain(vmHandler),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_RUNNING_VM_OR_VM_LEASES_PRESENT_ON_STORAGE_DOMAIN));
    }

    @Test
    public void validVmLeasesForBackupDomain() {
        QueryReturnValue ret = new QueryReturnValue();
        List<VmBase> vmLeases = new ArrayList<>();
        VM vm1 = new VM();
        vm1.setName("firstVM");
        vm1.setStatus(VMStatus.Down);
        vmLeases.add(vm1.getStaticData());
        ret.setReturnValue(vmLeases);
        ret.setSucceeded(true);
        doReturn(ret).when(validator).getEntitiesWithLeaseIdForStorageDomain(any());
        assertThat(validator.isRunningVmsOrVmLeasesForBackupDomain(vmHandler), isValid());
    }

    @Test
    public void invalidRunningVmsForBackupDomain() {
        // VM1
        VM vm1 = new VM();
        vm1.setName("firstVM");
        Map<Guid, Disk> attachedDisksForVm1 = new HashMap<>();
        DiskImage diskVm1 = new DiskImage();
        diskVm1.setStorageIds(new ArrayList<>(Collections.singletonList(domain.getId())));
        diskVm1.setPlugged(true);
        attachedDisksForVm1.put(Guid.newGuid(), diskVm1);
        vm1.setDiskMap(attachedDisksForVm1);

        // VM2
        VM vm2 = new VM();
        vm2.setName("secondVM");
        Map<Guid, Disk> attachedDisksForVm2 = new HashMap<>();
        DiskImage diskVm2 = new DiskImage();
        diskVm2.setStorageIds(new ArrayList<>(Collections.singletonList(domain.getId())));
        diskVm2.setPlugged(true);
        attachedDisksForVm2.put(Guid.newGuid(), diskVm2);
        vm2.setDiskMap(attachedDisksForVm2);

        List<VM> runningVMs = new ArrayList<>();
        runningVMs.add(vm1);
        runningVMs.add(vm2);
        when(vmDao.getAllActiveForStorageDomain(any())).thenReturn(runningVMs);
        QueryReturnValue ret = new QueryReturnValue();
        ret.setReturnValue(new ArrayList<VmBase>());
        ret.setSucceeded(true);
        doReturn(ret).when(validator).getEntitiesWithLeaseIdForStorageDomain(any());
        assertThat(validator.isRunningVmsOrVmLeasesForBackupDomain(vmHandler),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_RUNNING_VM_OR_VM_LEASES_PRESENT_ON_STORAGE_DOMAIN));
    }

    @Test
    public void validRunningVmsWithUnpluggedDisksForBackupDomain() {
        // VM1
        VM vm1 = new VM();
        vm1.setName("firstVM");
        Map<Guid, Disk> attachedDisksForVm1 = new HashMap<>();
        DiskImage diskVm1 = new DiskImage();
        diskVm1.setStorageIds(new ArrayList<>(Collections.singletonList(domain.getId())));
        diskVm1.setPlugged(false);
        attachedDisksForVm1.put(Guid.newGuid(), diskVm1);
        vm1.setDiskMap(attachedDisksForVm1);

        List<VM> runningVMs = new ArrayList<>();
        runningVMs.add(vm1);
        when(vmDao.getAllActiveForStorageDomain(any())).thenReturn(runningVMs);
        QueryReturnValue ret = new QueryReturnValue();
        ret.setReturnValue(new ArrayList<VmBase>());
        ret.setSucceeded(true);
        doReturn(ret).when(validator).getEntitiesWithLeaseIdForStorageDomain(any());
        assertThat(validator.isRunningVmsOrVmLeasesForBackupDomain(vmHandler), isValid());
    }

    private static StorageDomain mockStorageDomain(int availableSize, int usedSize, StorageType storageType) {
        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(availableSize);
        sd.setUsedDiskSize(usedSize);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStorageType(storageType);
        sd.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        return sd;
    }

    private void assertDiscardAfterDeleteUpdate(boolean discardAfterDelete, StorageType storageType,
            boolean supportsDiscard, ValidationResult result) {
        domain.setDiscardAfterDelete(discardAfterDelete);
        domain.setStorageType(storageType);
        domain.setSupportsDiscard(supportsDiscard);
        assertEquals(validator.isDiscardAfterDeleteLegal(() -> supportsDiscard), result);
    }

    private void assertGetDiscardAfterDeleteLegalForNewBlockStorageDomainPredicate(boolean allLunsSupportDiscard) {
        when(discardFunctionalityHelper.allLunsSupportDiscard(any())).thenReturn(allLunsSupportDiscard);
        assertEquals(
                validator.getDiscardAfterDeleteLegalForNewBlockStorageDomainPredicate(Collections.emptyList()).get(),
                allLunsSupportDiscard);
    }
}
