package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.ReadOnlyDeviceResource;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendReadOnlyCdRomsResourceTest
        extends AbstractBackendCdRomsResourceTest<BackendReadOnlyCdRomsResource<VM>> {

    public BackendReadOnlyCdRomsResourceTest() {
        super(new BackendReadOnlyCdRomsResource<VM>
                                    (VM.class,
                                     PARENT_ID,
                                     VdcQueryType.GetVmByVmId,
                                     new IdQueryParameters(PARENT_ID)),
              VdcQueryType.GetVmByVmId,
              new IdQueryParameters(PARENT_ID),
              "Id");
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        Object subResource = collection.getDeviceResource(GUIDS[0].toString());
        assertFalse(subResource instanceof DeviceResource);
        assertTrue(subResource instanceof ReadOnlyDeviceResource);
    }

    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            collection.getDeviceResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }
}
