package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

@RunWith(MockitoJUnitRunner.class)
public class StorageConnectionValidatorTest {
    private StorageServerConnections connection;
    private StorageConnectionValidator validator;
    private StorageDomain domain;
    @Mock
    protected StorageServerConnectionDao storageServerConnectionDao;

    @Before
    public void setUp() {
        connection = new StorageServerConnections();
        connection.setId("0cc146e8-e5ed-482c-8814-270bc48c297e");
        connection.setStorageType(StorageType.ISCSI);
        validator = spy(new StorageConnectionValidator(connection));
        domain = new StorageDomain();
        domain.setStorageType(StorageType.ISCSI);
        domain.setId(Guid.createGuidFromString("72e3a666-89e1-4005-a7ca-f7548004a9ab"));
        domain.setStatus(StorageDomainStatus.Maintenance);
        domain.setStorageDomainSharedStatus(StorageDomainSharedStatus.Inactive);
    }

    @Test
    public void isConnectionNotExists() {
        validator = new StorageConnectionValidator(null);
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST,
                validator.isConnectionExists().getMessage());
    }

    @Test
    public void isConnectionExists() {
        validator = new StorageConnectionValidator(connection);
        assertTrue(validator.isConnectionExists().isValid());
    }

    @Test
    public void isSameStorageType() {
        domain.setStorageType(StorageType.ISCSI);
        assertTrue(validator.isSameStorageType(domain).isValid());
    }

    @Test
    public void isNotSameStorageType() {
        domain.setStorageType(StorageType.NFS);
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_NOT_SAME_STORAGE_TYPE,
                validator.isSameStorageType(domain).getMessage());
    }

    @Test
    public void isNotISCSIConnectionAndDomain() {
        connection.setStorageType(StorageType.NFS);
        domain.setStorageType(StorageType.NFS);
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_ACTION_IS_SUPPORTED_ONLY_FOR_ISCSI_DOMAINS,
                validator.isISCSIConnectionAndDomain(domain).getMessage());
    }

    @Test
    public void isISCSIConnectionAndDomain() {
        connection.setStorageType(StorageType.ISCSI);
        domain.setStorageType(StorageType.ISCSI);
        assertTrue(validator.isISCSIConnectionAndDomain(domain).isValid());
    }

    @Test
    public void isDomainOfConnectionExistsAndNonActive() {
        domain.setStorageType(StorageType.ISCSI);
        assertTrue(validator.isDomainOfConnectionExistsAndInactive(domain).isValid());
    }

    @Test
    public void isDomainOfConnectionExistsAndActive() {
        domain.setStorageType(StorageType.ISCSI);
        domain.setStatus(StorageDomainStatus.Active);
        domain.setStorageDomainSharedStatus(StorageDomainSharedStatus.Active);
        assertFalse(validator.isDomainOfConnectionExistsAndInactive(domain).isValid());
    }

    @Test
    public void isConnectionForISCSIDomainNotAttached() {
        doReturn(getConnections()).when(validator).getAllConnectionsForDomain(domain.getId());
        assertFalse(validator.isConnectionForISCSIDomainAttached(domain));
    }

    @Test
    public void isConnectionForISCSIDomainAttached() {
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId("0cc146e8-e5ed-482c-8814-270bc48c297e");
        List<StorageServerConnections> connections = getConnections();
        connections.add(connection);
        doReturn(connections).when(validator).getAllConnectionsForDomain(domain.getId());
        assertTrue(validator.isConnectionForISCSIDomainAttached(domain));
    }

    private List<StorageServerConnections> getConnections() {
         List<StorageServerConnections> connectionsList = new ArrayList<>();
         StorageServerConnections connection1 = new StorageServerConnections();
         connection1.setId("1cc146e8-e5ed-482c-8814-270bc48c2981");
         StorageServerConnections connection2 = new StorageServerConnections();
         connection2.setId("2cc146e8-e5ed-482c-8814-270bc48c2981");
         StorageServerConnections connection3 = new StorageServerConnections();
         connection3.setId("3cc146e8-e5ed-482c-8814-270bc48c2981");
         connectionsList.add(connection1);
         connectionsList.add(connection2);
         connectionsList.add(connection3);
         return connectionsList;
    }
}
