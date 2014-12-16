package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class StorageDomainToPoolRelationValidatorTest {
    private static final Version UNSUPPORTED_VERSION = Version.v3_0;
    private static final Version SUPPORTED_VERSION = Version.v3_3;

    private StorageDomain storageDomain;
    private StoragePool storagePool;
    private StorageDomainToPoolRelationValidator validator;

    @Mock
    private StoragePoolDAO storagePoolDAO;
    @Mock
    private StorageDomainDAO storageDomainDAO;
    @Mock
    private StoragePoolIsoMapDAO storagePoolIsoMapDAO;

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
        storagePool.setcompatibility_version(Version.v3_5);

        when(storagePoolIsoMapDAO.getAllForStorage(any(Guid.class))).thenReturn(new ArrayList<StoragePoolIsoMap>());
        spyValidator();
    }

    private void spyValidator() {
        // Create the spied validators.
        validator = spy(new StorageDomainToPoolRelationValidator(storageDomain.getStorageStaticData(), storagePool));

        doReturn(storagePoolIsoMapDAO).when(validator).getStoragePoolIsoMapDao();

        doReturn(storagePoolDAO).when(validator).getStoragePoolDao();
        doReturn(storageDomainDAO).when(validator).getStorageDomainDao();
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
        assertTrue("Attaching a valid domain to attach was failed",
                validator.validateDomainCanBeAttachedToPool().isValid());
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
        when(storagePoolDAO.getStorageTypesInPool(storagePool.getId())).thenReturn(Collections.singletonList(StorageType.NFS));

        storageDomain.setStorageType(StorageType.ISCSI);
        for (Version version : Version.ALL) {
            if (version.compareTo(Version.v3_0) >= 0) { // No reason to test unsupported versions
                testAddingMixedTypes(version, FeatureSupported.mixedDomainTypesOnDataCenter(version));
            }
        }
    }

    private void testAddingMixedTypes(Version version, boolean addingMixedTypesShouldSucceed) {
        storagePool.setcompatibility_version(version);

        ValidationResult attachDomainResult = validator.validateDomainCanBeAttachedToPool();
        if (addingMixedTypesShouldSucceed) {
            assertTrue("Attaching an ISCSI domain to a pool with NFS domain with with mixed type allowed failed, version: " + version, attachDomainResult.isValid());
        }
        else {
            assertFalse("Attaching an ISCSI domain to a pool with NFS domain with no mixed type allowed succeeded, version: " + version, attachDomainResult.isValid());
            assertFailingMessage(
                    "Attaching an ISCSI domain to a pool with NFS domain with no mixed type failed with the wrong message",
                    attachDomainResult,
                    VdcBllMessages.ACTION_TYPE_FAILED_MIXED_STORAGE_TYPES_NOT_ALLOWED);
        }
    }

    @Test
    public void testPosixDcAndMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(SUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isPosixSupportedInDC(), isValid());
    }

    @Test
    public void testPosixDcAndNotMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isPosixSupportedInDC(),
                failsWith(VdcBllMessages.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION));
    }

    @Test
    public void testGlusterDcAndMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(SUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isGlusterSupportedInDC(), isValid());
    }

    @Test
    public void testGlusterDcAndNotMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isGlusterSupportedInDC(),
                failsWith(VdcBllMessages.DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION));
    }

    @Test
    public void testLocalDcAndMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(true);
        assertThat(validator.isPosixSupportedInDC(), isValid());
    }

    @Test
    public void testPosixCompatibility() {
        storageDomain.setStorageType(StorageType.POSIXFS);
        assertTrue("Attaching a POSIX domain failed while it should have succeeded",
                validator.validateDomainCanBeAttachedToPool().isValid());
    }

    @Test
    public void testAttachPosixCompatibilityOnLowVersion() {
        storagePool.setcompatibility_version(Version.v3_0);

        storageDomain.setStorageType(StorageType.POSIXFS);
        storageDomain.setStorageFormat(StorageFormatType.V1);

        ValidationResult attachPosixToLowVersionResult = validator.validateDomainCanBeAttachedToPool();
        assertFalse("Attaching a POSIX domain succeeded while it should have failed",
                attachPosixToLowVersionResult.isValid());
        assertFailingMessage("Attaching a POSIX domain failed with the wrong message",
                attachPosixToLowVersionResult,
                VdcBllMessages.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
    }

    @Test
    public void testGlusterCompatibility() {
        storageDomain.setStorageType(StorageType.GLUSTERFS);
        assertTrue("Attaching a GLUSTER domain failed while it should have succeeded", validator.validateDomainCanBeAttachedToPool().isValid());
    }

    @Test
    public void testGlusterCompatibilityOnLowVersion() {
        storagePool.setcompatibility_version(Version.v3_0);

        storageDomain.setStorageFormat(StorageFormatType.V1);
        storageDomain.setStorageType(StorageType.GLUSTERFS);

        ValidationResult attachGlusterToLowVersionResult = validator.validateDomainCanBeAttachedToPool();
        assertFalse("Attaching a GLUSTER domain succeeded while it should have failed", attachGlusterToLowVersionResult.isValid());
        assertFailingMessage("Attaching a GLUSTER domain failed failed with the wrong message",
                attachGlusterToLowVersionResult,
                VdcBllMessages.DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
    }

    @Test
    public void testAttachFailDomainTypeIncorrect() {
        storageDomain.setStorageType(StorageType.LOCALFS);
        ValidationResult attachIncorrectTypeResult = validator.validateDomainCanBeAttachedToPool();
        assertFalse("Attaching domain with an incorrect type succeeded while it should have failed", attachIncorrectTypeResult.isValid());
        assertFailingMessage("Attaching domain with an incorrect type failed with the wrong message",
                attachIncorrectTypeResult,
                VdcBllMessages.ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH);
    }

    @Test
    public void testAttachFailDomainAlreadyInPool() {
        List<StoragePoolIsoMap> isoMap = new ArrayList<>();
        isoMap.add(new StoragePoolIsoMap());
        when(storagePoolIsoMapDAO.getAllForStorage(any(Guid.class))).thenReturn(isoMap);

        ValidationResult attachedDomainInsertionResult = validator.validateDomainCanBeAttachedToPool();
        assertFalse("Attaching domain that is already in a pool succeeded while it should have failed",
                attachedDomainInsertionResult.isValid());
        assertFailingMessage("Attaching domain that is already in a pool failed with the wrong message",
                attachedDomainInsertionResult,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
    }

    @Test
    public void testAttachFailFormatType() {
        storageDomain.setStorageFormat(StorageFormatType.V3);
        storagePool.setcompatibility_version(Version.v3_0);

        ValidationResult invalidFormatAttachingResult = validator.validateDomainCanBeAttachedToPool();
        assertFalse("Attaching domain with unsupported version succeeded while it should have failed", invalidFormatAttachingResult.isValid());
        assertFailingMessage("Attaching domain with unsupported version failed with the wrong message",
                invalidFormatAttachingResult,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL);
    }

    /**
     * Tests attaching an ISO/Export domain to a pool first to a pool without an ISO/Export domain attached (should succeed)
     * then to a pool with an ISO/Export domain attached (should fail)
     */

    @Test
    public void testCanAttachSingleISOOrExport() {
        for (StorageDomainType type : Arrays.<StorageDomainType> asList(StorageDomainType.ISO, StorageDomainType.ImportExport)) {
            storageDomain.setStorageDomainType(type);
            spyValidator();
            assertTrue("Attaching domain of type " + type + " failed while it should have succeed", validator.validateDomainCanBeAttachedToPool().isValid());
        }
    }

    @Test
    public void testCanAttachMultipleISOOrExport() {
        for (StorageDomainType type : Arrays.<StorageDomainType> asList(StorageDomainType.ISO, StorageDomainType.ImportExport)) {
            storageDomain.setStorageDomainType(type);
            spyValidator();

            // Make the pool to have already a domain with the same type of the domain we want to attach.
            ArrayList<StorageDomain> domainList = new ArrayList<StorageDomain>();
            StorageDomain domainWithSameType = new StorageDomain();
            domainWithSameType.setStorageDomainType(type);
            domainList.add(domainWithSameType);
            when(storageDomainDAO.getAllForStoragePool(any(Guid.class))).thenReturn(domainList);

            ValidationResult attachMultipleISOOrExportResult = validator.validateDomainCanBeAttachedToPool();
            assertFalse("Attaching domain of type " + type + " succeeded while it should have failed",
                    attachMultipleISOOrExportResult.isValid());

            assertFailingMessage("Attaching domain of type " + type + " succeeded though another domain of the same type already exists in the pool",
                    attachMultipleISOOrExportResult,
                    (type  == StorageDomainType.ISO ? VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_ISO_DOMAIN :
                            VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_EXPORT_DOMAIN));
        }
    }

    private void assertFailingMessage(String failMessage, ValidationResult validationResult, VdcBllMessages vdcBllMessage) {
        assertTrue(failMessage, validationResult.getMessage().equals(vdcBllMessage));
    }
}
