package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;

@RunWith(MockitoJUnitRunner.class)
public class StorageDomainToPoolRelationValidatorTest {
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
        storagePool.setCompatibilityVersion(Version.ALL.get(0));

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

    @Test
    public void testAttachOnValidDomain() {
        assertThat("Attaching a valid domain to attach was failed",
                validator.validateDomainCanBeAttachedToPool(), isValid());
    }

    @Test
    public void testPosixCompatibility() {
        storageDomain.setStorageType(StorageType.POSIXFS);
        assertThat("Attaching a POSIX domain failed while it should have succeeded",
                validator.validateDomainCanBeAttachedToPool(), isValid());
    }

    @Test
    public void testGlusterCompatibility() {
        storageDomain.setStorageType(StorageType.GLUSTERFS);
        assertThat("Attaching a GLUSTER domain failed while it should have succeeded", validator.validateDomainCanBeAttachedToPool(), isValid());
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
