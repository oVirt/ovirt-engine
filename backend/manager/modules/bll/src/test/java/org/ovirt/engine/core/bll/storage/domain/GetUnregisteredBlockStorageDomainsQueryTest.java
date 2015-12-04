package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;

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
    private StorageDomainDao storageDomainDao;

    @Mock
    private LunDao lunDao;

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

        doReturn(storageDomainDao).when(getQuery()).getStorageDomainDao();
        doReturn(getExistingStorageDomains()).when(storageDomainDao).getAll();
        doReturn(lunDao).when(getQuery()).getLunDao();
    }

    @Test
    public void testIscsiFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.ISCSI);
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(storageDomainId, vgId);
        doReturn(Collections.emptyList()).when(lunDao).getAll();

        doReturn(createSuccessVdcReturnValue()).when(getQuery()).
                executeConnectStorageToVds(any(StorageServerConnectionParametersBase.class));

        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).
                executeGetDeviceList(any(GetDeviceListQueryParameters.class));

        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).
                executeGetVGInfo(any(GetVGInfoVDSCommandParameters.class));

        doReturn(createGetStorageDomainInfoReturnValue()).when(getQuery()).
                executeHSMGetStorageDomainInfo(any(HSMGetStorageDomainInfoVDSCommandParameters.class));

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

        doReturn(createSuccessVdcReturnValue()).when(getQuery()).
                executeConnectStorageToVds(any(StorageServerConnectionParametersBase.class));

        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).
                executeGetDeviceList(any(GetDeviceListQueryParameters.class));

        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).
                executeGetVGInfo(any(GetVGInfoVDSCommandParameters.class));

        doReturn(createGetStorageDomainInfoReturnValue()).when(getQuery()).
                executeHSMGetStorageDomainInfo(any(HSMGetStorageDomainInfoVDSCommandParameters.class));

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

        doReturn(Collections.emptyList()).when(lunDao).getAll();
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
        assertEquals(0, storageDomains.size());

        List<StorageServerConnections> connections = returnValue.getSecond();
        assertEquals(2, connections.size());
    }

    @Test
    public void testFcpFoundUnregisteredDomain() {
        when(getQueryParameters().getStorageType()).thenReturn(StorageType.FCP);
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());

        List<LUNs> luns = getLUNs(storageDomainId, vgId);

        doReturn(Collections.emptyList()).when(lunDao).getAll();
        doReturn(createGetDeviceListReturnValue(luns)).when(getQuery()).
                executeGetDeviceList(any(GetDeviceListQueryParameters.class));

        doReturn(createGetVGInfoReturnValue(luns)).when(getQuery()).
                executeGetVGInfo(any(GetVGInfoVDSCommandParameters.class));

        doReturn(createGetStorageDomainInfoReturnValue()).when(getQuery()).
                executeHSMGetStorageDomainInfo(any(HSMGetStorageDomainInfoVDSCommandParameters.class));

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
        when(getQueryParameters().getStorageServerConnections()).thenReturn(getConnections());
        when(getQueryParameters().getVdsId()).thenReturn(Guid.newGuid());
        List<LUNs> luns = getLUNs(existingStorageDomainId, existingVgId);

        doReturn(Collections.emptyList()).when(lunDao).getAll();
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
        lun1.setLunConnections(new ArrayList<>(getConnections()));

        LUNs lun2 = new LUNs();
        lun2.setStorageDomainId(sdId);
        lun2.setVolumeGroupId(vgId);
        lun2.setLunConnections(new ArrayList<>(getConnections()));

        return new ArrayList<>(Arrays.asList(lun1, lun2));
    }

    private List<StorageDomain> getExistingStorageDomains() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(existingStorageDomainId);

        return Collections.singletonList(storageDomain);
    }

    private static VdcReturnValueBase createSuccessVdcReturnValue() {
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setSucceeded(true);

        return returnValue;
    }

    private static VdcQueryReturnValue createGetDeviceListReturnValue(List<LUNs> luns) {
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
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
