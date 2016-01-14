package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;

public class GetBlockStorageDomainsWithAttachedStoragePoolGuidQueryTest extends
        AbstractQueryTest<StorageDomainsAndStoragePoolIdQueryParameters, GetFileStorageDomainsWithAttachedStoragePoolGuidQuery<StorageDomainsAndStoragePoolIdQueryParameters>> {
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontendMock;

    private StorageDomain storageDomain;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        storageDomain = new StorageDomain();
        storageDomain.setStorageName("Name of Storage");
        storageDomain.setStorageType(StorageType.ISCSI);

        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        VdsDao vdsDaoMock = mock(VdsDao.class);
        List<VDS> listVds = new ArrayList<>();
        listVds.add(vds);
        when(vdsDaoMock.getAllForStoragePoolAndStatus(any(Guid.class), eq(VDSStatus.Up))).thenReturn(listVds);
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDaoMock);
    }

    @Test
    public void testAttachedStorageDomainWithStorageDomainsParameterQuery() {
        mockVdsCommand();
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        mockStoragePoolDao(storagePool);

        // Create parameters
        List<StorageDomain> storageDomainList = new ArrayList<>();
        storageDomainList.add(storageDomain);
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(storageDomainList);

        // Run 'HSMGetStorageDomainInfo' command
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);

        Pair<StorageDomainStatic, Guid> storageDomainToPoolId =
                new Pair<>(storageDomain.getStorageStaticData(), Guid.newGuid());
        returnValue.setReturnValue(storageDomainToPoolId);
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.HSMGetStorageDomainInfo),
                any(HSMGetStorageDomainInfoVDSCommandParameters.class))).thenReturn(returnValue);

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        returnedStorageDomainList.add(storageDomain.getStorageStaticData());
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testUnattachedStorageDomainWithStorageDomainsParameterQuery() {
        mockVdsCommand();
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        mockStoragePoolDao(storagePool);

        // Create parameters
        List<StorageDomain> storageDomainList = new ArrayList<>();
        storageDomainList.add(storageDomain);
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(storageDomainList);

        // Run 'HSMGetStorageDomainInfo' command
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);

        Pair<StorageDomainStatic, Guid> storageDomainToPoolId = new Pair<>(storageDomain.getStorageStaticData(), null);
        returnValue.setReturnValue(storageDomainToPoolId);
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.HSMGetStorageDomainInfo),
                any(HSMGetStorageDomainInfoVDSCommandParameters.class))).thenReturn(returnValue);

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testEmptyStorageDomainListQuery() {
        mockVdsCommand();
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        mockStoragePoolDao(storagePool);

        // Create parameters
        List<StorageDomain> storageDomainList = new ArrayList<>();
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(storageDomainList);

        // Run 'HSMGetStorageDomainInfo' command
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);

        Pair<StorageDomainStatic, Guid> storageDomainToPoolId =
                new Pair<>(storageDomain.getStorageStaticData(), Guid.newGuid());
        returnValue.setReturnValue(storageDomainToPoolId);
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.HSMGetStorageDomainInfo),
                any(HSMGetStorageDomainInfoVDSCommandParameters.class))).thenReturn(returnValue);

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testNullStorageDomainListQuery() {
        mockVdsCommand();
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
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.HSMGetStorageDomainInfo),
                any(HSMGetStorageDomainInfoVDSCommandParameters.class))).thenReturn(returnValue);

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<StorageDomainStatic> returnedStorageDomainList = new ArrayList<>();
        assertEquals(returnedStorageDomainList, getQuery().getQueryReturnValue().getReturnValue());
    }

    private void mockVdsCommand() {
        vdsBrokerFrontendMock = mock(VDSBrokerFrontend.class);
        doReturn(vdsBrokerFrontendMock).when(getQuery()).getVdsBroker();
    }

    private void mockStoragePoolDao(StoragePool storagePool) {
        StoragePoolDao storagePoolDaoMock = mock(StoragePoolDao.class);
        when(getDbFacadeMockInstance().getStoragePoolDao()).thenReturn(storagePoolDaoMock);
        when(storagePoolDaoMock.get(any(Guid.class))).thenReturn(storagePool);
    }
}
