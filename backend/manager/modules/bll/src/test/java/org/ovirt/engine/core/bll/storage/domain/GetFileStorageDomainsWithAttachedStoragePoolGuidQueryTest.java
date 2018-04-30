package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetFileStorageDomainsWithAttachedStoragePoolGuidQueryTest
        extends AbstractGetStorageDomainsWithAttachedStoragePoolGuidQueryTestCase
        <StorageDomainsAndStoragePoolIdQueryParameters, GetFileStorageDomainsWithAttachedStoragePoolGuidQuery<StorageDomainsAndStoragePoolIdQueryParameters>> {

    @Override
    protected StorageType getStorageType() {
        return StorageType.NFS;
    }

    @Test
    public void testNullStorageDomainListQuery() {
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        mockStoragePoolDao(storagePool);

        // Create parameters
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(null);

        // Run 'HSMGetStorageDomainInfo' command
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);

        Pair<StorageDomainStatic, Guid> storageDomainToPoolId =
                new Pair<>(storageDomain.getStorageStaticData(), Guid.newGuid());
        returnValue.setReturnValue(storageDomainToPoolId);
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.HSMGetStorageDomainInfo), any()))
                .thenReturn(returnValue);

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testFetchUsingStorageServerConnectionWithEmptyListRetrieved() {
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        mockStoragePoolDao(storagePool);

        // Create parameters
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(null);
        StorageServerConnections storageServerConnections = new StorageServerConnections();
        when(paramsMock.getStorageServerConnection()).thenReturn(storageServerConnections);

        List<StorageDomain> storageDomains = new ArrayList<>();
        QueryReturnValue vdcReturnValue = new QueryReturnValue();
        vdcReturnValue.setSucceeded(true);
        vdcReturnValue.setReturnValue(storageDomains);
        doReturn(vdcReturnValue).when(getQuery()).getExistingStorageDomainList(eq(storageServerConnections));

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testFetchUsingStorageServerConnectionWithFailedInternalQuery() {
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        mockStoragePoolDao(storagePool);

        // Create parameters
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(null);
        StorageServerConnections storageServerConnections = new StorageServerConnections();
        when(paramsMock.getStorageServerConnection()).thenReturn(storageServerConnections);

        QueryReturnValue vdcReturnValue = new QueryReturnValue();
        vdcReturnValue.setSucceeded(false);
        vdcReturnValue.setReturnValue(null);
        doReturn(vdcReturnValue).when(getQuery()).getExistingStorageDomainList(eq(storageServerConnections));

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testFetchUsingStorageServerConnectionWithAttachedStorageDomain() {
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        mockStoragePoolDao(storagePool);

        // Create parameters
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(null);
        StorageServerConnections storageServerConnection = new StorageServerConnections();
        when(paramsMock.getStorageServerConnection()).thenReturn(storageServerConnection);

        StorageDomain mockedStorageDomain = new StorageDomain();
        mockedStorageDomain.setStorageStaticData(new StorageDomainStatic());
        mockedStorageDomain.setStoragePoolId(Guid.newGuid());

        List<StorageDomain> storageDomains = new ArrayList<>();
        storageDomains.add(mockedStorageDomain);
        QueryReturnValue vdcReturnValue = new QueryReturnValue();
        vdcReturnValue.setSucceeded(true);
        vdcReturnValue.setReturnValue(storageDomains);
        doReturn(vdcReturnValue).when(getQuery()).getExistingStorageDomainList(eq(storageServerConnection));

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        returnedStorageDomainList.add(mockedStorageDomain.getStorageStaticData());
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testFetchUsingStorageServerConnectionWithUnattachedStorageDomain() {
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        mockStoragePoolDao(storagePool);

        // Create parameters
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(null);
        StorageServerConnections storageServerConnections = new StorageServerConnections();
        when(paramsMock.getStorageServerConnection()).thenReturn(storageServerConnections);

        StorageDomain mockedStorageDomain = new StorageDomain();
        mockedStorageDomain.setStorageStaticData(new StorageDomainStatic());

        List<StorageDomain> storageDomains = new ArrayList<>();
        storageDomains.add(mockedStorageDomain);
        QueryReturnValue vdcReturnValue = new QueryReturnValue();
        vdcReturnValue.setSucceeded(true);
        vdcReturnValue.setReturnValue(storageDomains);
        doReturn(vdcReturnValue).when(getQuery()).getExistingStorageDomainList(eq(storageServerConnections));

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

}
