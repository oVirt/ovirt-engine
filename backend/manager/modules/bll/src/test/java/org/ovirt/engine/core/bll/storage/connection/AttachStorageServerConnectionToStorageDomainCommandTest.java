package org.ovirt.engine.core.bll.storage.connection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AttachStorageServerConnectionToStorageDomainCommandTest extends BaseCommandTest {
    @Spy
    @InjectMocks
    private AttachStorageConnectionToStorageDomainCommand<AttachDetachStorageConnectionParameters> command = getCommand();

    private StorageConnectionValidator validator = null;

    private StorageDomain domain = null;

    @Mock
    LunDao lunDao;

    @Mock
    StorageServerConnectionLunMapDao lunMapDao;

    private static AttachStorageConnectionToStorageDomainCommand<AttachDetachStorageConnectionParameters> getCommand() {
        Guid connectionId = Guid.newGuid();
        Guid domainId = Guid.newGuid();

        AttachDetachStorageConnectionParameters parameters = new AttachDetachStorageConnectionParameters();
        parameters.setStorageConnectionId(connectionId.toString());
        parameters.setStorageDomainId(domainId);

        return new AttachStorageConnectionToStorageDomainCommand<>(parameters, null);
    }

    @BeforeEach
    public void init() {
        validator = mock(StorageConnectionValidator.class);
        doReturn(validator).when(command).createStorageConnectionValidator();
        domain = new StorageDomain();
        domain.setId(command.getParameters().getStorageDomainId());
        domain.setStorageDomainType(StorageDomainType.Data);
        domain.setStatus(StorageDomainStatus.Maintenance);
        domain.setStorageType(StorageType.ISCSI);
        doReturn(domain).when(command).getStorageDomain();
        // A default storage domain is expected to have discovered LUNs; the dedicated test below
        // overrides this to exercise the empty-LUN validation path.
        when(lunDao.getAllForVolumeGroup(any())).thenReturn(Collections.singletonList(new LUNs()));
    }

    @Test
    public void validateSuccess() {
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.FALSE);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailure() {
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.TRUE);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_ALREADY_EXISTS);
    }

    @Test
    public void validateFailureNotExists() {
        ValidationResult result = new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        when(validator.isConnectionExists()).thenReturn(result);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void executeCommandMapsConnectionToRealLuns() {
        LUNs lun1 = new LUNs();
        lun1.setLUNId("lun1");
        LUNs lun2 = new LUNs();
        lun2.setLUNId("lun2");
        when(lunDao.getAllForVolumeGroup(any())).thenReturn(Arrays.asList(lun1, lun2));

        command.executeCommand();

        verify(lunMapDao, times(2)).save(any(LUNStorageServerConnectionMap.class));
    }

    @Test
    public void executeCommandSkipsLunsAlreadyMappedToConnection() {
        LUNs lun1 = new LUNs();
        lun1.setLUNId("lun1");
        when(lunDao.getAllForVolumeGroup(any())).thenReturn(Collections.singletonList(lun1));
        when(lunMapDao.get(any(LUNStorageServerConnectionMapId.class)))
                .thenReturn(new LUNStorageServerConnectionMap());

        command.executeCommand();

        verify(lunMapDao, never()).save(any(LUNStorageServerConnectionMap.class));
    }

    @Test
    public void validateFailsWhenDomainHasNoDiscoveredLuns() {
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.FALSE);
        when(lunDao.getAllForVolumeGroup(any())).thenReturn(Collections.emptyList());

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_HAS_NO_LUNS);
    }

}
