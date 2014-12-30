package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.PARENT_ID;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.verifyModelSpecific;
import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResourceTest.PARENT_GUID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.MAC;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.PortMirroring;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.api.restapi.resource.BaseBackendResource.WebFaultException;
import org.ovirt.engine.api.restapi.util.RxTxCalculator;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicResourceTest
        extends AbstractBackendSubResourceTest<NIC, VmNetworkInterface, BackendDeviceResource<NIC, Nics, VmNetworkInterface>> {

    protected static final Guid NIC_ID = GUIDS[1];
    protected static final String ADDRESS = "10.11.12.13";

    protected static BackendVmNicsResource collection;

    public BackendVmNicResourceTest() {
        super((BackendVmNicResource)getCollection().getDeviceSubResource(NIC_ID.toString()));
    }

    protected static BackendVmNicsResource getCollection() {
        return new BackendVmNicsResource(PARENT_ID);
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     new ArrayList<VmNetworkInterface>());
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setAllContentHeaderExpectation();
        setUpEntityQueryExpectations(1);
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setGetGuestAgentQueryExpectations(1);
        control.replay();

        NIC nic = resource.get();
        verifyModelSpecific(nic, 1);
        verifyLinks(nic);
    }

    @Test
    public void testGetNoNetwork() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setAllContentHeaderExpectation();
        setUpEntityQueryExpectations(1);
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1, Collections.<org.ovirt.engine.core.common.businessentities.network.Network> emptyList());
        setGetGuestAgentQueryExpectations(1);
        control.replay();

        NIC nic = resource.get();
        assertNotNull(nic);
        assertNull(nic.getNetwork().getName());
        assertNull(nic.getNetwork().getId());
        verifyLinks(nic);
    }

    @Test
    public void testGetIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            setAllContentHeaderExpectation();
            setUpEntityQueryExpectations(1);
            setGetVmQueryExpectations(1);
            setGetNetworksQueryExpectations(1);
            setGetGuestAgentQueryExpectations(1);
            control.replay();

            NIC nic = resource.get();
            assertTrue(nic.isSetStatistics());
            verifyModelSpecific(nic, 1);
            verifyLinks(nic);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     new ArrayList<VmNetworkInterface>());
        control.replay();
        try {
            resource.update(getNic(false));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(3);
        setAllContentHeaderExpectation();
        setGetVmQueryExpectations(2);
        setGetNetworksQueryExpectations(2);
        setGetGuestAgentQueryExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmInterface,
                                           AddVmInterfaceParameters.class,
                                           new String[] { "VmId", "Interface.Id" },
                                           new Object[] { PARENT_ID, GUIDS[1] },
                                           true,
                                           true));

        NIC nic = resource.update(getNic(false));
        assertNotNull(nic);
    }

    @Test(expected = WebApplicationException.class)
    public void testUpdateWrongPortMirroringNetwork() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        control.replay();
        NIC nic = getNic(false);
        nic.getPortMirroring().getNetworks().getNetworks().get(0).setId(GUIDS[1].toString());
        nic = resource.update(nic);
        assertNotNull(nic);
    }

    @Test
    public void testUpdateNoNetworkWithPortMirroringNetworkFail() throws Exception {
        try {
            VmNetworkInterface entity = getEntity(1, null);
            setUpGetEntityExpectations(1, entity);
            setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmInterface,
                    AddVmInterfaceParameters.class,
                    new String[] { "VmId", "Interface.Id" },
                    new Object[] { PARENT_ID, GUIDS[1] },
                    false,
                    true));
            NIC nic = getNic(false);
            nic.setNetwork(new Network());
            resource.update(nic);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, CANT_DO);
        }
    }

    @Test(expected=WebFaultException.class)
    public void testUpdateWithNonExistingNetwork() throws Exception {
        control.replay();
        NIC nic = resource.update(getNic(true));
        nic.getNetwork().setId(GUIDS[2].toString());
        assertNotNull(nic);
    }

    @Test
    public void testUpdateWithExistingNetwork() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setAllContentHeaderExpectation();
        setUpEntityQueryExpectations(2);
        setGetVmQueryExpectations(2);
        setGetNetworksQueryExpectations(2);
        setGetGuestAgentQueryExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmInterface,
                                           AddVmInterfaceParameters.class,
                                           new String[] { "VmId", "Interface.Id" },
                                           new Object[] { PARENT_ID, GUIDS[1] },
                                           true,
                                           true));

        NIC nic = resource.update(getNic(true));
        assertNotNull(nic);
    }

    @Test
    public void testUpdateWithNoNetwork() throws Exception {
        VmNetworkInterface entity = getEntity(1, null);
        setAllContentHeaderExpectation();
        setUpGetEntityExpectations(3, entity);
        setGetGuestAgentQueryExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmInterface,
                AddVmInterfaceParameters.class,
                new String[] { "VmId", "Interface.Id" },
                new Object[] { PARENT_ID, GUIDS[1] },
                true,
                true));

        NIC nic = getNic(false);
        nic.setNetwork(new Network());
        nic.setPortMirroring(null);
        nic = resource.update(nic);
        assertNotNull(nic);
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        VmNetworkInterface entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<NIC, VmNetworkInterface> statisticsResource =
            (BackendStatisticsResource<NIC, VmNetworkInterface>)((NicResource) resource).getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    protected void setGetVmQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            VM vm = new VM();
            vm.setVdsGroupId(GUIDS[0]);
            setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { PARENT_ID },
                    vm);
        }
    }

    protected void setGetNetworksQueryExpectations(int times) throws Exception {
        ArrayList<org.ovirt.engine.core.common.businessentities.network.Network> networks = new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>();
        org.ovirt.engine.core.common.businessentities.network.Network network = new org.ovirt.engine.core.common.businessentities.network.Network();
        network.setId(GUIDS[0]);
        network.setName("orcus");
        networks.add(network);
        setGetNetworksQueryExpectations(times, networks);
    }

    protected void setGetNetworksQueryExpectations(int times, List<org.ovirt.engine.core.common.businessentities.network.Network> networks) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[0] },
                    networks);
        }
    }

    protected VmNetworkInterface setUpStatisticalExpectations() throws Exception {
        VmNetworkStatistics stats = control.createMock(VmNetworkStatistics.class);
        VmNetworkInterface entity = control.createMock(VmNetworkInterface.class);
        expect(entity.getStatistics()).andReturn(stats);
        expect(entity.getSpeed()).andReturn(50).anyTimes();
        expect(entity.getId()).andReturn(NIC_ID).anyTimes();
        expect(stats.getReceiveRate()).andReturn(10D);
        expect(stats.getTransmitRate()).andReturn(20D);
        expect(stats.getReceiveDropRate()).andReturn(30D);
        expect(stats.getTransmitDropRate()).andReturn(40D);
        expect(stats.getReceivedBytes()).andReturn(50L);
        expect(stats.getTransmittedBytes()).andReturn(60L);
        List<VmNetworkInterface> ifaces = new ArrayList<VmNetworkInterface>();
        ifaces.add(entity);
        setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     ifaces);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<NIC, VmNetworkInterface> query, VmNetworkInterface entity) throws Exception {
        assertEquals(NIC.class, query.getParentType());
        assertSame(entity, query.resolve(NIC_ID));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                new String[] { "data.current.rx", "data.current.tx", "errors.total.rx", "errors.total.tx",
                        "data.total.rx", "data.total.tx" },
                new BigDecimal[] { asDec(RxTxCalculator.percent2bytes(50, 10D)), asDec(RxTxCalculator.percent2bytes(50, 20D)),
                        asDec(30), asDec(40), asDec(50), asDec(60) });
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetNic());
        assertEquals(NIC_ID.toString(), adopted.getNic().getId());
        assertTrue(adopted.getNic().isSetVm());
        assertEquals(PARENT_ID.toString(), adopted.getNic().getVm().getId());
    }

    protected NIC getNic(boolean withNetwork) {
        NIC nic = new NIC();
        nic.setMac(new MAC());
        nic.getMac().setAddress("00:1a:4a:16:85:18");
        if (withNetwork) {
            Network network = new Network();
            network.setId(GUIDS[0].toString());
            nic.setNetwork(network);
        }

        Network network = new Network();
        network.setId(GUIDS[0].toString());
         nic.setPortMirroring(new PortMirroring());
         nic.getPortMirroring().setNetworks(new Networks());
         nic.getPortMirroring().getNetworks().getNetworks().add(network);

        return nic;
    }

    protected VmNetworkInterface getEntity(int index, String networkName) {
        return setUpEntityExpectations(control.createMock(VmNetworkInterface.class),
                control.createMock(VmNetworkStatistics.class),
                index, networkName);
    }

    @Override
    protected VmNetworkInterface getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmNetworkInterface.class),
                                       control.createMock(VmNetworkStatistics.class),
                                       index);
    }

    protected List<VmNetworkInterface> getEntityList() {
        List<VmNetworkInterface> entities = new ArrayList<VmNetworkInterface>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;

    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { PARENT_ID },
                                         getEntityList());
        }
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, getEntity(1));
    }

    protected void setUpGetEntityExpectations(int times, VmNetworkInterface entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { PARENT_ID },
                                       entity);
        }
    }

    @Test
    public void testActivateNic() throws Exception {
        BackendVmNicResource backendVmNicResource = (BackendVmNicResource) resource;
        setUpGetEntityExpectations(4);
        setGetVmQueryExpectations(4);
        setGetNetworksQueryExpectations(4);
        setGetGuestAgentQueryExpectations(3);
        setAllContentHeaderExpectation();
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmInterface,
                AddVmInterfaceParameters.class,
                new String[] { "VmId", "Interface.Id" },
                new Object[] { PARENT_ID, GUIDS[1] }));

        verifyActionResponse(backendVmNicResource.activate(new Action()));
    }

    private void setAllContentHeaderExpectation() {
        List<String> allContentHeaders = new ArrayList<String>();
        allContentHeaders.add("true");
        expect(httpHeaders.getRequestHeader("All-Content")).andReturn(allContentHeaders).anyTimes();
    }

    @Test
    public void testDeactivateNic() throws Exception {
        BackendVmNicResource backendVmNicResource = (BackendVmNicResource) resource;
        setAllContentHeaderExpectation();
        setUpGetEntityExpectations(4);
        setGetVmQueryExpectations(4);
        setGetNetworksQueryExpectations(4);
        setGetGuestAgentQueryExpectations(3);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmInterface,
                AddVmInterfaceParameters.class,
                new String[] { "VmId", "Interface.Id" },
                new Object[] { PARENT_ID, GUIDS[1] }));

        verifyActionResponse(backendVmNicResource.deactivate(new Action()));
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "vms/" + PARENT_GUID.toString() + "/nics/" + NIC_ID.toString(), false);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
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
        iface.setMacAddress(ADDRESS);
        List<VmGuestAgentInterface> list = new ArrayList<VmGuestAgentInterface>();
        list.add(iface);
        return list;
    }
}
