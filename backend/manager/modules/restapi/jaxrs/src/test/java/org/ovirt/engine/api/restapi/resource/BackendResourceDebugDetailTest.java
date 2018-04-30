package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
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
