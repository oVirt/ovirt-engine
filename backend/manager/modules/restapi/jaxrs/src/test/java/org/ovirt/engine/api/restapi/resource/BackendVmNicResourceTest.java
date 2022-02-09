/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Mac;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.restapi.util.RxTxCalculator;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            QueryType.GetVmInterfacesByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            Collections.emptyList()
        );

        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setAllContentHeaderExpectation();
        setUpEntityQueryExpectations(1);
        Nic nic = resource.get();
        verifyModelSpecific(nic, 1);
        verifyLinks(nic);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            QueryType.GetVmInterfacesByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            new ArrayList<VmNetworkInterface>()
        );

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getNic(false))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2);
        setAllContentHeaderExpectation();
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateVmInterface,
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
    public void testRemove() {
        setUpEntityQueryExpectations(1);
        setAllContentHeaderExpectation();
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveVmInterface,
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
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(1);
        setAllContentHeaderExpectation();
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveVmInterface,
                RemoveVmInterfaceParameters.class,
                new String[] { "VmId", "InterfaceId" },
                new Object[] { VM_ID, NIC_ID },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    protected VmNetworkInterface setUpStatisticalExpectations() {
        VmNetworkStatistics stats = mock(VmNetworkStatistics.class);
        VmNetworkInterface entity = mock(VmNetworkInterface.class);
        when(entity.getStatistics()).thenReturn(stats);
        when(entity.getSpeed()).thenReturn(50);
        when(entity.getId()).thenReturn(NIC_ID);
        when(stats.getReceiveRate()).thenReturn(10D);
        when(stats.getTransmitRate()).thenReturn(20D);
        when(stats.getReceiveDrops()).thenReturn(new BigInteger("30"));
        when(stats.getTransmitDrops()).thenReturn(new BigInteger("40"));
        when(stats.getReceivedBytes()).thenReturn(new BigInteger("50"));
        when(stats.getTransmittedBytes()).thenReturn(new BigInteger("60"));
        List<VmNetworkInterface> ifaces = new ArrayList<>();
        ifaces.add(entity);
        setUpEntityQueryExpectations(
            QueryType.GetVmInterfacesByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            ifaces
        );
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
                "data.current.rx.bps",
                "data.current.tx.bps",
                "errors.total.rx",
                "errors.total.tx",
                "data.total.rx",
                "data.total.tx"
            },
            new BigDecimal[] {
                asDec(RxTxCalculator.percent2bytes(50, 10D)),
                asDec(RxTxCalculator.percent2bytes(50, 20D)),
                asDec(RxTxCalculator.percent2bits(50, 10D)),
                asDec(RxTxCalculator.percent2bits(50, 20D)),
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
        return setUpEntityExpectations(mock(VmNetworkInterface.class),
                                       mock(VmNetworkStatistics.class),
                                       index);
    }

    protected List<VmNetworkInterface> getEntityList() {
        List<VmNetworkInterface> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;

    }

    protected void setUpEntityQueryExpectations(int times) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                QueryType.GetVmInterfacesByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getEntityList()
            );
        }
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, getEntity(1));
    }

    protected void setUpGetEntityExpectations(int times, VmNetworkInterface entity) {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                QueryType.GetVmInterfacesByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                entity
            );
        }
    }

    @Test
    public void testActivateNic() {
        BackendVmNicResource backendVmNicResource = resource;
        setUpGetEntityExpectations(3);
        setAllContentHeaderExpectation();
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateVmInterface,
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
        when(httpHeaders.getRequestHeader("All-Content")).thenReturn(allContentHeaders);
    }

    @Test
    public void testDeactivateNic() {
        BackendVmNicResource backendVmNicResource = resource;
        setAllContentHeaderExpectation();
        setUpGetEntityExpectations(3);
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateVmInterface,
                AddVmInterfaceParameters.class,
                new String[] { "VmId", "Interface.Id" },
                new Object[] { VM_ID, NIC_ID }
            )
        );

        verifyActionResponse(backendVmNicResource.deactivate(new Action()));
    }

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, "vms/" + VM_ID + "/nics/" + NIC_ID, false);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
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
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getVmId()).thenReturn(VM_ID);
        when(entity.getNetworkName()).thenReturn(networkName);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getMacAddress()).thenReturn(ADDRESSES[2]);
        when(entity.getType()).thenReturn(0);
        when(entity.getSpeed()).thenReturn(50);
        return setUpStatisticalEntityExpectations(entity, statistics);
    }

    private VmNetworkInterface setUpStatisticalEntityExpectations(VmNetworkInterface entity, VmNetworkStatistics statistics) {
        when(entity.getStatistics()).thenReturn(statistics);
        when(statistics.getReceiveRate()).thenReturn(1D);
        when(statistics.getReceiveDrops()).thenReturn(BigInteger.TWO);
        when(statistics.getTransmitRate()).thenReturn(3D);
        when(statistics.getTransmitDrops()).thenReturn(new BigInteger("4"));
        when(statistics.getReceivedBytes()).thenReturn(new BigInteger("5"));
        when(statistics.getTransmittedBytes()).thenReturn(new BigInteger("6"));
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
