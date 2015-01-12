package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
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
            // Indicates a supported storage format of V2 & V3
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_0.toString(), "0,1"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_1.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_2.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_3.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_4.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_5.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_6.toString(), "3"),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_0.toString(), false),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_4.toString(), true),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_6.toString(), true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_0.toString(), false),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_4.toString(), true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_6.toString(), true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_0.toString(), false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_1.toString(), false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_2.toString(), false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_3.toString(), false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_4.toString(), true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_6.toString(), true)
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

    private void checkStoragePoolSucceeds() {
        assertTrue(cmd.checkStoragePool());
    }

    private static StoragePool createStoragePool(Version compatibilityVersion) {
        StoragePool pool = new StoragePool();
        pool.setName("DefaultStoragePool");
        pool.setId(Guid.newGuid());
        pool.setIsLocal(false);
        pool.setCompatibilityVersion(compatibilityVersion);
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
