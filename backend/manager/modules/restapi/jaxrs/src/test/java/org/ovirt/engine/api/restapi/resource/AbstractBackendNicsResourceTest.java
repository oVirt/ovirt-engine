package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

@Ignore
public class AbstractBackendNicsResourceTest<T extends AbstractBackendReadOnlyDevicesResource<NIC, Nics, VmNetworkInterface>>
        extends AbstractBackendCollectionResourceTest<NIC, VmNetworkInterface, T> {

    protected final static Guid PARENT_ID = GUIDS[1];
    protected static final String[] ADDRESSES = { "10.11.12.13", "13.12.11.10", "10.01.10.01" };

    protected VdcQueryType queryType;
    protected VdcQueryParametersBase queryParams;
    protected String queryIdName;

    public AbstractBackendNicsResourceTest(T collection,
                                           VdcQueryType queryType,
                                           VdcQueryParametersBase queryParams,
                                           String queryIdName) {
        super(collection, null, "");
        this.queryType = queryType;
        this.queryParams = queryParams;
        this.queryIdName = queryIdName;
    }

    @Override
    @Test
    @Ignore
    public void testQuery() throws Exception {
        // skip test inherited from base class as searching
        // over Interfaces is unsupported by the backend
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(queryType,
                                       queryParams.getClass(),
                                       new String[] { queryIdName },
                                       new Object[] { PARENT_ID },
                                       getEntityList(),
                                       failure);
        }
    }

    protected List<VmNetworkInterface> getEntityList() {
        List<VmNetworkInterface> entities = new ArrayList<VmNetworkInterface>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Override
    protected VmNetworkInterface getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmNetworkInterface.class),
                                       control.createMock(VmNetworkStatistics.class),
                                       index);
    }

    static VmNetworkInterface setUpEntityExpectations(VmNetworkInterface entity,
            VmNetworkStatistics statistics,
            int index) {
        return setUpEntityExpectations(entity, statistics, index, NAMES[2]);
    }

    static VmNetworkInterface setUpEntityExpectations(VmNetworkInterface entity,
            VmNetworkStatistics statistics,
            int index,
            String networkName) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getVmId()).andReturn(PARENT_ID).anyTimes();
        expect(entity.getNetworkName()).andReturn(networkName).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getMacAddress()).andReturn(ADDRESSES[2]).anyTimes();
        expect(entity.getType()).andReturn(0).anyTimes();
        expect(entity.getSpeed()).andReturn(50).anyTimes();
        return setUpStatisticalEntityExpectations(entity, statistics);
    }

    static VmNetworkInterface setUpStatisticalEntityExpectations(VmNetworkInterface entity, VmNetworkStatistics statistics) {
        expect(entity.getStatistics()).andReturn(statistics).anyTimes();
        expect(statistics.getReceiveRate()).andReturn(1D).anyTimes();
        expect(statistics.getReceiveDropRate()).andReturn(2D).anyTimes();
        expect(statistics.getTransmitRate()).andReturn(3D).anyTimes();
        expect(statistics.getTransmitDropRate()).andReturn(4D).anyTimes();
        expect(statistics.getReceivedBytes()).andReturn(5L).anyTimes();
        expect(statistics.getTransmittedBytes()).andReturn(6L).anyTimes();
        return entity;
    }

    @Override
    protected List<NIC> getCollection() {
        return collection.list().getNics();
    }

    static NIC getModel(int index) {
        NIC model = new NIC();
        model.setName(NAMES[index]);
        model.setInterface(NicInterface.RTL8139_VIRTIO.value());
        model.setNetwork(new Network());
        model.getNetwork().setId(GUIDS[0].toString());
        return model;
    }

    @Override
    protected void verifyModel(NIC model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(NIC model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index].toString(), model.getName());
        assertTrue(model.isSetVm());
        assertEquals(PARENT_ID.toString(), model.getVm().getId());
        assertTrue(model.isSetNetwork());
        assertEquals(GUIDS[0].toString(), model.getNetwork().getId());
        assertTrue(model.isSetMac());
        assertEquals(ADDRESSES[2].toString(), model.getMac().getAddress());
    }

    protected void setGetNetworksQueryExpectations(int times) throws Exception {
            while (times-- > 0) {
                ArrayList<org.ovirt.engine.core.common.businessentities.network.Network> networks = new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>();
                org.ovirt.engine.core.common.businessentities.network.Network network = new org.ovirt.engine.core.common.businessentities.network.Network();
                network.setId(GUIDS[0]);
                network.setName("orcus");
                networks.add(network);
                setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                        IdQueryParameters.class,
                        new String[] { "Id" },
                        new Object[] { GUIDS[0] },
                        networks);
            }
        }

    protected void setGetGuestAgentQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVmGuestAgentInterfacesByVmId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { PARENT_ID },
                    getListOfVmGuestAgentInterfaces());
        }
    }

    @SuppressWarnings("serial")
    private Object getListOfVmGuestAgentInterfaces() {
        VmGuestAgentInterface iface = new VmGuestAgentInterface();
        iface.setMacAddress(ADDRESSES[2]);
        List<VmGuestAgentInterface> list = new ArrayList<VmGuestAgentInterface>();
        list.add(iface);
        return list;
    }
}
