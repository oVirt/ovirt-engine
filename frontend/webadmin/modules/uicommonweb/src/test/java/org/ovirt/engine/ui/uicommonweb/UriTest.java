package org.ovirt.engine.ui.uicommonweb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class UriTest {

    private String uriCandidate;

    public UriTest(String uriCandidate) {
        this.uriCandidate = uriCandidate;
    }

    @Test
    public void runTest() {
        Uri uri = new Uri(uriCandidate);
        assertTrue(uri.isValid());
        assertEquals(uriCandidate.toLowerCase(), uri.getStringRepresentation());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> comparisonParameters() {
        return Arrays.asList(new Object[][] {
                { "www.redhat.com" }, //$NON-NLS-1$
                { "www.redhat.com/" }, //$NON-NLS-1$
                { "www.redhat.com/main" }, //$NON-NLS-1$
                { "www.redhat.com/main/" }, //$NON-NLS-1$
                { "www.redhat.com/main/index.html" }, //$NON-NLS-1$
                { "http://www.redhat.com" }, //$NON-NLS-1$
                { "http://www.redhat.com/" }, //$NON-NLS-1$
                { "http://www.redhat.com/main" }, //$NON-NLS-1$
                { "http://www.redhat.com/main/" }, //$NON-NLS-1$
                { "http://www.redhat.com/main/index.html" }, //$NON-NLS-1$
                { "www.redhat.com:80" }, //$NON-NLS-1$
                { "www.redhat.com:80/" }, //$NON-NLS-1$
                { "www.redhat.com:80/main" }, //$NON-NLS-1$
                { "www.redhat.com:80/main/" }, //$NON-NLS-1$
                { "www.redhat.com:80/main/index.html" }, //$NON-NLS-1$
                { "http://www.redhat.com:80" }, //$NON-NLS-1$
                { "http://www.redhat.com:80/" }, //$NON-NLS-1$
                { "http://www.redhat.com:80/main" }, //$NON-NLS-1$
                { "http://www.redhat.com:80/main/" }, //$NON-NLS-1$
                { "http://www.redhat.com:80/main/index.html" } //$NON-NLS-1$
        });
    }
}
