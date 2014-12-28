package org.ovirt.engine.core.bll.storage;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map_id;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AttachStorageServerConnectionToStorageDomainCommandTest {
    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    private AttachStorageConnectionToStorageDomainCommand<AttachDetachStorageConnectionParameters> command = null;

    private StorageConnectionValidator validator = null;

    private StorageDomain domain = null;

    @Mock
    LunDAO lunDao;

    @Mock
    StorageServerConnectionDAO connectionDAO;

    @Mock
    StorageServerConnectionLunMapDAO lunMapDAO;

    @Before
    public void init() {
        Guid connectionId = Guid.newGuid();
        Guid domainId = Guid.newGuid();
        AttachDetachStorageConnectionParameters parameters = new AttachDetachStorageConnectionParameters();
        parameters.setStorageConnectionId(connectionId.toString());
        parameters.setStorageDomainId(domainId);
        validator = mock(StorageConnectionValidator.class);
        command = spy(new AttachStorageConnectionToStorageDomainCommand<AttachDetachStorageConnectionParameters>(parameters));
        doReturn(validator).when(command).createStorageConnectionValidator();
        doReturn(lunDao).when(command).getLunDao();
        doReturn(connectionDAO).when(command).getStorageServerConnectionDAO();
        doReturn(lunMapDAO).when(command).getStorageServerConnectionLunMapDao();
        domain = new StorageDomain();
        domain.setId(domainId);
        domain.setStorageDomainType(StorageDomainType.Data);
        domain.setStatus(StorageDomainStatus.Maintenance);
        domain.setStorageType(StorageType.ISCSI);
        doReturn(domain).when(command).getStorageDomain();
    }

    @Test
    public void canDoActionSuccess() {
        when(validator.isConnectionExists()).thenReturn(ValidationResult.VALID);
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.FALSE);
        when(validator.isISCSIConnectionAndDomain(domain)).thenReturn(ValidationResult.VALID);
        when(validator.isDomainOfConnectionExistsAndInactive(domain)).thenReturn(ValidationResult.VALID);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void canDoActionFailure() {
        when(validator.isConnectionExists()).thenReturn(ValidationResult.VALID);
        when(validator.isConnectionForISCSIDomainAttached(domain)).thenReturn(Boolean.TRUE);
        when(validator.isISCSIConnectionAndDomain(domain)).thenReturn(ValidationResult.VALID);
        when(validator.isDomainOfConnectionExistsAndInactive(domain)).thenReturn(ValidationResult.VALID);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_ALREADY_EXISTS);
    }

    @Test
    public void canDoActionFailureNotExists() {
        ValidationResult result = new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        when(validator.isConnectionExists()).thenReturn(result);
        when(validator.isISCSIConnectionAndDomain(domain)).thenReturn(ValidationResult.VALID);
        when(validator.isDomainOfConnectionExistsAndInactive(domain)).thenReturn(ValidationResult.VALID);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void executeCommandNotFirstDummyLun() {
       LUNs dummyLun = new LUNs();
       dummyLun.setLUN_id(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX + domain.getId());
       when(lunDao.get(dummyLun.getLUN_id())).thenReturn(dummyLun);

       List<StorageServerConnections> connectionsForDomain = new ArrayList<>();
       StorageServerConnections connection = new StorageServerConnections();
       connection.setid(Guid.newGuid().toString());
       connection.setstorage_type(StorageType.ISCSI);
       connection.setiqn("iqn.1.2.3.4.com");
       connection.setconnection("123.345.266.255");
       connectionsForDomain.add(connection);
       when(connectionDAO.getAllForDomain(domain.getId())).thenReturn(connectionsForDomain);
       LUN_storage_server_connection_map_id map_id = new LUN_storage_server_connection_map_id(dummyLun.getLUN_id(), connection.getid());
       when(lunMapDAO.get(map_id)).thenReturn(null);
       //dummy lun already exists, thus no need to save
       verify(lunDao, never()).save(dummyLun);
       verify(lunMapDAO, never()).save(new LUN_storage_server_connection_map());
       command.executeCommand();
       CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void executeCommandFirstDummyLun() {
       LUNs dummyLun = new LUNs();
       dummyLun.setLUN_id(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX + domain.getId());
       when(lunDao.get(dummyLun.getLUN_id())).thenReturn(null);
       doNothing().when(lunDao).save(dummyLun);
       List<StorageServerConnections> connectionsForDomain = new ArrayList<>();
       StorageServerConnections connection = new StorageServerConnections();
       connection.setid(Guid.newGuid().toString());
       connection.setstorage_type(StorageType.ISCSI);
       connection.setiqn("iqn.1.2.3.4.com");
       connection.setconnection("123.345.266.255");
       connectionsForDomain.add(connection);
       when(connectionDAO.getAllForDomain(domain.getId())).thenReturn(connectionsForDomain);
       LUN_storage_server_connection_map_id map_id = new LUN_storage_server_connection_map_id(dummyLun.getLUN_id(), connection.getid());
       when(lunMapDAO.get(map_id)).thenReturn(null);
       LUN_storage_server_connection_map map = new LUN_storage_server_connection_map();
       doNothing().when(lunMapDAO).save(map);
       command.executeCommand();
       CommandAssertUtils.checkSucceeded(command, true);
    }

}
