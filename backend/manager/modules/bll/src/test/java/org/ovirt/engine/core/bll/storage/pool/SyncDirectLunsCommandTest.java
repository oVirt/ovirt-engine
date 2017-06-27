package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.ValidateTestUtils.runAndAssertValidateFailure;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.SyncDirectLunsParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;

@RunWith(MockitoJUnitRunner.class)
public class SyncDirectLunsCommandTest {

    @Mock
    private DiskLunMapDao diskLunMapDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private HostValidator hostValidator;

    private LUNs lun1;
    private LUNs lun2;
    private LUNs lun3;
    private DiskLunMap disk1Lun1Map;

    @Spy
    @InjectMocks
    private SyncDirectLunsCommand<SyncDirectLunsParameters> command =
            new SyncDirectLunsCommand<>(new SyncDirectLunsParameters(), null);

    @Before
    public void setUp() {
        lun1 = new LUNs();
        lun1.setLUNId("lun1");
        lun1.setDiskId(Guid.newGuid());
        disk1Lun1Map = new DiskLunMap(lun1.getDiskId(), lun1.getLUNId());

        lun2 = new LUNs();
        lun2.setLUNId("lun2");
        lun2.setDiskId(Guid.newGuid());

        lun3 = new LUNs();
        lun3.setLUNId("lun3");
        lun3.setDiskId(Guid.newGuid());

        doReturn(hostValidator).when(command).getHostValidator();
    }

    @Test
    public void testGetLunsToUpdateInDbWithoutDirectLunId() {
        command.getParameters().setDeviceList(Arrays.asList(lun1, lun2, lun3));
        mockLunToDiskIdsOfDirectLunsAttachedToVmsInPool(lun1, lun2);
        assertEquals(Arrays.asList(lun1, lun2), command.getLunsToUpdateInDb());
    }

    @Test
    public void testGetLunsToUpdateInDbWithDirectLunId() {
        command.getParameters().setDeviceList(Arrays.asList(lun1, lun2, lun3));
        mockLunToDiskIdsOfDirectLunsAttachedToVmsInPool(lun1, lun2);
        command.getParameters().setDirectLunId(lun1.getDiskId());
        when(diskLunMapDao.getDiskLunMapByDiskId(lun1.getDiskId())).thenReturn(disk1Lun1Map);
        assertEquals(Collections.singletonList(lun1), command.getLunsToUpdateInDb());
    }

    @Test
    public void validateNoDirectLunIdAndInvalidStoragePool() {
        doReturn(false).when(command).validateStoragePool();
        assertFalse(command.validate());
    }

    @Test
    public void validateNoDirectLunIdAndValidStoragePool() {
        doReturn(true).when(command).validateStoragePool();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateWithDirectLunIdAndInvalidVds() {
        command.getParameters().setDirectLunId(Guid.newGuid());
        when(hostValidator.hostExists()).thenReturn(new ValidationResult(EngineMessage.VDS_INVALID_SERVER_ID));
        runAndAssertValidateFailure(command, EngineMessage.VDS_INVALID_SERVER_ID);
    }

    @Test
    public void validateWithDirectLunIdWhenVdsIsNotUp() {
        command.getParameters().setDirectLunId(Guid.newGuid());
        when(hostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(hostValidator.isUp())
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL));
        runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
    }

    @Test
    public void validateWithRandomDirectLunId() {
        command.getParameters().setDirectLunId(Guid.newGuid());
        when(hostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(hostValidator.isUp()).thenReturn(ValidationResult.VALID);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void validateWithValidDirectLunId() {
        command.getParameters().setDirectLunId(lun1.getDiskId());
        when(diskLunMapDao.getDiskLunMapByDiskId(any())).thenReturn(disk1Lun1Map);
        command.getParameters().setDirectLunId(Guid.newGuid());
        when(hostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(hostValidator.isUp()).thenReturn(ValidationResult.VALID);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private void mockLunToDiskIdsOfDirectLunsAttachedToVmsInPool(LUNs... luns) {
        doReturn(Arrays.stream(luns).collect(Collectors.toMap(LUNs::getLUNId, LUNs::getDiskId)))
                .when(command).getLunToDiskIdsOfDirectLunsAttachedToVmsInPool();
    }
}
