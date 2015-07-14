package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class StorageDomainToPoolRelationValidatorTest {
    private static final Version UNSUPPORTED_VERSION = Version.v3_0;
    private static final Version SUPPORTED_VERSION = Version.v3_3;

    private StorageDomain storageDomain;
    private StoragePool storagePool;
    private StorageDomainToPoolRelationValidator validator;

    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Before
    public void setUp() throws Exception {
        // Create the storage domain.
        storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        storageDomain.setStorageFormat(StorageFormatType.V3);
        storageDomain.setStorageType(StorageType.NFS);

        // Create the storage pool.
        storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());
        storagePool.setCompatibilityVersion(Version.v3_5);

        when(storagePoolIsoMapDao.getAllForStorage(storageDomain.getId())).thenReturn(Collections.<StoragePoolIsoMap>emptyList());
        spyValidator();
    }

    private void spyValidator() {
        // Create the spied validators.
        validator = spy(new StorageDomainToPoolRelationValidator(storageDomain.getStorageStaticData(), storagePool));

        doReturn(storagePoolIsoMapDao).when(validator).getStoragePoolIsoMapDao();

        doReturn(storagePoolDao).when(validator).getStoragePoolDao();
        doReturn(storageDomainDao).when(validator).getStorageDomainDao();
    }

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_0.toString(), false),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_4.toString(), true),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_0.toString(), false),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_4.toString(), true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_0.toString(), false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_1.toString(), false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_2.toString(), false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_3.toString(), false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_4.toString(), true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_6.toString(), true),
            mockConfig(ConfigValues.PosixStorageEnabled, UNSUPPORTED_VERSION.toString(), false),
            mockConfig(ConfigValues.PosixStorageEnabled, SUPPORTED_VERSION.toString(), true),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, UNSUPPORTED_VERSION.toString(), false),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, SUPPORTED_VERSION.toString(), true)
    );

    @Test
    public void testAttachOnValidDomain() {
        assertThat("Attaching a valid domain to attach was failed",
                validator.validateDomainCanBeAttachedToPool(), isValid());
    }

    /**
     * Mixed types are not allowed on version lower than V3.4, test that attempting to attach a domain of different type
     * than what already exists in the data center will fail for versions 3.0 to 3.3 inclusive
     */
    @Test
    public void testMixedTypesOnAllVersions() {
        // Use an old format so the domain will be able to attach to each DC.
        storageDomain.setStorageFormat(StorageFormatType.V1);

        // Mock the pool to have a NFS type domain
        when(storagePoolDao.getStorageTypesInPool(storagePool.getId())).thenReturn(Collections.singletonList(StorageType.NFS));

        storageDomain.setStorageType(StorageType.ISCSI);
        for (Version version : Version.ALL) {
            if (version.compareTo(Version.v3_0) >= 0) { // No reason to test unsupported versions
                assertAddingMixedTypes(version, FeatureSupported.mixedDomainTypesOnDataCenter(version));
            }
        }
    }

    private void assertAddingMixedTypes(Version version, boolean addingMixedTypesShouldSucceed) {
        storagePool.setCompatibilityVersion(version);

        ValidationResult attachDomainResult = validator.validateDomainCanBeAttachedToPool();
        if (addingMixedTypesShouldSucceed) {
            assertThat("Attaching an ISCSI domain to a pool with NFS domain with with mixed type allowed failed, version: " + version, attachDomainResult, isValid());
        }
        else {
            assertThat(attachDomainResult, failsWith(EngineMessage.ACTION_TYPE_FAILED_MIXED_STORAGE_TYPES_NOT_ALLOWED));
        }
    }

    @Test
    public void testPosixDcAndMatchingCompatiblityVersion() {
        storagePool.setCompatibilityVersion(SUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isPosixSupportedInDC(), isValid());
    }

    @Test
    public void testPosixDcAndNotMatchingCompatiblityVersion() {
        storagePool.setCompatibilityVersion(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isPosixSupportedInDC(),
                failsWith(EngineMessage.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION));
    }

    @Test
    public void testGlusterDcAndMatchingCompatiblityVersion() {
        storagePool.setCompatibilityVersion(SUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isGlusterSupportedInDC(), isValid());
    }

    @Test
    public void testGlusterDcAndNotMatchingCompatiblityVersion() {
        storagePool.setCompatibilityVersion(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isGlusterSupportedInDC(),
                failsWith(EngineMessage.DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION));
    }

    @Test
    public void testLocalDcAndMatchingCompatiblityVersion() {
        storagePool.setCompatibilityVersion(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(true);
        assertThat(validator.isPosixSupportedInDC(), isValid());
    }

    @Test
    public void testPosixCompatibility() {
        storageDomain.setStorageType(StorageType.POSIXFS);
        assertThat("Attaching a POSIX domain failed while it should have succeeded",
                validator.validateDomainCanBeAttachedToPool(), isValid());
    }

    @Test
    public void testAttachPosixCompatibilityOnLowVersion() {
        storagePool.setCompatibilityVersion(Version.v3_0);

        storageDomain.setStorageType(StorageType.POSIXFS);
        storageDomain.setStorageFormat(StorageFormatType.V1);

        ValidationResult attachPosixToLowVersionResult = validator.validateDomainCanBeAttachedToPool();
        assertThat(attachPosixToLowVersionResult,
                failsWith(EngineMessage.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION));
    }

    @Test
    public void testGlusterCompatibility() {
        storageDomain.setStorageType(StorageType.GLUSTERFS);
        assertThat("Attaching a GLUSTER domain failed while it should have succeeded", validator.validateDomainCanBeAttachedToPool(), isValid());
    }

    @Test
    public void testGlusterCompatibilityOnLowVersion() {
        storagePool.setCompatibilityVersion(Version.v3_0);

        storageDomain.setStorageFormat(StorageFormatType.V1);
        storageDomain.setStorageType(StorageType.GLUSTERFS);

        ValidationResult attachGlusterToLowVersionResult = validator.validateDomainCanBeAttachedToPool();
        assertThat(attachGlusterToLowVersionResult,
                failsWith(EngineMessage.DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION));
    }

    @Test
    public void testAttachFailDomainTypeIncorrect() {
        storageDomain.setStorageType(StorageType.LOCALFS);
        ValidationResult attachIncorrectTypeResult = validator.validateDomainCanBeAttachedToPool();
        assertThat(attachIncorrectTypeResult,
                failsWith(EngineMessage.ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH));
    }

    @Test
    public void testAttachFailDomainAlreadyInPool() {
        when(storagePoolIsoMapDao.getAllForStorage(storageDomain.getId())).thenReturn(Collections.singletonList(new StoragePoolIsoMap()));

        ValidationResult attachedDomainInsertionResult = validator.validateDomainCanBeAttachedToPool();
        assertThat(attachedDomainInsertionResult,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL));
    }

    @Test
    public void testAttachFailFormatType() {
        storageDomain.setStorageFormat(StorageFormatType.V3);
        storagePool.setCompatibilityVersion(Version.v3_0);

        ValidationResult invalidFormatAttachingResult = validator.validateDomainCanBeAttachedToPool();
        assertThat(invalidFormatAttachingResult,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL));
    }

    /**
     * Tests attaching an ISO/Export domain to a pool first to a pool without an ISO/Export domain attached (should succeed)
     * then to a pool with an ISO/Export domain attached (should fail)
     */

    @Test
    public void testCanAttachSingleISOOrExport() {
        for (StorageDomainType type : Arrays.asList(StorageDomainType.ISO, StorageDomainType.ImportExport)) {
            storageDomain.setStorageDomainType(type);
            assertThat(validator.validateDomainCanBeAttachedToPool(), isValid());
        }
    }

    @Test
    public void testCanAttachMultipleISOOrExport() {
        for (StorageDomainType type : Arrays.asList(StorageDomainType.ISO, StorageDomainType.ImportExport)) {
            storageDomain.setStorageDomainType(type);

            // Make the pool to have already a domain with the same type of the domain we want to attach.
            StorageDomain domainWithSameType = new StorageDomain();
            domainWithSameType.setStorageDomainType(type);
            when(storageDomainDao.getAllForStoragePool(storagePool.getId())).thenReturn(Collections.singletonList(domainWithSameType));

            ValidationResult attachMultipleISOOrExportResult = validator.validateDomainCanBeAttachedToPool();
            assertThat("Attaching domain of type " + type + " succeeded though another domain of the same type already exists in the pool",
                    attachMultipleISOOrExportResult,
                    failsWith(type  == StorageDomainType.ISO ? EngineMessage.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_ISO_DOMAIN :
                            EngineMessage.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_EXPORT_DOMAIN));
        }
    }

    @Test
    public void testIsStorageDomainNotInAnyPoolSucceed() {
        assertThat(validator.isStorageDomainNotInAnyPool(), isValid());
    }

    @Test
    public void testIsStorageDomainNotInAnyPoolFailure() {
        when(storagePoolIsoMapDao.getAllForStorage(storageDomain.getId())).
                thenReturn(Collections.singletonList(new StoragePoolIsoMap()));
        assertThat(validator.isStorageDomainNotInAnyPool(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL));
    }
}
