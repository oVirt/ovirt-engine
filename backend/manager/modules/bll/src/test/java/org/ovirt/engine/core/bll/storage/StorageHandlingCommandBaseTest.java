package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
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
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class StorageHandlingCommandBaseTest {

    StorageHandlingCommandBase<StoragePoolManagementParameter> cmd;

    @Mock
    StoragePoolDAO storagePoolDAO;

    @Mock
    DbFacade facade;

    @Mock
    StorageDomainDAO storageDomainDAO;

    @Mock
    private StoragePoolIsoMapDAO storagePoolIsoMapDAO;

    List<StorageDomain> attachedDomains = new ArrayList<StorageDomain>();

    StoragePool storagePool;
    List<StoragePoolIsoMap> isoMap = new ArrayList<>();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            // Indicates a supported storage format of V2 & V3 in version 3.4
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_0.toString(), "0,1"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_1.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_2.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_3.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_4.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_5.toString(), "3"),
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
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_5.toString(), true)
    );

    @Before
    public void setUp() {
        storagePool = createStoragePool(Version.v3_4);

        initCommand();

        when(facade.getStoragePoolDao()).thenReturn(storagePoolDAO);
        when(facade.getStoragePoolDao().get(storagePool.getId())).thenReturn(storagePool);

        when(facade.getStorageDomainDao()).thenReturn(storageDomainDAO);
        when(storageDomainDAO.getAllForStoragePool(storagePool.getId())).thenReturn(attachedDomains);

        when(facade.getStoragePoolIsoMapDao()).thenReturn(storagePoolIsoMapDAO);
        when(storagePoolIsoMapDAO.getAllForStorage(any(Guid.class))).thenReturn(isoMap);
    }

    public void initCommand() {
        cmd = new TestStorageHandlingCommandBase(new StoragePoolManagementParameter(storagePool));
    }

    @Test
    public void storagePoolNotFound() {
        checkStoragePoolFails();
    }

    @Test
    public void storagePoolNull() {
        createCommandWithNullPool();
        checkStoragePoolFails();
    }

    @Test
    public void storagePoolExists() {
        checkStoragePoolSucceeds();
    }

    @Test
    public void nameTooLong() {
        setAcceptableNameLength(10);
        checkStoragePoolNameLengthSucceeds();
    }

    @Test
    public void nameAcceptableLength() {
        setAcceptableNameLength(255);
        checkStoragePoolNameLengthFails();
    }



    private StorageDomain createValidStorageDomain() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        storageDomain.setStorageFormat(StorageFormatType.V3);
        return storageDomain;
    }

    @Test
    public void testAttachOnValidDomain() {
        StorageDomain storageDomain = createValidStorageDomain();
        assertTrue("Attaching a valid domain to attach was failed", cmd.checkDomainCanBeAttached(storageDomain));
    }


    /**
     * Mixed types are not allowed on version lower than V3.4, test that attempting to attach a domain of different type
     * than what already exists in the data center will fail for versions 3.0 to 3.3 inclusive
     */
    @Test
    public void testMixedTypesOnAllVersions() {
        for (Version version : Version.ALL) {
            if (version.compareTo(Version.v3_0) >= 0) { // No reason to test unsupported versions
                testAddingMixedTypes(version, FeatureSupported.mixedDomainTypesOnDataCenter(version));
            }
        }
    }

    private void testAddingMixedTypes(Version version, boolean addingMixedTypesShouldSucceed) {
        storagePool.setcompatibility_version(version);

        // This will make the storage pool show as if he already has an NFS domain attached
        when(storagePoolDAO.getStorageTypesInPool(storagePool.getId())).thenReturn(Collections.singletonList(StorageType.NFS));

        StorageDomain domainToAttach = createValidStorageDomain();
        domainToAttach.setStorageFormat(cmd.getSupportedStorageFormatSet(version).iterator().next());
        initCommand();
        assertTrue("Attaching an NFS domain to a pool with NFS domain with no mixed type allowed failed, version: " + version, cmd.checkDomainCanBeAttached(domainToAttach));

        domainToAttach.setStorageType(StorageType.ISCSI);
        initCommand();
        if (addingMixedTypesShouldSucceed) {
            assertTrue("Attaching an ISCSI domain to a pool with NFS domain with with mixed type allowed failed, version: " + version, cmd.checkDomainCanBeAttached(domainToAttach));
        }
        else {
            assertFalse("Attaching an ISCSI domain to a pool with NFS domain with no mixed type allowed succeeded, version: " + version, cmd.checkDomainCanBeAttached(domainToAttach));
            CanDoActionTestUtils.assertCanDoActionMessages("Attaching an ISCSI domain to a pool with NFS domain with no mixed type failed with the wrong message", cmd,
                    VdcBllMessages.ACTION_TYPE_FAILED_MIXED_STORAGE_TYPES_NOT_ALLOWED);
        }

    }

    @Test
    public void testAttachPosixCompatibility() {
        StorageDomain storageDomain = createValidStorageDomain();

        storageDomain.setStorageType(StorageType.POSIXFS);
        assertTrue("Attaching a POSIX domain failed while it should have succeeded", cmd.checkDomainCanBeAttached(storageDomain));

        storagePool.setcompatibility_version(Version.v3_0);
        storageDomain.setStorageFormat(StorageFormatType.V1);

        initCommand();
        storageDomain.setStorageType(StorageType.POSIXFS);
        assertFalse("Attaching a POSIX domain succeeded while it should have failed", cmd.checkDomainCanBeAttached(storageDomain));
        CanDoActionTestUtils.assertCanDoActionMessages("Attaching a POSIX domain failed with the wrong message", cmd,
                VdcBllMessages.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);


    }

    @Test
    public void testAttachGlusterCompatibility() {
        StorageDomain storageDomain = createValidStorageDomain();
        storageDomain.setStorageType(StorageType.GLUSTERFS);
        assertTrue("Attaching a GLUSTER domain failed while it should have succeeded", cmd.checkDomainCanBeAttached(storageDomain));

        storagePool.setcompatibility_version(Version.v3_0);
        storageDomain.setStorageFormat(StorageFormatType.V1);

        initCommand();
        storageDomain.setStorageType(StorageType.GLUSTERFS);
        assertFalse("Attaching a GLUSTER domain succeeded while it should have failed", cmd.checkDomainCanBeAttached(storageDomain));
        CanDoActionTestUtils.assertCanDoActionMessages("Attaching a GLUSTER domain failed failed with the wrong message", cmd,
                VdcBllMessages.DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
    }

    @Test
    public void testAttachFailDomainTypeIncorrect() {
        StorageDomain storageDomain = createValidStorageDomain();
        storageDomain.setStorageType(StorageType.LOCALFS);
        assertFalse("Attaching domain with an incorrect type succeeded while it should have failed", cmd.checkDomainCanBeAttached(storageDomain));
        CanDoActionTestUtils.assertCanDoActionMessages("Attaching domain with an incorrect type failed with the wrong message", cmd,
                VdcBllMessages.ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH);
    }

    @Test
    public void testAttachFailDomainAlreadyInPool() {
        StorageDomain storageDomain = createValidStorageDomain();
        isoMap.add(new StoragePoolIsoMap());
        assertFalse("Attaching domain that is already in a pool succeeded while it should have failed", cmd.checkDomainCanBeAttached(storageDomain));
        CanDoActionTestUtils.assertCanDoActionMessages("Attaching domain that is already in a pool failed with the wrong message", cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
    }


    @Test
    public void testAttachFailLockDomain() {
        StorageDomain storageDomain = createValidStorageDomain();
        storageDomain.setStorageDomainSharedStatus(StorageDomainSharedStatus.Locked);
        assertFalse("Attaching domain in locked status succeeded while it should have failed", cmd.checkDomainCanBeAttached(storageDomain));
        CanDoActionTestUtils.assertCanDoActionMessages("Attaching domain with in locked status failed with the wrong message", cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
    }

    @Test
    public void testAttachFailFormatType() {
        StorageDomain domainToAttach = createValidStorageDomain();
        domainToAttach.setStorageFormat(StorageFormatType.V2);
        assertFalse("Attaching domain with unsupported version succeeded while it should have failed", cmd.checkDomainCanBeAttached(domainToAttach));
        CanDoActionTestUtils.assertCanDoActionMessages("Attaching domain with unsupported version failed with the wrong message", cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL);
    }

    @Test
    public void testCanAttachISOOrExport() {
        for (StorageDomainType type : Arrays.<StorageDomainType> asList(StorageDomainType.ISO, StorageDomainType.ImportExport)) {
            StorageDomain storageDomainToAttach = createValidStorageDomain();
            storageDomainToAttach.setStorageDomainType(type);
            testCanAttachWithISOOrExport(storageDomainToAttach);
        }
    }

    /**
     * Tests attaching an ISO/Export domain to a pool first to a pool without an ISO/Export domain attached (should succeed)
     * then to a pool with an ISO/Export domain attached (should fail)
     */
    private void testCanAttachWithISOOrExport(StorageDomain domainToAttach) {
        assertTrue("Attaching domain of type " + domainToAttach.getStorageDomainType() + " failed while it should have succeed",
                cmd.checkDomainCanBeAttached(domainToAttach));

        StorageDomain existingDomain = createValidStorageDomain();
        existingDomain.setStorageDomainType(domainToAttach.getStorageDomainType());
        addDomainToPool(existingDomain);
        assertFalse("Attaching domain of type " + domainToAttach.getStorageDomainType() + " succeeded while it should have failed",
                cmd.checkDomainCanBeAttached(domainToAttach));
        CanDoActionTestUtils.assertCanDoActionMessages("Attaching domain of type " + domainToAttach.getStorageDomainType()
                + " succeeded though another domain" +
                "of the same type already exists in the pool", cmd,
                domainToAttach.getStorageDomainType()  == StorageDomainType.ISO ? VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_ISO_DOMAIN :
                    VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_EXPORT_DOMAIN);

    }

    private void addDomainToPool(StorageDomain storageDomain) {
        attachedDomains.add(storageDomain);
    }

    private void checkStoragePoolSucceeds() {
        assertTrue(cmd.checkStoragePool());
    }

    private static StoragePool createStoragePool(Version compatibilityVersion) {
        StoragePool pool = new StoragePool();
        pool.setName("DefaultStoragePool");
        pool.setId(Guid.newGuid());
        pool.setIsLocal(false);
        pool.setcompatibility_version(compatibilityVersion);
        return pool;
    }

    private void checkStoragePoolFails() {
        when(facade.getStoragePoolDao().get(storagePool.getId())).thenReturn(null);
        assertFalse(cmd.checkStoragePool());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages
                .ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST.toString()));
    }

    private void createCommandWithNullPool() {
        cmd = new TestStorageHandlingCommandBase(new StoragePoolManagementParameter());
    }

    private static void setAcceptableNameLength(final int length) {
        mcr.mockConfigValue(ConfigValues.StoragePoolNameSizeLimit, length);
    }

    private void checkStoragePoolNameLengthSucceeds() {
        assertFalse(cmd.checkStoragePoolNameLengthValid());
    }

    private void checkStoragePoolNameLengthFails() {
        assertTrue(cmd.checkStoragePoolNameLengthValid());
    }

    private class TestStorageHandlingCommandBase extends StorageHandlingCommandBase<StoragePoolManagementParameter> {
        public TestStorageHandlingCommandBase(StoragePoolManagementParameter parameters) {
            super(parameters);
        }

        @Override
        public DbFacade getDbFacade() {
            return facade;
        }

        @Override
        protected void executeCommand() {
            // Intentionally empty - no behavior is requiered
        }
    }
}
