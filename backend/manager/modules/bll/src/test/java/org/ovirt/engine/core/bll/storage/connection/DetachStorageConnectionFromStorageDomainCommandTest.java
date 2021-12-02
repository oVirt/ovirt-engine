package org.ovirt.engine.core.bll.storage.connection;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class DetachStorageConnectionFromStorageDomainCommandTest extends BaseCommandTest {
    private StorageConnectionValidator validator = null;

    private StorageDomain domain = null;

    Guid connectionId = Guid.newGuid();
    Guid domainId = Guid.newGuid();

    @Spy
    @InjectMocks
    private DetachStorageConnectionFromStorageDomainCommand<AttachDetachStorageConnectionParameters> command =
            new DetachStorageConnectionFromStorageDomainCommand<>(
                    new AttachDetachStorageConnectionParameters(domainId, connectionId.toString()), null);

    @Mock
    LunDao lunDao;
    @Mock
    StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;

    @BeforeEach
    public void init() {
        validator = mock(StorageConnectionValidator.class);
        doReturn(validator).when(command).createStorageConnectionValidator();
        domain = new StorageDomain();
        domain.setId(domainId);
        domain.setStorageDomainType(StorageDomainType.Data);
        domain.setStatus(StorageDomainStatus.Maintenance);
        domain.setStorageType(StorageType.ISCSI);
        domain.setStorage(Guid.newGuid().toString());
        doReturn(domain).when(command).getStorageDomain();
    }

    @Test
    public void validateSuccess() {
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.TRUE);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailure() {
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.FALSE);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_NOT_EXIST);
    }

    @Test
    public void validateFailureNotExists() {
        ValidationResult result = new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        when(validator.isConnectionExists()).thenReturn(result);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void executeCommand() {
        LUNs lun1 = new LUNs();
        lun1.setLUNId(Guid.newGuid().toString());
        lun1.setStorageDomainId(domain.getId());

        LUNs lun2 = new LUNs();
        lun2.setLUNId(Guid.newGuid().toString());
        lun2.setStorageDomainId(domain.getId());

        LUNs lun3 = new LUNs();
        lun3.setLUNId(Guid.newGuid().toString());
        lun3.setStorageDomainId(domain.getId());

        List<LUNs> lunsForConnection = new ArrayList<>();
        lunsForConnection.add(lun1);
        lunsForConnection.add(lun2);
        lunsForConnection.add(lun3);

        List<LUNs> lunsForVG = new ArrayList<>();
        lunsForVG.add(lun1);
        lunsForVG.add(lun2);

        when(lunDao.getAllForStorageServerConnection(connectionId.toString())).thenReturn(lunsForConnection);
        when(lunDao.getAllForVolumeGroup(domain.getStorage())).thenReturn(lunsForVG);
        when(lunDao.get(lun1.getLUNId())).thenReturn(lun1);
        when(lunDao.get(lun2.getLUNId())).thenReturn(lun2);
        command.executeCommand();
        verify(lunDao, times(1)).remove(lun1.getLUNId());
        verify(lunDao, times(1)).remove(lun2.getLUNId());
        CommandAssertUtils.checkSucceeded(command, true);
    }
}
