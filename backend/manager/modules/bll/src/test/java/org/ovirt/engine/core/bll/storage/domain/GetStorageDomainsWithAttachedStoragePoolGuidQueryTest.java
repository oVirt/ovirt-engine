package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetStorageDomainsWithAttachedStoragePoolGuidQueryTest
        extends AbstractGetStorageDomainsWithAttachedStoragePoolGuidQueryTestCase
        <StorageDomainsAndStoragePoolIdQueryParameters, GetStorageDomainsWithAttachedStoragePoolGuidQuery<StorageDomainsAndStoragePoolIdQueryParameters>> {

    @Mock
    private StorageDomainDao storageDomainDaoMock;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(storageDomainDaoMock.getHostedEngineStorageDomainIds()).thenReturn(Collections.emptyList());

        doReturn(Boolean.TRUE).when(getQuery()).connectStorageDomain(eq(storageDomain));
        doReturn(Boolean.TRUE).when(getQuery()).disconnectStorageDomain(eq(storageDomain));
    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.ISCSI;
    }


    @Test
    public void testUninitializedStoragePool() {
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Uninitialized);
        mockStoragePoolDao(storagePool);

        // Create parameters
        List<StorageDomain> storageDomainList = new ArrayList<>();
        storageDomainList.add(storageDomain);
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(storageDomainList);
        when(paramsMock.isCheckStoragePoolStatus()).thenReturn(Boolean.TRUE);

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
    public void testStoragePoolIdReturnNullStoragePool() {
        mockStoragePoolDao(null);

        // Create parameters
        List<StorageDomain> storageDomainList = new ArrayList<>();
        storageDomainList.add(storageDomain);
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(storageDomainList);
        when(paramsMock.isCheckStoragePoolStatus()).thenReturn(Boolean.TRUE);

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
    public void testStoragePoolIsUninitializedButCheckBooleanIsFalse() {
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Uninitialized);
        mockStoragePoolDao(storagePool);

        // Create parameters
        List<StorageDomain> storageDomainList = new ArrayList<>();
        storageDomainList.add(storageDomain);
        StorageDomainsAndStoragePoolIdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getStorageDomainList()).thenReturn(storageDomainList);
        when(paramsMock.isCheckStoragePoolStatus()).thenReturn(Boolean.FALSE);

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
}
