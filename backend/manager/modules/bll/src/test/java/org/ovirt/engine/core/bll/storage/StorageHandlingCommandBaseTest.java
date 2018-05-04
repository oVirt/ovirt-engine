package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class StorageHandlingCommandBaseTest extends BaseCommandTest {

    @InjectMocks
    private StorageHandlingCommandBase<StoragePoolManagementParameter> cmd = mock(
            StorageHandlingCommandBase.class,
            withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS)
                    .useConstructor(new StoragePoolManagementParameter(createStoragePool()), null));

    @Mock
    private StoragePoolDao storagePoolDao;

    private StoragePool storagePool;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.StoragePoolNameSizeLimit, 10));

    @Before
    public void setUp() {
        storagePool = cmd.getParameters().getStoragePool();
        cmd.init();

        when(storagePoolDao.get(storagePool.getId())).thenReturn(storagePool);
    }

    @Test
    public void nameTooLong() {
        storagePool.setName("123456789 - this is too long");
        checkStoragePoolNameLengthSucceeds();
    }

    @Test
    public void nameAcceptableLength() {
        storagePool.setName("123456789");
        checkStoragePoolNameLengthFails();
    }

    private static StoragePool createStoragePool() {
        StoragePool pool = new StoragePool();
        pool.setName("DefaultStoragePool");
        pool.setId(Guid.newGuid());
        pool.setIsLocal(false);
        return pool;
    }

    private void checkStoragePoolNameLengthSucceeds() {
        assertFalse(cmd.checkStoragePoolNameLengthValid());
    }

    private void checkStoragePoolNameLengthFails() {
        assertTrue(cmd.checkStoragePoolNameLengthValid());
    }
}
