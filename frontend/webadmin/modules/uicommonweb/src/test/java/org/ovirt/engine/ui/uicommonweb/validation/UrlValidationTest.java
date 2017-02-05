package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Spy;
import org.ovirt.engine.ui.uicommonweb.Uri;

@RunWith(Parameterized.class)
public class UrlValidationTest {

    @Spy
    private UrlValidation urlValidation;

    private UriHostAddressValidation hostValidation;

    @Parameterized.Parameter(0)
    public String url;
    @Parameterized.Parameter(1)
    public boolean expectedResult;

    @Before
    public void setup() {
        urlValidation = spy(new UrlValidation(new String[] { Uri.SCHEME_HTTP }));
        hostValidation = new UriHostAddressValidation(null);
        doReturn(null).when(urlValidation).getUriMessage();
        doReturn(null).when(urlValidation).getSchemeMessage(anyString());
        doReturn(hostValidation).when(urlValidation).getHostValidation();
    }

    @Test
    public void runTest() {
        assertEquals(expectedResult, urlValidation.validate(url).getSuccess());
    }

    @Parameterized.Parameters
    public static Object[][] comparisonParameters() {
        return new Object[][] {
                { null, false },
                { "", false }, //$NON-NLS-1$
                { "http://", false }, //$NON-NLS-1$
                { "www.redhat.com", false }, //$NON-NLS-1$
                { "192.168.0.1", false }, //$NON-NLS-1$
                { "ftp://www.redhat.com", false }, //$NON-NLS-1$
                { "ftp://192.168.0.1", false }, //$NON-NLS-1$

                { "http://1.2.3.4:666", true }, //$NON-NLS-1$
                { "http://1.2.3.4", true }, //$NON-NLS-1$
                { "http://[1:2:3:4:5:6:7:8]", true }, //$NON-NLS-1$
                { "http://[1:2:3:4:5:6:7:8]:666", true }, //$NON-NLS-1$
                { "http://www.redhat.com", true }, //$NON-NLS-1$
                { "http://www.redhat.com/main", true }, //$NON-NLS-1$
                { "http://www.redhat.com/main/index.html", true }, //$NON-NLS-1$
                { "http://www.redhat.com:80", true }, //$NON-NLS-1$
                { "http://www.redhat.com:80/main", true }, //$NON-NLS-1$
                { "http://www.redhat.com:80/main/index.html", true } //$NON-NLS-1$
        };
    }

}
