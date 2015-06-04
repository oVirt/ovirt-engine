package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendCdRomsResourceTest
        extends AbstractBackendCdRomsResourceTest<BackendCdRomsResource> {

    public BackendCdRomsResourceTest() {
        super(new BackendCdRomsResource(PARENT_ID,
                                       VdcQueryType.GetVmByVmId,
                                       new IdQueryParameters(PARENT_ID)),
              VdcQueryType.GetVmByVmId,
              new IdQueryParameters(PARENT_ID),
              "Id");
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     getEntity(0),
                                     2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));
        verifyRemove(collection.deprecatedRemove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { PARENT_ID },
                new VM(),
                1);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(wae.getResponse().getStatus(), 404);
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     getEntity(0),
                                     2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));
        try {
            collection.deprecatedRemove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddCdRom() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     getEntity(0),
                                     1);

        setUpCreationExpectations(VdcActionType.UpdateVm,
                                  VmManagementParametersBase.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetVmByVmId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { PARENT_ID },
                                  getEntity(0));
        CdRom model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof CdRom);
        verifyModel((CdRom) response.getEntity(), 0);
    }

    @Test
    public void testAddCdRomCantDo() throws Exception {
        doTestBadAddCdRom(false, true, CANT_DO);
    }

    @Test
    public void testAddCdRomFailure() throws Exception {
        doTestBadAddCdRom(true, false, FAILURE);
    }

    private void doTestBadAddCdRom(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     getEntity(0),
                                     1);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));
        CdRom model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        CdRom model = new CdRom();
        model.setName(NAMES[0]);
        model.setFile(new File());

        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "CdRom", "add", "file.id");
        }
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        assertTrue(collection.getDeviceSubResource(GUIDS[0].toString()) instanceof DeviceResource);
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

    protected void setUpEntityQueryExpectations(VdcQueryType query,
            Class<? extends VdcQueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn,
            int times) {
        while (times-->0) {
            setUpEntityQueryExpectations(query, queryClass, queryNames, queryValues, queryReturn);
        }
    }
}
