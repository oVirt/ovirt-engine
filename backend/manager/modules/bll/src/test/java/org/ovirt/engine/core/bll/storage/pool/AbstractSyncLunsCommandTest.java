package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSyncLunsCommandTest {

    @Mock
    private StoragePoolDao storagePoolDao;

    private SyncLunsParameters parameters = new SyncLunsParameters();

    @InjectMocks
    @SuppressWarnings("unchecked")
    private AbstractSyncLunsCommand<SyncLunsParameters> command = mock(
            AbstractSyncLunsCommand.class,
            withSettings().useConstructor(parameters, null).defaultAnswer(CALLS_REAL_METHODS));

    @Test
    public void testValidateStoragePoolSucceeds() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());
        storagePool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDao.get(storagePool.getId())).thenReturn(storagePool);
        command.setStoragePoolId(storagePool.getId());
        assertTrue(command.validateStoragePool());
    }

    @Test
    public void testValidateStoragePoolNoStoragePoolId() {
        assertFalse(command.validateStoragePool());
        ValidateTestUtils.assertValidationMessages("", command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testValidateStoragePoolRandomStoragePoolId() {
        command.setStoragePoolId(Guid.newGuid());
        assertFalse(command.validateStoragePool());
        ValidateTestUtils.assertValidationMessages("", command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testValidateStoragePoolStoragePoolNotUp() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());
        when(storagePoolDao.get(storagePool.getId())).thenReturn(storagePool);
        command.setStoragePoolId(storagePool.getId());
        assertFalse(command.validateStoragePool());
        ValidateTestUtils.assertValidationMessages("", command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
    }

    @Test
    public void testGetDeviceListWithNoDeviceList() {
        List<LUNs> deviceList = new LinkedList<>(Collections.singletonList(new LUNs()));
        doReturn(deviceList).when(command).runGetDeviceList(any());
        assertEquals(deviceList, command.getDeviceList());
    }

    @Test
    public void testGetDeviceListWithDeviceListAndNoLunsIds() {
        List<LUNs> deviceList = new LinkedList<>(Collections.singletonList(new LUNs()));
        parameters.setDeviceList(deviceList);
        assertEquals(deviceList, command.getDeviceList());
    }

    @Test
    public void testGetDeviceListWithDeviceListAndLunsIds() {
        LUNs lun1 = new LUNs();
        lun1.setId("lun1");
        LUNs lun2 = new LUNs();
        lun2.setId("lun2");

        parameters.setDeviceList(Arrays.asList(lun1, lun2));
        Set<String> lunsIds = Collections.singleton("lun2");

        assertEquals(command.getDeviceList(lunsIds), Collections.singletonList(lun2));
    }
}
