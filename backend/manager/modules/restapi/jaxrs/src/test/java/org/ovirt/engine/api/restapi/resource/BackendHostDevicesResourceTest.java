package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostDevicesResourceTest
    extends AbstractBackendHostDevicesResourceTest<BackendHostDevicesResource> {

    public BackendHostDevicesResourceTest() {
        super(new BackendHostDevicesResource(HOST_ID));
    }

    @Test
    public void testList() {
        resource.setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(
                VdcQueryType.GetHostDevicesByHostId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { HOST_ID },
                getHostDeviceCollection());

        control.replay();

        verifyHostDevices(resource.list().getHostDevices());
    }
}
