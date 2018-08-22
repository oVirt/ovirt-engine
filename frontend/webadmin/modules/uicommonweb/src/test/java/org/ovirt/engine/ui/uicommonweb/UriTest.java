package org.ovirt.engine.ui.uicommonweb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class UriTest {

    public static final String EMPTY = ""; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM = "www.redhat.com"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_S = "www.redhat.com/"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_MAIN = "www.redhat.com/main"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_MAIN_S = "www.redhat.com/main/"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_MAIN_INDEX_HTML = "www.redhat.com/main/index.html"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM = "http://www.redhat.com"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_S = "http://www.redhat.com/"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_MAIN = "http://www.redhat.com/main"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_MAIN1 = "http://www.redhat.com/main/"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_MAIN_INDEX_HTML = "http://www.redhat.com/main/index.html"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_80 = "www.redhat.com:80"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_80_S = "www.redhat.com:80/"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_80_MAIN = "www.redhat.com:80/main"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_80_MAIN_S = "www.redhat.com:80/main/"; //$NON-NLS-1$
    public static final String WWW_REDHAT_COM_80_MAIN_INDEX_HTML = "www.redhat.com:80/main/index.html"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_80 = "http://www.redhat.com:80"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_80_S = "http://www.redhat.com:80/"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_80_MAIN = "http://www.redhat.com:80/main"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_80_MAIN_S = "http://www.redhat.com:80/main/"; //$NON-NLS-1$
    public static final String HTTP_WWW_REDHAT_COM_80_MAIN_INDEX_HTML = "http://www.redhat.com:80/main/index.html"; //$NON-NLS-1$
    public static final String HTTP = "http"; //$NON-NLS-1$
    public static final String HTTPS = "https"; //$NON-NLS-1$
    public static final String SLASH = "/"; //$NON-NLS-1$
    public static final String MAIN = "/main"; //$NON-NLS-1$
    public static final String MAIN_S = "/main/"; //$NON-NLS-1$
    public static final String MAIN_INDEX_HTML = "/main/index.html"; //$NON-NLS-1$

    @ParameterizedTest
    @ValueSource(strings = {
            WWW_REDHAT_COM,
            WWW_REDHAT_COM_S,
            WWW_REDHAT_COM_MAIN,
            WWW_REDHAT_COM_MAIN_S,
            WWW_REDHAT_COM_MAIN_INDEX_HTML,
            HTTP_WWW_REDHAT_COM,
            HTTP_WWW_REDHAT_COM_S,
            HTTP_WWW_REDHAT_COM_MAIN,
            HTTP_WWW_REDHAT_COM_MAIN1,
            HTTP_WWW_REDHAT_COM_MAIN_INDEX_HTML,
            WWW_REDHAT_COM_80,
            WWW_REDHAT_COM_80_S,
            WWW_REDHAT_COM_80_MAIN,
            WWW_REDHAT_COM_80_MAIN_S,
            WWW_REDHAT_COM_80_MAIN_INDEX_HTML,
            HTTP_WWW_REDHAT_COM_80,
            HTTP_WWW_REDHAT_COM_80_S,
            HTTP_WWW_REDHAT_COM_80_MAIN,
            HTTP_WWW_REDHAT_COM_80_MAIN_S,
            HTTP_WWW_REDHAT_COM_80_MAIN_INDEX_HTML
    })
    public void createByCandidate(String uriCandidate) {
        Uri uri = new Uri(uriCandidate);
        assertTrue(uri.isValid());
        assertEquals(uriCandidate.toLowerCase(), uri.getStringRepresentation());
    }

    public static Stream<Arguments> createByComponents() {
        return Stream.of(
                Arguments.of(EMPTY, WWW_REDHAT_COM, EMPTY, WWW_REDHAT_COM),
                Arguments.of(EMPTY, WWW_REDHAT_COM, SLASH, WWW_REDHAT_COM_S),
                Arguments.of(EMPTY, WWW_REDHAT_COM, MAIN, WWW_REDHAT_COM_MAIN),
                Arguments.of(EMPTY, WWW_REDHAT_COM, MAIN_S, WWW_REDHAT_COM_MAIN_S),
                Arguments.of(EMPTY, WWW_REDHAT_COM, MAIN_INDEX_HTML, WWW_REDHAT_COM_MAIN_INDEX_HTML),
                Arguments.of(HTTP, WWW_REDHAT_COM, EMPTY, HTTP_WWW_REDHAT_COM),
                Arguments.of(HTTPS, WWW_REDHAT_COM, SLASH, "https://www.redhat.com/"), //$NON-NLS-1$
                Arguments.of(HTTP, WWW_REDHAT_COM, MAIN, HTTP_WWW_REDHAT_COM_MAIN),
                Arguments.of(HTTP, WWW_REDHAT_COM, MAIN_S, HTTP_WWW_REDHAT_COM_MAIN1),
                Arguments.of(HTTP, WWW_REDHAT_COM, MAIN_INDEX_HTML, HTTP_WWW_REDHAT_COM_MAIN_INDEX_HTML),
                Arguments.of(EMPTY, WWW_REDHAT_COM_80, EMPTY, WWW_REDHAT_COM_80),
                Arguments.of(EMPTY, WWW_REDHAT_COM_80, SLASH, WWW_REDHAT_COM_80_S),
                Arguments.of(EMPTY, WWW_REDHAT_COM_80, MAIN, WWW_REDHAT_COM_80_MAIN),
                Arguments.of(EMPTY, WWW_REDHAT_COM_80, MAIN_S, WWW_REDHAT_COM_80_MAIN_S),
                Arguments.of(EMPTY, WWW_REDHAT_COM_80, MAIN_INDEX_HTML, WWW_REDHAT_COM_80_MAIN_INDEX_HTML),
                Arguments.of(HTTP, WWW_REDHAT_COM_80, EMPTY, HTTP_WWW_REDHAT_COM_80),
                Arguments.of(HTTPS, WWW_REDHAT_COM_80, SLASH, "https://www.redhat.com:80/"), //$NON-NLS-1$
                Arguments.of(HTTP, WWW_REDHAT_COM_80, MAIN, HTTP_WWW_REDHAT_COM_80_MAIN),
                Arguments.of(HTTP, WWW_REDHAT_COM_80, MAIN_S, HTTP_WWW_REDHAT_COM_80_MAIN_S),
                Arguments.of(null,  WWW_REDHAT_COM, MAIN_INDEX_HTML, WWW_REDHAT_COM_MAIN_INDEX_HTML),
                Arguments.of(HTTP, null, MAIN_INDEX_HTML, null),
                Arguments.of(HTTP, "www.redhat:80.com", MAIN_INDEX_HTML, null), //$NON-NLS-1$
                Arguments.of(HTTP, WWW_REDHAT_COM, null, HTTP_WWW_REDHAT_COM)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void createByComponents(String scheme, String authority, String path, String expected) {
        Uri uri = new Uri(scheme, authority, path);
        assertEquals(expected != null, uri.isValid());
        assertEquals(expected, uri.getStringRepresentation());
    }


}
