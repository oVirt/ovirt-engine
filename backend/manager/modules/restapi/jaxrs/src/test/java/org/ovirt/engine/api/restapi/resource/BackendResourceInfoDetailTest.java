package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendResourceInfoDetailTest extends AbstractBackendResourceLoggingTest {

    @Test
    public void testDebugFaultDetail() throws Exception {
        setUpLogExpectations(false);

        Throwable t = new Exception("snafu");
        String detail = AbstractBackendResource.detail(t);

        assertEquals(t.getMessage(), detail);
    }
}
