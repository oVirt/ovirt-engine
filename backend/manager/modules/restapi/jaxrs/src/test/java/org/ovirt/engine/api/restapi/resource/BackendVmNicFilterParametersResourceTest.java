/*
Copyright (c) 2017 Red Hat, Inc.

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.NetworkFilterParameter;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicFilterParametersResourceTest
        extends AbstractBackendCollectionResourceTest<NetworkFilterParameter, VmNicFilterParameter, BackendVmNicFilterParametersResource> {

    private static final Guid PARAMETER_ID = GUIDS[0];
    private static final Guid VM_ID = GUIDS[1];
    private static final Guid VM_NIC_ID = GUIDS[2];

    public BackendVmNicFilterParametersResourceTest() {
        super(new BackendVmNicFilterParametersResource(VM_ID, VM_NIC_ID), null, null);
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
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
    public void testAddParameter() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(
                ActionType.AddVmNicFilterParameter,
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
    public void testAddNicCantDo() throws Exception {
        doTestBadAddNic(false, true, CANT_DO);
    }

    @Test
    public void testAddNicFailure() throws Exception {
        doTestBadAddNic(true, false, FAILURE);
    }

    private void doTestBadAddNic(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(
            setUpActionExpectations(
                ActionType.AddVmNicFilterParameter,
                    VmNicFilterParameterParameters.class,
                new String[] { "VmId" },
                new Object[] { VM_ID },
                valid,
                success
            )
        );
        NetworkFilterParameter model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        NetworkFilterParameter model = new NetworkFilterParameter();

        setUriInfo(setUpBasicUriExpectations());
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "NetworkFilterParameter", "add", "value");
        }
    }

    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        try {
            collection.getParameterResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
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
