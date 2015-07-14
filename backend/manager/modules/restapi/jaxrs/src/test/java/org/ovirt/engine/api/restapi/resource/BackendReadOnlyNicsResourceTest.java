package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.ReadOnlyDeviceResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendReadOnlyNicsResourceTest
        extends AbstractBackendNicsResourceTest<BackendReadOnlyNicsResource> {

    public BackendReadOnlyNicsResourceTest() {
        super(new BackendReadOnlyNicsResource(PARENT_ID,
                                               VdcQueryType.GetTemplateInterfacesByTemplateId,
                                               new IdQueryParameters(PARENT_ID)),
              VdcQueryType.GetTemplateInterfacesByTemplateId,
              new IdQueryParameters(PARENT_ID),
              "Id");
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        Object subResource = collection.getDeviceSubResource(GUIDS[0].toString());
        assertFalse(subResource instanceof DeviceResource);
        assertTrue(subResource instanceof ReadOnlyDeviceResource);
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

    @Override
    @Ignore
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

}
