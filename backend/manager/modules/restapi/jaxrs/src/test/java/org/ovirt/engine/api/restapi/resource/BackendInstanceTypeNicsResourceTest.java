/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendInstanceTypeNicsResourceTest
        extends AbstractBackendCollectionResourceTest<Nic, VmNetworkInterface, BackendTemplateNicsResource> {

    protected static final Guid INSTANCE_TYPE_ID = GUIDS[1];
    protected static final String[] ADDRESSES = { "10.11.12.13", "13.12.11.10", "10.01.10.01" };

    public BackendInstanceTypeNicsResourceTest() {
        super(new BackendTemplateNicsResource(INSTANCE_TYPE_ID), null, null);
    }

    @Override
    protected void setUpQueryExpectations(String query) {
        setUpEntityQueryExpectations(1);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(1, failure);
    }

    protected void setUpEntityQueryExpectations(int times) {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                QueryType.GetTemplateInterfacesByTemplateId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { INSTANCE_TYPE_ID },
                getEntityList(),
                failure
            );
        }
    }

    protected List<VmNetworkInterface> getEntityList() {
        List<VmNetworkInterface> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Override
    protected VmNetworkInterface getEntity(int index) {
        return setUpEntityExpectations(
            mock(VmNetworkInterface.class),
            mock(VmNetworkStatistics.class),
            index
        );
    }

    static VmNetworkInterface setUpEntityExpectations(
            VmNetworkInterface entity,
            VmNetworkStatistics statistics,
            int index) {
        return setUpEntityExpectations(entity, statistics, index, NAMES[2]);
    }

    static VmNetworkInterface setUpEntityExpectations(
            VmNetworkInterface entity,
            VmNetworkStatistics statistics,
            int index,
            String networkName) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getVmId()).thenReturn(INSTANCE_TYPE_ID);
        when(entity.getNetworkName()).thenReturn(networkName);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getMacAddress()).thenReturn(ADDRESSES[2]);
        when(entity.getType()).thenReturn(0);
        when(entity.getSpeed()).thenReturn(50);
        return setUpStatisticalEntityExpectations(entity, statistics);
    }

    static VmNetworkInterface setUpStatisticalEntityExpectations(
            VmNetworkInterface entity,
            VmNetworkStatistics statistics) {
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
    protected List<Nic> getCollection() {
        return collection.list().getNics();
    }

    static Nic getModel(int index) {
        Nic model = new Nic();
        model.setName(NAMES[index]);
        model.setInterface(NicInterface.RTL8139_VIRTIO);
        return model;
    }

    @Override
    protected void verifyModel(Nic model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Nic model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertTrue(model.isSetVm());
        assertEquals(INSTANCE_TYPE_ID.toString(), model.getVm().getId());
        assertTrue(model.isSetMac());
        assertEquals(ADDRESSES[2], model.getMac().getAddress());
    }

    @Test
    public void testAddNic() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(
            ActionType.AddVmTemplateInterface,
            AddVmTemplateInterfaceParameters.class,
            new String[] {},
            new Object[] {},
            true,
            true,
            null,
            QueryType.GetTemplateInterfacesByTemplateId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { INSTANCE_TYPE_ID },
            asList(getEntity(0))
        );
        Nic model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Nic);
        verifyModel((Nic) response.getEntity(), 0);
    }

    @Test
    public void testAddNicCantDo() {
        doTestBadAddNic(false, true, CANT_DO);
    }

    @Test
    public void testAddNicFailure() {
        doTestBadAddNic(true, false, FAILURE);
    }

    private void doTestBadAddNic(boolean valid, boolean success, String detail) {
        setUriInfo(
            setUpActionExpectations(
                ActionType.AddVmTemplateInterface,
                AddVmTemplateInterfaceParameters.class,
                new String[] { "VmTemplateId" },
                new Object[] { INSTANCE_TYPE_ID },
                valid,
                success
            )
        );
        Nic model = getModel(0);

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        Nic model = new Nic();
        model.setName(null);

        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)), "Nic", "add", "name");
    }

    @Test
    public void testSubResourceLocatorBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> collection.getNicResource("foo")));
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }
}
