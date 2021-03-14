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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.NetworkFilterParameter;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVmNicFilterParametersResourceTest
        extends AbstractBackendCollectionResourceTest<NetworkFilterParameter, VmNicFilterParameter, BackendVmNicFilterParametersResource> {

    private static final Guid PARAMETER_ID = GUIDS[0];
    private static final Guid VM_ID = GUIDS[1];
    private static final Guid VM_NIC_ID = GUIDS[2];

    public BackendVmNicFilterParametersResourceTest() {
        super(new BackendVmNicFilterParametersResource(VM_ID, VM_NIC_ID), null, null);
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
                QueryType.GetVmInterfaceFilterParametersByVmInterfaceId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_NIC_ID },
                getEntityList(),
                failure
            );
        }
    }

    static NetworkFilterParameter getModel(int index) {
        NetworkFilterParameter model = new NetworkFilterParameter();
        model.setName(NAMES[index]);
        model.setId(GUIDS[index].toString());
        model.setValue(NAMES[index]);
        return model;
    }

    protected List<VmNicFilterParameter> getEntityList() {
        List<VmNicFilterParameter> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Override
    protected VmNicFilterParameter getEntity(int index) {
        return setUpEntityExpectations(mock(VmNicFilterParameter.class), index);
    }


    static VmNicFilterParameter setUpEntityExpectations(
            VmNicFilterParameter entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        return entity;
    }

    @Override
    protected List<NetworkFilterParameter> getCollection() {
        return collection.list().getNetworkFilterParameters();
    }

    @Override
    protected void verifyModel(NetworkFilterParameter model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(NetworkFilterParameter model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
    }

    @Test
    public void testAddParameter() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(
                ActionType.AddVmNicFilterParameterLive,
                VmNicFilterParameterParameters.class,
                new String[] { "VmId" },
                new Object[] { VM_ID },
            true,
            true,
                PARAMETER_ID,
            QueryType.GetVmInterfaceFilterParameterById,
            IdQueryParameters.class,
                new String[] { "Id"},
                new Object[] {PARAMETER_ID},
            asList(getEntity(0))
        );
        NetworkFilterParameter model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof NetworkFilterParameter);
        verifyModel((NetworkFilterParameter) response.getEntity(), 0);
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
                ActionType.AddVmNicFilterParameterLive,
                    VmNicFilterParameterParameters.class,
                new String[] { "VmId" },
                new Object[] { VM_ID },
                valid,
                success
            )
        );
        NetworkFilterParameter model = getModel(0);

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        NetworkFilterParameter model = new NetworkFilterParameter();

        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "NetworkFilterParameter", "add", "value");
    }

    @Test
    public void testSubResourceLocatorBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> collection.getParameterResource("foo")));
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
