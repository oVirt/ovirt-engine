package org.ovirt.engine.core.bll.storage.connection;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
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
    StorageServerConnectionDao connectionDao;

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
    public void executeCommandNotFirstDummyLun() {
       LUNs dummyLun = new LUNs();
       dummyLun.setLUNId(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX + domain.getId());
       when(lunDao.get(dummyLun.getLUNId())).thenReturn(dummyLun);

       List<StorageServerConnections> connectionsForDomain = new ArrayList<>();
       StorageServerConnections connection = new StorageServerConnections();
       connection.setId(Guid.newGuid().toString());
       connection.setStorageType(StorageType.ISCSI);
       connection.setIqn("iqn.1.2.3.4.com");
       connection.setConnection("123.345.266.255");
       connectionsForDomain.add(connection);
       when(connectionDao.getAllForDomain(domain.getId())).thenReturn(connectionsForDomain);
       //dummy lun already exists, thus no need to save
       verify(lunDao, never()).save(dummyLun);
       verify(lunMapDao, never()).save(new LUNStorageServerConnectionMap());
       command.executeCommand();
       CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void executeCommandFirstDummyLun() {
       List<StorageServerConnections> connectionsForDomain = new ArrayList<>();
       StorageServerConnections connection = new StorageServerConnections();
       connection.setId(Guid.newGuid().toString());
       connection.setStorageType(StorageType.ISCSI);
       connection.setIqn("iqn.1.2.3.4.com");
       connection.setConnection("123.345.266.255");
       connectionsForDomain.add(connection);
       when(connectionDao.getAllForDomain(domain.getId())).thenReturn(connectionsForDomain);
       command.executeCommand();
       CommandAssertUtils.checkSucceeded(command, true);
    }

}
