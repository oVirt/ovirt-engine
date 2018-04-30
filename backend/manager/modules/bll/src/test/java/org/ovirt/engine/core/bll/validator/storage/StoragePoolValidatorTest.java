package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class StoragePoolValidatorTest {

    private StoragePoolValidator validator;
    private StoragePool storagePool;

    @BeforeEach
    public void setup() {
        storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        validator = spy(new StoragePoolValidator(storagePool));
    }

    @Test
    public void testIsNotLocalFsWithDefaultCluster() {
        storagePool.setIsLocal(true);
        doReturn(false).when(validator).containsDefaultCluster();
        assertThat(validator.isNotLocalfsWithDefaultCluster(), isValid());
    }

    @Test
    public void testIsNotLocalFsWithDefaultClusterWhenClusterIsDefault() {
        storagePool.setIsLocal(true);
        doReturn(true).when(validator).containsDefaultCluster();
        assertThat(validator.isNotLocalfsWithDefaultCluster(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_CLUSTER_CANNOT_BE_LOCALFS));
    }

    @Test
    public void testExistsValid() {
        assertThat("Storage pool should exist", validator.exists(), isValid());
    }

    @Test
    public void testExistsInvalid() {
        validator = new StoragePoolValidator(null);
        assertThat(validator.exists(), failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST));
    }

    @Test
    public void testExistsAndUpValid() {
        assertThat("Storage pool should be up", validator.existsAndUp(), isValid());
    }

    @Test
    public void testExistAndUpInvalid() {
        storagePool.setStatus(StoragePoolStatus.NonResponsive);
        assertThat(validator.existsAndUp(), failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL));
    }

    @Test
    public void testIsInStatusValid() {
        assertThat(validator.isInStatus(StoragePoolStatus.Up, StoragePoolStatus.Contend), isValid());
        assertThat(validator.isNotInStatus(StoragePoolStatus.Up, StoragePoolStatus.Contend),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL));
    }

    @Test
    public void testIsInStatusInvalid() {
        assertThat(validator.isInStatus(StoragePoolStatus.NonResponsive, StoragePoolStatus.NotOperational),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL));
        assertThat(validator.isNotInStatus(StoragePoolStatus.NonResponsive, StoragePoolStatus.NotOperational),
                isValid());
    }
}
