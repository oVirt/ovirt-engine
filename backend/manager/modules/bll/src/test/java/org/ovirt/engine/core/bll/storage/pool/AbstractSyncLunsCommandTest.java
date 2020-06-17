package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

@ExtendWith(MockitoExtension.class)
public class AbstractSyncLunsCommandTest {

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private HostValidator hostValidator;

    private SyncLunsParameters parameters = new SyncLunsParameters();

    @InjectMocks
    @SuppressWarnings("unchecked")
    private AbstractSyncLunsCommand<SyncLunsParameters> command = mock(
            AbstractSyncLunsCommand.class,
            withSettings().useConstructor(parameters, null).defaultAnswer(CALLS_REAL_METHODS));

    @BeforeEach
    public void setUp() {
        doReturn(hostValidator).when(command).getHostValidator();
    }

    @Test
    public void testValidateStoragePoolSucceeds() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());
        storagePool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDao.get(storagePool.getId())).thenReturn(storagePool);
        command.setStoragePoolId(storagePool.getId());
        assertTrue(command.validate());
    }

    @Test
    public void testValidateStoragePoolNoStoragePoolId() {
        assertFalse(command.validate());
        ValidateTestUtils.assertValidationMessages("", command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testValidateStoragePoolRandomStoragePoolId() {
        command.setStoragePoolId(Guid.newGuid());
        assertFalse(command.validate());
        ValidateTestUtils.assertValidationMessages("", command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testValidateStoragePoolStoragePoolNotUp() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());
        when(storagePoolDao.get(storagePool.getId())).thenReturn(storagePool);
        command.setStoragePoolId(storagePool.getId());
        assertFalse(command.validate());
        ValidateTestUtils.assertValidationMessages("", command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
    }

    @Test
    public void testValidateVdsDoesNotExist() {
        when(hostValidator.hostExists()).thenReturn(new ValidationResult(EngineMessage.VDS_INVALID_SERVER_ID));
        assertFalse(command.validateVds());
        ValidateTestUtils.assertValidationMessages("", command,
                EngineMessage.VDS_INVALID_SERVER_ID);
    }

    @Test
    public void validateVdsIsNotUp() {
        when(hostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(hostValidator.isUp())
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL));
        assertFalse(command.validateVds());
        ValidateTestUtils.assertValidationMessages("", command,
                EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
    }

    @Test
    public void testGetDeviceListWithNoDeviceList() {
        List<LUNs> deviceList = new LinkedList<>(Collections.singletonList(new LUNs()));
        doReturn(deviceList).when(command).runGetDeviceList(any(), any());
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
