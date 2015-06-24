package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendTemplateNicsResourceTest
        extends AbstractBackendNicsResourceTest<BackendTemplateNicsResource> {

    public BackendTemplateNicsResourceTest() {
        super(new BackendTemplateNicsResource(PARENT_ID),
              VdcQueryType.GetTemplateInterfacesByTemplateId,
              new IdQueryParameters(PARENT_ID),
              "Id");
    }

    private VmNetworkInterface getInterface(int id, String name){
        VmNetworkInterface inter = new VmNetworkInterface();
        inter.setId(GUIDS[id]);
        inter.setName(name);
        return inter;
    }

    @Test
    public void testAddNic() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setGetTemplateQueryExpectations(2);
        setGetNetworksQueryExpectations(2);
        setUpCreationExpectations(VdcActionType.AddVmTemplateInterface,
                                  AddVmTemplateInterfaceParameters.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetTemplateInterfacesByTemplateId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { PARENT_ID },
                                  asList(getEntity(0)));
        NIC model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof NIC);
        verifyModel((NIC) response.getEntity(), 0);
    }

    @Test
    public void testAddNicCantDo() throws Exception {
        doTestBadAddNic(false, true, CANT_DO);
    }

    @Test
    public void testAddNicFailure() throws Exception {
        doTestBadAddNic(true, false, FAILURE);
    }

    private void doTestBadAddNic(boolean canDo, boolean success, String detail) throws Exception {
        setGetTemplateQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.AddVmTemplateInterface,
                                           AddVmTemplateInterfaceParameters.class,
                                           new String[] { "VmTemplateId" },
                                           new Object[] { PARENT_ID },
                                           canDo,
                                           success));
        NIC model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        NIC model = new NIC();
        model.setName(null);
        model.setNetwork(new Network());

        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "NIC", "add", "name");
        }
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        assertTrue(collection.getDeviceSubResource(GUIDS[0].toString()) instanceof NicResource);
    }

    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            collection.getDeviceSubResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setGetTemplateQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    protected void setGetTemplateQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            VmTemplate template = new VmTemplate();
            template.setVdsGroupId(GUIDS[0]);
            setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                        GetVmTemplateParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { PARENT_ID },
                                         template);
        }
    }
}
