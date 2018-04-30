package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetUnregisteredBlockStorageDomainsQueryTest extends
        AbstractQueryTest<GetUnregisteredBlockStorageDomainsParameters, GetUnregisteredBlockStorageDomainsQuery<GetUnregisteredBlockStorageDomainsParameters>> {

    private final String connectionIqn1 = Guid.newGuid().toString();
    private final String connectionIqn2 = Guid.newGuid().toString();
    private final String vgId = Guid.newGuid().toString();
    private final String existingVgId = Guid.newGuid().toString();
    private final Guid storageDomainId = Guid.newGuid();
    private final Guid existingStorageDomainId = Guid.newGuid();

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private LunDao lunDao;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        doReturn(getExistingStorageDomains()).when(storageDomainDao).getAll();
    }

    @Test
    public void testIscsiFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.ISCSI);
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(storageDomainId, vgId);

        doReturn(createSuccessActionReturnValue()).when(getQuery()).executeConnectStorageToVds(any());
        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).executeGetDeviceList(any());
        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).executeGetVGInfo(any());
        doReturn(createGetStorageDomainInfoReturnValue()).when(getQuery()).executeHSMGetStorageDomainInfo(any());

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(1, storageDomains.size());
        assertEquals(storageDomains.get(0).getId(), storageDomainId);

        List<StorageServerConnections> connections = returnValue.getSecond();
        assertEquals(2, connections.size());
    }

    @Test
    public void testIscsiExternalLunDiskPartOfUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.ISCSI);
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(storageDomainId, vgId);
        doReturn(luns).when(lunDao).getAll();

        doReturn(createSuccessActionReturnValue()).when(getQuery()).executeConnectStorageToVds(any());
        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).executeGetDeviceList(any());
        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).executeGetVGInfo(any());

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(0, storageDomains.size());
    }

    @Test
    public void testIscsiNotFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.ISCSI);
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(existingStorageDomainId, existingVgId);

        doReturn(createSuccessActionReturnValue()).when(getQuery()).executeConnectStorageToVds(any());
        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).executeGetDeviceList(any());

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(0, storageDomains.size());

        List<StorageServerConnections> connections = returnValue.getSecond();
        assertEquals(2, connections.size());
    }

    @Test
    public void testFcpFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.FCP);
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(storageDomainId, vgId);

        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).executeGetDeviceList(any());
        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).executeGetVGInfo(any());
        doReturn(createGetStorageDomainInfoReturnValue()).when(getQuery()).executeHSMGetStorageDomainInfo(any());

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(1, storageDomains.size());
        assertEquals(storageDomains.get(0).getId(), storageDomainId);
    }

    @Test
    public void testFcpNotFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.FCP);
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());
        List<LUNs> luns = getLUNs(existingStorageDomainId, existingVgId);

        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).executeGetDeviceList(any());

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(0, storageDomains.size());
    }

    private List<StorageServerConnections> getConnections() {
        StorageServerConnections connection1 = new StorageServerConnections();
        connection1.setIqn(connectionIqn1);

        StorageServerConnections connection2 = new StorageServerConnections();
        connection2.setIqn(connectionIqn2);

        return new ArrayList<>(Arrays.asList(connection1, connection2));
    }

    private List<LUNs> getLUNs(Guid sdId, String vgId) {
        LUNs lun1 = new LUNs();
        lun1.setStorageDomainId(sdId);
        lun1.setVolumeGroupId(vgId);
        lun1.setLunConnections(getConnections());

        LUNs lun2 = new LUNs();
        lun2.setStorageDomainId(sdId);
        lun2.setVolumeGroupId(vgId);
        lun2.setLunConnections(getConnections());

        return new ArrayList<>(Arrays.asList(lun1, lun2));
    }

    private List<StorageDomain> getExistingStorageDomains() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(existingStorageDomainId);

        return Collections.singletonList(storageDomain);
    }

    private static ActionReturnValue createSuccessActionReturnValue() {
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(true);

        return returnValue;
    }

    private static QueryReturnValue createGetDeviceListReturnValue(List<LUNs> luns) {
        QueryReturnValue returnValue = new QueryReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(luns);

        return returnValue;
    }

    private static VDSReturnValue createGetVGInfoReturnValue(List<LUNs> luns) {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(luns);

        return returnValue;
    }

    private VDSReturnValue createGetStorageDomainInfoReturnValue() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);

        Pair<StorageDomainStatic, Object> pair = new Pair<>(storageDomain.getStorageStaticData(), null);
        returnValue.setReturnValue(pair);

        return returnValue;
    }
}
