package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class GetGeoRepSessionsForStorageDomainQueryTest extends AbstractQueryTest<IdQueryParameters, GetGeoRepSessionsForStorageDomainQuery<IdQueryParameters>> {

    @Mock
    GlusterBrickDao glusterBrickDaoMock;
    @Mock
    StorageDomainStaticDao storageDomainDao;
    @Mock
    StorageServerConnectionDao storageServerConnectionDao;
    @Mock
    VdsDao vdsDao;
    @Mock
    InterfaceDao interfaceDao;
    @Mock
    GlusterGeoRepDao glusterGeoRepDao;
    @Mock
    GlusterVolumeDao glusterVolumeDao;

    Guid volId = Guid.newGuid();
    Guid clusterId = Guid.newGuid();

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    private void setupMock1() {
        doReturn(getStorageDomain()).when(storageDomainDao).get(any());
        doReturn(getStorageServerConn(volId)).when(storageServerConnectionDao).get(any());
        doReturn(getSessions()).when(glusterGeoRepDao).getGeoRepSessions(volId);
    }

    private void setupMock2() {
        doReturn("serverx").when(getQuery()).resolveHostName("serverx");
        doReturn(getStorageDomain()).when(storageDomainDao).get(any());
        doReturn(getStorageServerConn(null)).when(storageServerConnectionDao).get(any());
        doReturn(getSessions()).when(glusterGeoRepDao).getGeoRepSessions(volId);
        doReturn(getVdsList()).when(vdsDao).getAll();
        doReturn(getInterfaceList()).when(interfaceDao).getAllInterfacesForVds(any(Guid.class));
        GlusterVolumeEntity gv = new GlusterVolumeEntity();
        gv.setId(volId);
        doReturn(gv).when(glusterVolumeDao).getByName(clusterId, "volumey");
    }

    private List<VdsNetworkInterface> getInterfaceList() {
        List<VdsNetworkInterface> vdsNI = new ArrayList<>();
        VdsNetworkInterface vdsNI1 = new VdsNetworkInterface();
        vdsNI1.setIpv4Address("serverx");
        vdsNI.add(vdsNI1);
        VdsNetworkInterface vdsNI2 = new VdsNetworkInterface();
        vdsNI.add(vdsNI2);
        return vdsNI;
    }

    private StorageDomainStatic getStorageDomain() {
        StorageDomainStatic st = new StorageDomainStatic();
        st.setStorage("");
        return st;
    }

    private StorageServerConnections getStorageServerConn(Guid volumeId) {
        StorageServerConnections ssc = new StorageServerConnections();
        ssc.setStorageType(StorageType.GLUSTERFS);
        ssc.setGlusterVolumeId(volumeId);
        ssc.setConnection("serverx:/volumey");
        return ssc;
    }

    private List<VDS> getVdsList() {
        List<VDS> vdsList = new ArrayList<>();
        VDS vds1 = new VDS();
        vds1.setId(Guid.newGuid());
        vds1.setClusterId(clusterId);
        vdsList.add(vds1);
        return vdsList;
    }

    private List<GlusterGeoRepSession> getSessions() {
        List<GlusterGeoRepSession> list = new ArrayList<>();
        list.add(new GlusterGeoRepSession());
        return list;
    }

    @Test
    public void testExecuteQueryCommand() {
        setupMock1();
        getQuery().executeQueryCommand();
        List<GlusterGeoRepSession> actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, actual.size());
    }

    @Test
    public void testExecuteQueryCommandNoVol() {
        setupMock2();
        getQuery().executeQueryCommand();
        List<GlusterGeoRepSession> actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, actual.size());
    }
}
