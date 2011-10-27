package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({DbFacade.class, Config.class})
@RunWith(PowerMockRunner.class)
public class StorageHandlingCommandBaseTest {

    StorageHandlingCommandBase cmd;
    StoragePoolDAO dao;

    @Before
    public void setUp() {
        mockStatic(DbFacade.class);
        mockStatic(Config.class);

        DbFacade facade = mock(DbFacade.class);

        dao = mock(StoragePoolDAO.class);
        cmd = new TestStorageHandlingCommandBase(new StoragePoolManagementParameter(createStoragePool()));

        when(DbFacade.getInstance()).thenReturn(facade);
        when(facade.getStoragePoolDAO()).thenReturn(dao);
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
        createValidStoragePool();
        checkStoragePoolSucceeds();
    }

    @Test
    public void nameTooLong() {
        createValidStoragePool();
        setAcceptableNameLength(10);
        checkStoragePoolNameLengthSucceeds();
    }

    @Test
    public void nameAcceptableLength() {
        createValidStoragePool();
        setAcceptableNameLength(255);
        checkStoragePoolNameLengthFails();
    }

    private void checkStoragePoolSucceeds() {
        assertTrue(cmd.CheckStoragePool());
    }

    private storage_pool createStoragePool() {
        storage_pool pool = new storage_pool();
        pool.setname("DefaultStoragePool");
        pool.setId(Guid.NewGuid());
        return pool;
    }

    private void checkStoragePoolFails() {
        assertFalse(cmd.CheckStoragePool());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages
                .ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST.toString()));
    }

    private void createCommandWithNullPool() {
        cmd = new TestStorageHandlingCommandBase(new StoragePoolManagementParameter());
    }

    private void createValidStoragePool() {
        when(dao.get(any(Guid.class))).thenReturn(createStoragePool());
    }

    private void setAcceptableNameLength(final int length) {
        when(Config.GetValue(ConfigValues.StoragePoolNameSizeLimit)).thenReturn(length);
    }

    private void checkStoragePoolNameLengthSucceeds() {
        assertFalse(cmd.CheckStoragePoolNameLengthValid());
    }

    private void checkStoragePoolNameLengthFails() {
        assertTrue(cmd.CheckStoragePoolNameLengthValid());
    }

    private class TestStorageHandlingCommandBase extends StorageHandlingCommandBase<StoragePoolManagementParameter> {

        public TestStorageHandlingCommandBase(StoragePoolManagementParameter parameters) {
            super(parameters);
        }

        @Override
        protected void executeCommand() {
        }
    }
}
