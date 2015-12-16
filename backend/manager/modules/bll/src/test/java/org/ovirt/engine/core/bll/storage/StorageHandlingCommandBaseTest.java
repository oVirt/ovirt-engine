package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class StorageHandlingCommandBaseTest extends BaseCommandTest {

    private StorageHandlingCommandBase<StoragePoolManagementParameter> cmd;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    private StoragePool storagePool;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_0, false),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_4, true),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_5, true),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v3_6, true),
            mockConfig(ConfigValues.GlusterFsStorageEnabled, Version.v4_0, true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_0, false),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_4, true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_5, true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_6, true),
            mockConfig(ConfigValues.PosixStorageEnabled, Version.v4_0, true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_0, false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_1, false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_2, false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_3, false),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_4, true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_5, true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v3_6, true),
            mockConfig(ConfigValues.MixedDomainTypesInDataCenter, Version.v4_0, true)
    );

    @Before
    public void setUp() {
        storagePool = createStoragePool(Version.v3_4);

        initCommand();

        doReturn(storagePoolDao).when(cmd).getStoragePoolDao();
        when(storagePoolDao.get(storagePool.getId())).thenReturn(storagePool);

        doReturn(storageDomainDao).when(cmd).getStorageDomainDao();
        when(storageDomainDao.getAllForStoragePool(storagePool.getId())).thenReturn(Collections.<StorageDomain>emptyList());

        doReturn(storagePoolIsoMapDao).when(cmd).getStoragePoolIsoMapDao();
        when(storagePoolIsoMapDao.getAllForStorage(any(Guid.class))).thenReturn(Collections.<StoragePoolIsoMap>emptyList());
    }

    public void initCommand() {
        cmd = spy(new TestStorageHandlingCommandBase(new StoragePoolManagementParameter(storagePool)));
    }

    @Test
    public void storagePoolNotFound() {
        when(storagePoolDao.get(storagePool.getId())).thenReturn(null);
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
        assertFalse(cmd.checkStoragePool());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage
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

    private static class TestStorageHandlingCommandBase extends StorageHandlingCommandBase<StoragePoolManagementParameter> {
        TestStorageHandlingCommandBase(StoragePoolManagementParameter parameters) {
            super(parameters);
        }

        @Override
        protected void executeCommand() {
            // Intentionally empty - no behavior is requiered
        }
    }
}
