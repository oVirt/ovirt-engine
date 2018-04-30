package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.ui.uicommonweb.Uri;

public class UrlValidationTest {

    private UrlValidation urlValidation;

    private UriHostAddressValidation hostValidation;

    @BeforeEach
    public void setup() {
        urlValidation = spy(new UrlValidation(new String[] { Uri.SCHEME_HTTP }));
        hostValidation = new UriHostAddressValidation(null);
        doReturn(null).when(urlValidation).getUriMessage();
        doReturn(null).when(urlValidation).getSchemeMessage(any());
        doReturn(hostValidation).when(urlValidation).getHostValidation();
    }

    @ParameterizedTest
    @MethodSource
    public void validate(String url, boolean expectedResult) {
        assertEquals(expectedResult, urlValidation.validate(url).getSuccess());
    }

    public static Stream<Arguments> validate() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("", false), //$NON-NLS-1$
                Arguments.of("http://", false), //$NON-NLS-1$
                Arguments.of("www.redhat.com", false), //$NON-NLS-1$
                Arguments.of("192.168.0.1", false), //$NON-NLS-1$
                Arguments.of("ftp://www.redhat.com", false), //$NON-NLS-1$
                Arguments.of("ftp://192.168.0.1", false), //$NON-NLS-1$
                Arguments.of("http://www.redhat.com:/main", false), //$NON-NLS-1$
                Arguments.of("http://www.redhat.com:123456/main", false), //$NON-NLS-1$

                Arguments.of("http://1.2.3.4:666", true), //$NON-NLS-1$
                Arguments.of("http://1.2.3.4", true), //$NON-NLS-1$
                Arguments.of("http://[1:2:3:4:5:6:7:8]", true), //$NON-NLS-1$
                Arguments.of("http://[1:2:3:4:5:6:7:8]:666", true), //$NON-NLS-1$
                Arguments.of("http://www.redhat.com", true), //$NON-NLS-1$
                Arguments.of("http://www.redhat.com/main", true), //$NON-NLS-1$
                Arguments.of("http://www.redhat.com/main/index.html", true), //$NON-NLS-1$
                Arguments.of("http://www.redhat.com:80", true), //$NON-NLS-1$
                Arguments.of("http://www.redhat.com:80/main", true), //$NON-NLS-1$
                Arguments.of("http://www.redhat.com:80/main/index.html", true) //$NON-NLS-1$
        );
    }

}
