/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Mac;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.restapi.util.RxTxCalculator;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicResourceTest
        extends AbstractBackendSubResourceTest<Nic, VmNetworkInterface, BackendVmNicResource> {

    private static final Guid VM_ID = GUIDS[0];
    private static final Guid NIC_ID = GUIDS[1];
    private static final String ADDRESS = "10.11.12.13";
    private static final String[] ADDRESSES = { "10.11.12.13", "13.12.11.10", "10.01.10.01" };

    public BackendVmNicResourceTest() {
        super(new BackendVmNicResource(NIC_ID.toString(), VM_ID));
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmInterfacesByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            Collections.emptyList()
        );
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setAllContentHeaderExpectation();
        setUpEntityQueryExpectations(1);
        setGetGuestAgentQueryExpectations(1);
        control.replay();

        Nic nic = resource.get();
        verifyModelSpecific(nic, 1);
        verifyLinks(nic);
    }

    @Test
    public void testGetIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            setAllContentHeaderExpectation();
            setUpEntityQueryExpectations(1);
            setGetGuestAgentQueryExpectations(1);
            control.replay();

            Nic nic = resource.get();
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
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmInterfacesByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            new ArrayList<VmNetworkInterface>()
        );
        control.replay();
        try {
            resource.update(getNic(false));
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setAllContentHeaderExpectation();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVmInterface,
                AddVmInterfaceParameters.class,
                new String[] { "VmId", "Interface.Id" },
                new Object[] { VM_ID, NIC_ID },
                true,
                true
            )
        );
        Nic nic = resource.update(getNic(false));
        assertNotNull(nic);
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        VmNetworkInterface entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<Nic, VmNetworkInterface> statisticsResource =
            (BackendStatisticsResource<Nic, VmNetworkInterface>) resource.getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }


    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(1);
        setAllContentHeaderExpectation();
        setGetGuestAgentQueryExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveVmInterface,
                RemoveVmInterfaceParameters.class,
                new String[] { "VmId", "InterfaceId" },
                new Object[] { VM_ID, NIC_ID },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(1);
        setAllContentHeaderExpectation();
        setGetGuestAgentQueryExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveVmInterface,
                RemoveVmInterfaceParameters.class,
                new String[] { "VmId", "InterfaceId" },
                new Object[] { VM_ID, NIC_ID },
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
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
        List<VmNetworkInterface> ifaces = new ArrayList<>();
        ifaces.add(entity);
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmInterfacesByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            ifaces
        );
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<Nic, VmNetworkInterface> query, VmNetworkInterface entity) throws Exception {
        assertEquals(Nic.class, query.getParentType());
        assertSame(entity, query.resolve(NIC_ID));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(
            statistics,
            new String[] {
                "data.current.rx",
                "data.current.tx",
                "errors.total.rx",
                "errors.total.tx",
                "data.total.rx",
                "data.total.tx"
            },
            new BigDecimal[] {
                asDec(RxTxCalculator.percent2bytes(50, 10D)),
                asDec(RxTxCalculator.percent2bytes(50, 20D)),
                asDec(30),
                asDec(40),
                asDec(50),
                asDec(60)
            }
        );
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetNic());
        assertEquals(NIC_ID.toString(), adopted.getNic().getId());
        assertTrue(adopted.getNic().isSetVm());
        assertEquals(VM_ID.toString(), adopted.getNic().getVm().getId());
    }

    protected Nic getNic(boolean withNetwork) {
        Nic nic = new Nic();
        nic.setMac(new Mac());
        nic.getMac().setAddress("00:1a:4a:16:85:18");
        if (withNetwork) {
            Network network = new Network();
            network.setId(GUIDS[0].toString());
        }

        Network network = new Network();
        network.setId(GUIDS[0].toString());

        return nic;
    }

    @Override
    protected VmNetworkInterface getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmNetworkInterface.class),
                                       control.createMock(VmNetworkStatistics.class),
                                       index);
    }

    protected List<VmNetworkInterface> getEntityList() {
        List<VmNetworkInterface> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;

    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetVmInterfacesByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getEntityList()
            );
        }
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, getEntity(1));
    }

    protected void setUpGetEntityExpectations(int times, VmNetworkInterface entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                VdcQueryType.GetVmInterfacesByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                entity
            );
        }
    }

    @Test
    public void testActivateNic() throws Exception {
        BackendVmNicResource backendVmNicResource = resource;
        setUpGetEntityExpectations(3);
        setAllContentHeaderExpectation();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVmInterface,
                AddVmInterfaceParameters.class,
                new String[] { "VmId", "Interface.Id" },
                new Object[] { VM_ID, NIC_ID }
            )
        );

        verifyActionResponse(backendVmNicResource.activate(new Action()));
    }

    private void setAllContentHeaderExpectation() {
        List<String> allContentHeaders = new ArrayList<>();
        allContentHeaders.add("true");
        expect(httpHeaders.getRequestHeader("All-Content")).andReturn(allContentHeaders).anyTimes();
    }

    @Test
    public void testDeactivateNic() throws Exception {
        BackendVmNicResource backendVmNicResource = resource;
        setAllContentHeaderExpectation();
        setUpGetEntityExpectations(3);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVmInterface,
                AddVmInterfaceParameters.class,
                new String[] { "VmId", "Interface.Id" },
                new Object[] { VM_ID, NIC_ID }
            )
        );

        verifyActionResponse(backendVmNicResource.deactivate(new Action()));
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "vms/" + VM_ID + "/nics/" + NIC_ID, false);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected void setGetGuestAgentQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetVmGuestAgentInterfacesByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getListOfVmGuestAgentInterfaces()
            );
        }
    }

    @SuppressWarnings("serial")
    private Object getListOfVmGuestAgentInterfaces() {
        VmGuestAgentInterface iface = new VmGuestAgentInterface();
        iface.setMacAddress(ADDRESS);
        List<VmGuestAgentInterface> list = new ArrayList<>();
        list.add(iface);
        return list;
    }

    private VmNetworkInterface setUpEntityExpectations(
            VmNetworkInterface entity,
            VmNetworkStatistics statistics,
            int index) {
        return setUpEntityExpectations(entity, statistics, index, NAMES[2]);
    }

    private VmNetworkInterface setUpEntityExpectations(
            VmNetworkInterface entity,
            VmNetworkStatistics statistics,
            int index,
            String networkName) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getVmId()).andReturn(VM_ID).anyTimes();
        expect(entity.getNetworkName()).andReturn(networkName).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getMacAddress()).andReturn(ADDRESSES[2]).anyTimes();
        expect(entity.getType()).andReturn(0).anyTimes();
        expect(entity.getSpeed()).andReturn(50).anyTimes();
        return setUpStatisticalEntityExpectations(entity, statistics);
    }

    private VmNetworkInterface setUpStatisticalEntityExpectations(VmNetworkInterface entity, VmNetworkStatistics statistics) {
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
    protected void verifyModel(Nic model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    private void verifyModelSpecific(Nic model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertTrue(model.isSetVm());
        assertEquals(VM_ID.toString(), model.getVm().getId());
        assertTrue(model.isSetMac());
        assertEquals(ADDRESSES[2], model.getMac().getAddress());
    }
}
