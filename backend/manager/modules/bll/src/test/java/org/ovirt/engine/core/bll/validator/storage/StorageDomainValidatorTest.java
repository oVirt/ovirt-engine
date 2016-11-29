package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.di.InjectorRule;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * A test case for the {@link org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator} class.
 * The hasSpaceForClonedDisk() and hasSpaceForNewDisk() methods are covered separately in
 * {@link org.ovirt.engine.core.bll.validator.storage.StorageDomainValidatorFreeSpaceTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StorageDomainValidatorTest {
    private StorageDomain domain;
    private StorageDomainValidator validator;
    private static final int CRITICAL_SPACE_THRESHOLD = 5;

    @ClassRule
    public static InjectorRule injectorRule = new InjectorRule();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DiscardAfterDeleteSupported, Version.v4_0, false));

    @Mock
    private BlockStorageDiscardFunctionalityHelper discardFunctionalityHelper;

    @Before
    public void setUp() {
        domain = new StorageDomain();
        validator = new StorageDomainValidator(domain);
    }

    @Test
    public void testIsDomainExistAndActiveDomainNotExists() {
        validator = new StorageDomainValidator(null);
        assertThat("Wrong failure for null domain",
                validator.isDomainExistAndActive(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST));
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
    public void discardAfterDeleteSupportedByDcVersion() {
        domain.setDiscardAfterDelete(true);
        assertThat(validator.isDiscardAfterDeleteSupportedByDcVersion(Version.v4_1), isValid());
    }

    @Test
    public void discardAfterDeleteNotSupportedByDcVersion() {
        domain.setDiscardAfterDelete(true);
        assertThat(validator.isDiscardAfterDeleteSupportedByDcVersion(Version.v4_0),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISCARD_AFTER_DELETE_NOT_SUPPORTED_BY_DC_VERSION));
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
        injectorRule.bind(BlockStorageDiscardFunctionalityHelper.class, discardFunctionalityHelper);
        when(discardFunctionalityHelper.allLunsSupportDiscard(anyCollectionOf(LUNs.class)))
                .thenReturn(allLunsSupportDiscard);
        assertEquals(
                validator.getDiscardAfterDeleteLegalForNewBlockStorageDomainPredicate(Collections.emptyList()).get(),
                allLunsSupportDiscard);
    }
}
