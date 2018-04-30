package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class StorageConnectionValidatorTest {
    private StorageServerConnections connection;
    private StorageConnectionValidator validator;
    private StorageDomain domain;

    private static final String CONNECTION_ID_FOR_VALIDATION = Guid.newGuid().toString();
    private static final int NUMBER_OF_EXISTING_CONNECTIONS = 3;

    @BeforeEach
    public void setUp() {
        connection = new StorageServerConnections();
        connection.setId(CONNECTION_ID_FOR_VALIDATION);
        connection.setStorageType(StorageType.ISCSI);
        validator = spy(new StorageConnectionValidator(connection));
        domain = new StorageDomain();
        domain.setStorageType(StorageType.ISCSI);
        domain.setId(Guid.newGuid());
        domain.setStatus(StorageDomainStatus.Maintenance);
        domain.setStorageDomainSharedStatus(StorageDomainSharedStatus.Inactive);
    }

    @Test
    public void isConnectionNotExists() {
        validator = new StorageConnectionValidator(null);
        assertThat(validator.isConnectionExists(), failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST));
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
        assertThat(validator.isSameStorageType(domain),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_NOT_SAME_STORAGE_TYPE));
    }

    @Test
    public void isNotISCSIConnectionAndDomain() {
        connection.setStorageType(StorageType.NFS);
        domain.setStorageType(StorageType.NFS);
        assertThat(validator.isISCSIConnectionAndDomain(domain),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_ACTION_IS_SUPPORTED_ONLY_FOR_ISCSI_DOMAINS));
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
        connection.setId(CONNECTION_ID_FOR_VALIDATION);
        List<StorageServerConnections> connections = getConnections();
        connections.add(connection);
        doReturn(connections).when(validator).getAllConnectionsForDomain(domain.getId());
        assertTrue(validator.isConnectionForISCSIDomainAttached(domain));
    }

    private List<StorageServerConnections> getConnections() {
        return Stream.generate(StorageServerConnections::new)
                .limit(NUMBER_OF_EXISTING_CONNECTIONS)
                .peek(conn -> conn.setId(Guid.newGuid().toString()))
                .collect(Collectors.toList());
    }
}
