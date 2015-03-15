package org.ovirt.engine.core.bll.storage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetUnregisteredBlockStorageDomainsQueryTest extends
        AbstractQueryTest<GetUnregisteredBlockStorageDomainsParameters, GetUnregisteredBlockStorageDomainsQuery<GetUnregisteredBlockStorageDomainsParameters>> {

    private final String connectionIqn1 = Guid.newGuid().toString();
    private final String connectionIqn2 = Guid.newGuid().toString();
    private final String vgId = Guid.newGuid().toString();
    private final String existingVgId = Guid.newGuid().toString();
    private final Guid storageDomainId = Guid.newGuid();
    private final Guid existingStorageDomainId = Guid.newGuid();

    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private LunDAO lunDAO;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        prepareMocks();
    }

    private void prepareMocks() {
        VDSBrokerFrontend vdsBrokerFrontendMock = mock(VDSBrokerFrontend.class);
        doReturn(vdsBrokerFrontendMock).when(getQuery()).getVdsBroker();

        BackendInternal backendMock = mock(BackendInternal.class);
        doReturn(backendMock).when(getQuery()).getBackend();

        doReturn(storageDomainDAO).when(getQuery()).getStorageDomainDAO();
        doReturn(getExistingStorageDomains()).when(storageDomainDAO).getAll();
        doReturn(lunDAO).when(getQuery()).getLunDAO();
    }

    @Test
    public void testIscsiFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.ISCSI);
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(storageDomainId, vgId);
        doReturn(Collections.emptyList()).when(lunDAO).getAll();

        doReturn(createSuccessVdcReturnValue()).when(getQuery()).
                executeConnectStorageToVds(any(StorageServerConnectionParametersBase.class));

        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).
                executeGetDeviceList(any(GetDeviceListQueryParameters.class));

        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).
                executeGetVGInfo(any(GetVGInfoVDSCommandParameters.class));

        doReturn(createGetStorageDomainInfoReturnValue(storageDomainId)).when(getQuery()).
                executeHSMGetStorageDomainInfo(any(HSMGetStorageDomainInfoVDSCommandParameters.class));

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(storageDomains.size(), 1);
        assertEquals(storageDomains.get(0).getId(), storageDomainId);

        List<StorageServerConnections> connections = returnValue.getSecond();
        assertEquals(connections.size(), 2);
    }

    @Test
    public void testIscsiExternalLunDiskPartOfUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.ISCSI);
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(storageDomainId, vgId);
        List<LUNs> externalLuns = new ArrayList<>();
        externalLuns.add(luns.get(1));
        doReturn(luns).when(lunDAO).getAll();

        doReturn(createSuccessVdcReturnValue()).when(getQuery()).
                executeConnectStorageToVds(any(StorageServerConnectionParametersBase.class));

        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).
                executeGetDeviceList(any(GetDeviceListQueryParameters.class));

        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).
                executeGetVGInfo(any(GetVGInfoVDSCommandParameters.class));

        doReturn(createGetStorageDomainInfoReturnValue(storageDomainId)).when(getQuery()).
                executeHSMGetStorageDomainInfo(any(HSMGetStorageDomainInfoVDSCommandParameters.class));

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(storageDomains.size(), 0);
    }

    @Test
    public void testIscsiNotFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.ISCSI);
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(existingStorageDomainId, existingVgId);

        doReturn(Collections.emptyList()).when(lunDAO).getAll();
        doReturn(createSuccessVdcReturnValue()).when(getQuery()).
                executeConnectStorageToVds(any(StorageServerConnectionParametersBase.class));

        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).
                executeGetDeviceList(any(GetDeviceListQueryParameters.class));

        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).
                executeGetVGInfo(any(GetVGInfoVDSCommandParameters.class));

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(storageDomains.size(), 0);

        List<StorageServerConnections> connections = returnValue.getSecond();
        assertEquals(connections.size(), 2);
    }

    @Test
    public void testFcpFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.FCP);
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(storageDomainId, vgId);

        doReturn(Collections.emptyList()).when(lunDAO).getAll();
        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).
                executeGetDeviceList(any(GetDeviceListQueryParameters.class));

        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).
                executeGetVGInfo(any(GetVGInfoVDSCommandParameters.class));

        doReturn(createGetStorageDomainInfoReturnValue(storageDomainId)).when(getQuery()).
                executeHSMGetStorageDomainInfo(any(HSMGetStorageDomainInfoVDSCommandParameters.class));

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(storageDomains.size(), 1);
        assertEquals(storageDomains.get(0).getId(), storageDomainId);
    }

    @Test
    public void testFcpNotFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.FCP);
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());
        List<LUNs> luns = getLUNs(existingStorageDomainId, existingVgId);

        doReturn(Collections.emptyList()).when(lunDAO).getAll();
        doReturn(createSuccessVdcReturnValue()).when(getQuery()).
                executeConnectStorageToVds(any(StorageServerConnectionParametersBase.class));

        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).
                executeGetDeviceList(any(GetDeviceListQueryParameters.class));

        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).
                executeGetVGInfo(any(GetVGInfoVDSCommandParameters.class));

        // Execute query
        getQuery().executeQueryCommand();

        // Assert query's results
        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue =
                getQuery().getQueryReturnValue().getReturnValue();

        List<StorageDomain> storageDomains = returnValue.getFirst();
        assertEquals(storageDomains.size(), 0);
    }

    private List<StorageServerConnections> getConnections() {
        StorageServerConnections connection1 = new StorageServerConnections();
        connection1.setiqn(connectionIqn1);

        StorageServerConnections connection2 = new StorageServerConnections();
        connection2.setiqn(connectionIqn2);

        return new ArrayList<>(Arrays.asList(connection1, connection2));
    }

    private List<LUNs> getLUNs(Guid storageDomainId, String vgId) {
        LUNs lun1 = new LUNs();
        lun1.setStorageDomainId(storageDomainId);
        lun1.setvolume_group_id(vgId);
        lun1.setLunConnections(new ArrayList<>(getConnections()));

        LUNs lun2 = new LUNs();
        lun2.setStorageDomainId(storageDomainId);
        lun2.setvolume_group_id(vgId);
        lun2.setLunConnections(new ArrayList<>(getConnections()));

        return new ArrayList<>(Arrays.asList(lun1, lun2));
    }

    private List<StorageDomain> getExistingStorageDomains() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(existingStorageDomainId);

        return Collections.singletonList(storageDomain);
    }

    private VdcReturnValueBase createSuccessVdcReturnValue() {
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setSucceeded(true);

        return returnValue;
    }

    private VdcQueryReturnValue createGetDeviceListReturnValue(List<LUNs> luns) {
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(luns);

        return returnValue;
    }

    private VDSReturnValue createGetVGInfoReturnValue(List<LUNs> luns) {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(luns);

        return returnValue;
    }

    private VDSReturnValue createGetStorageDomainInfoReturnValue(Guid storageDomainId) {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);

        Pair<StorageDomainStatic, Object> pair = new Pair<>(storageDomain.getStorageStaticData(), null);
        returnValue.setReturnValue(pair);

        return returnValue;
    }
}
