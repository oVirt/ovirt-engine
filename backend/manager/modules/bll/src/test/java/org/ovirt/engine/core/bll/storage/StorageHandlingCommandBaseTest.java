package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

public class StorageHandlingCommandBaseTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    StorageHandlingCommandBase<StoragePoolManagementParameter> cmd;
    StoragePoolDAO dao;
    DbFacade facade;

    @Before
    public void setUp() {
        facade = mock(DbFacade.class);

        dao = mock(StoragePoolDAO.class);
        cmd = new TestStorageHandlingCommandBase(new StoragePoolManagementParameter(createStoragePool()));

        when(facade.getStoragePoolDao()).thenReturn(dao);
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
        assertTrue(cmd.checkStoragePool());
    }

    private static StoragePool createStoragePool() {
        StoragePool pool = new StoragePool();
        pool.setname("DefaultStoragePool");
        pool.setId(Guid.NewGuid());
        return pool;
    }

    private void checkStoragePoolFails() {
        assertFalse(cmd.checkStoragePool());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages
                .ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST.toString()));
    }

    private void createCommandWithNullPool() {
        cmd = new TestStorageHandlingCommandBase(new StoragePoolManagementParameter());
    }

    private void createValidStoragePool() {
        when(dao.get(any(Guid.class))).thenReturn(createStoragePool());
    }

    private static void setAcceptableNameLength(final int length) {
        mcr.mockConfigValue(ConfigValues.StoragePoolNameSizeLimit, length);
    }

    private void checkStoragePoolNameLengthSucceeds() {
        assertFalse(cmd.CheckStoragePoolNameLengthValid());
    }

    private void checkStoragePoolNameLengthFails() {
        assertTrue(cmd.CheckStoragePoolNameLengthValid());
    }

    private class TestStorageHandlingCommandBase extends StorageHandlingCommandBase<StoragePoolManagementParameter> {
        private static final long serialVersionUID = 261663274282182312L;

        public TestStorageHandlingCommandBase(StoragePoolManagementParameter parameters) {
            super(parameters);
        }

        @Override
        protected DbFacade getDbFacade() {
            return facade;
        }

        @Override
        protected void executeCommand() {
            // Intentionally empty - no behavior is requiered
        }
    }
}
