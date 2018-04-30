package org.ovirt.engine.core.bll.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class StorageHandlingCommandBaseTest extends BaseCommandTest {

    @InjectMocks
    private StorageHandlingCommandBase<StoragePoolManagementParameter> cmd = mock(
            StorageHandlingCommandBase.class,
            withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS)
                    .useConstructor(new StoragePoolManagementParameter(createStoragePool()), null));

    @Mock
    private StoragePoolDao storagePoolDao;

    private StoragePool storagePool;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.StoragePoolNameSizeLimit, 10));
    }

    @BeforeEach
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
