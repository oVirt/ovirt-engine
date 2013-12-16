package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendNetworksResourceTest.getModel;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterNetworkResourceTest
    extends AbstractBackendNetworkResourceTest<BackendDataCenterNetworkResource> {

    static Guid networkId = GUIDS[0];
    static Guid dataCenterId = GUIDS[1];

    public BackendDataCenterNetworkResourceTest() {
        super(new BackendDataCenterNetworkResource(GUIDS[0].toString(), new BackendDataCenterNetworksResource(dataCenterId.toString())));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendDataCenterNetworkResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetNetworksByDataCenterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { dataCenterId },
                                     new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>());
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
        setUpEntityQueryExpectations(1);
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetNetworksByDataCenterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { dataCenterId },
                                     new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>());
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateNetwork,
                                           AddNetworkStoragePoolParameters.class,
                                           new String[] { "StoragePoolId" },
                                           new Object[] { dataCenterId },
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(1);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateNetwork,
                                           AddNetworkStoragePoolParameters.class,
                                           new String[] { "StoragePoolId" },
                                           new Object[] { dataCenterId },
                                           canDo,
                                           success));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();

        Network model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetNetworksByDataCenterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { dataCenterId },
                                         getEntityList());
        }
    }
}

