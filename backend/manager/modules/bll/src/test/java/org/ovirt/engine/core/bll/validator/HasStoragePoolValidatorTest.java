package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.HasStoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

@RunWith(MockitoJUnitRunner.class)
public class HasStoragePoolValidatorTest {
    @Mock
    private StoragePoolDao storagePoolDao;

    private HasStoragePool hsp;
    private HasStoragePoolValidator validator;

    @Before
    public void setUp() {
        hsp = new Cluster();
        validator = spy(new HasStoragePoolValidator(hsp));
        doReturn(storagePoolDao).when(validator).getStoragePoolDao();
    }

    @Test
    public void dataCenterExists() {
        Guid id = Guid.newGuid();
        hsp.setStoragePoolId(id);
        when(storagePoolDao.get(id)).thenReturn(mock(StoragePool.class));
        assertThat(validator.storagePoolExists(), isValid());
    }

    @Test
    public void dataCenterNotExistsWhenClusterIsOrphan() {
        assertThat(validator.storagePoolExists(), isValid());
    }

    @Test
    public void dataCenterDoesNotExist() {
        hsp.setStoragePoolId(Guid.newGuid());
        assertThat(validator.storagePoolExists(), failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST));
    }
}
