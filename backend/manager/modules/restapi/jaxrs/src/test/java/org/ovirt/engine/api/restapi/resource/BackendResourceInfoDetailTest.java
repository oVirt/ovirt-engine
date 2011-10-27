package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;

public class BackendResourceInfoDetailTest extends AbstractBackendResourceLoggingTest {

    @Test
    public void testDebugFaultDetail() throws Exception {
        setUpLogExpectations(false);

        Throwable t = new Exception("snafu");
        String detail = AbstractBackendResource.detail(t);

        assertEquals(t.getMessage(), detail);
    }
}
