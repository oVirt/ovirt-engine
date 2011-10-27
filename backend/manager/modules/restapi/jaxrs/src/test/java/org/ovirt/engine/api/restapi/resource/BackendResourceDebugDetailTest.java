package org.ovirt.engine.api.restapi.resource;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

public class BackendResourceDebugDetailTest extends AbstractBackendResourceLoggingTest {

    @Test
    public void testDebugFaultDetail() throws Exception {
        setUpLogExpectations(true);

        Throwable t = new Exception("snafu");
        String detail = AbstractBackendResource.detail(t);

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw, true));
        assertEquals(sw.toString(), detail);
    }
}
