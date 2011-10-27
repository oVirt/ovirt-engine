/*
* Copyright © 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.security.auth;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;


import junit.framework.Assert;

import static org.easymock.classextension.EasyMock.expect;

public class BasicAuthorizationSchemeTest extends Assert {

    private static final String SHORT_CREDENTIALS = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    private static final String UPN_LONG_CREDENTIALS = "Basic QWxhZGRpbkBNYWdocmViOm9wZW4gc2VzYW1l";
    private static final String LEGACY_LONG_CREDENTIALS = "Basic TWFnaHJlYlxBbGFkZGluOm9wZW4gc2VzYW1l";
    private static final String BAD_CREDENTIALS = "Basic 123456";
    private static final String DIGEST_CREDENTIALS =
        "Digest username=\"Mufasa\",realm=\"testrealm@host.com\","
        + "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",uri=\"/dir/index.html\","
        + "qop=auth,nc=00000001,cnonce=\"0a4f113b\","
        + "response=\"6629fae49393a05397450978507c4ef1\","
        + "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
    private static final String USER = "Aladdin";
    private static final String SECRET = "open sesame";
    private static final String DOMAIN = "Maghreb";


    private Scheme scheme;
    private IMocksControl control;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        scheme = new BasicAuthorizationScheme();
    }

    @Test
    public void testSchemeName() {
        assertEquals("Basic", scheme.getName());
    }

    @Test
    public void testDecodeShortCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(SHORT_CREDENTIALS));
        assertNotNull(principal);
        assertEquals(USER, principal.getUser());
        assertEquals(SECRET, principal.getSecret());
        assertNull(principal.getDomain());
        control.verify();
    }

    @Test
    public void testDecodeUpnLongCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(UPN_LONG_CREDENTIALS));
        assertNotNull(principal);
        assertEquals(USER, principal.getUser());
        assertEquals(SECRET, principal.getSecret());
        assertEquals(DOMAIN, principal.getDomain());
        control.verify();
    }

    @Test
    public void testDecodeLegacyLongCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(LEGACY_LONG_CREDENTIALS));
        assertNotNull(principal);
        assertEquals(USER, principal.getUser());
        assertEquals(SECRET, principal.getSecret());
        assertEquals(DOMAIN, principal.getDomain());
        control.verify();
    }

    @Test
    public void testDecodeBadCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(BAD_CREDENTIALS));
        assertNull(principal);
        control.verify();
    }

    @Test
    public void testDecodeDigestCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(DIGEST_CREDENTIALS));
        assertNull(principal);
        control.verify();
    }

    private HttpHeaders setUpHeadersExpectation(String credentials) {
        HttpHeaders headers = control.createMock(HttpHeaders.class);
        List<String> authHeaders = new ArrayList<String>();
        authHeaders.add(credentials);
        expect(headers.getRequestHeader(HttpHeaders.AUTHORIZATION)).andReturn(authHeaders);
        control.replay();
        return headers;
    }

}
