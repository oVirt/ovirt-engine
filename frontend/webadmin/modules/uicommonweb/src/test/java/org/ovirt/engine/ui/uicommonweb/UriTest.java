package org.ovirt.engine.ui.uicommonweb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UriTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "www.redhat.com", //$NON-NLS-1$
            "www.redhat.com/", //$NON-NLS-1$
            "www.redhat.com/main", //$NON-NLS-1$
            "www.redhat.com/main/", //$NON-NLS-1$
            "www.redhat.com/main/index.html", //$NON-NLS-1$
            "http://www.redhat.com", //$NON-NLS-1$
            "http://www.redhat.com/", //$NON-NLS-1$
            "http://www.redhat.com/main", //$NON-NLS-1$
            "http://www.redhat.com/main/", //$NON-NLS-1$
            "http://www.redhat.com/main/index.html", //$NON-NLS-1$
            "www.redhat.com:80", //$NON-NLS-1$
            "www.redhat.com:80/", //$NON-NLS-1$
            "www.redhat.com:80/main", //$NON-NLS-1$
            "www.redhat.com:80/main/", //$NON-NLS-1$
            "www.redhat.com:80/main/index.html", //$NON-NLS-1$
            "http://www.redhat.com:80", //$NON-NLS-1$
            "http://www.redhat.com:80/", //$NON-NLS-1$
            "http://www.redhat.com:80/main", //$NON-NLS-1$
            "http://www.redhat.com:80/main/", //$NON-NLS-1$
            "http://www.redhat.com:80/main/index.html" //$NON-NLS-1$
    })
    public void runTest(String uriCandidate) {
        Uri uri = new Uri(uriCandidate);
        assertTrue(uri.isValid());
        assertEquals(uriCandidate.toLowerCase(), uri.getStringRepresentation());
    }
}
