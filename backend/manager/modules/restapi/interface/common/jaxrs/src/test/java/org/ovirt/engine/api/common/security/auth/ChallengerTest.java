/*
* Copyright (c) 2010 Red Hat, Inc.
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

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyBoolean;

import org.ovirt.engine.api.common.invocation.Current;

import junit.framework.Assert;

public class ChallengerTest extends Assert {

    private static final String CREDENTIALS = "Basic TWFnaHJlYlxBbGFkZGluOm9wZW4gc2VzYW1l";
    private static final String USER = "Aladdin";
    private static final String SECRET = "open sesame";
    private static final String DOMAIN = "Maghreb";
    private static final String sessionId = "sessionId";
    private static final Principal principal = new Principal(USER, SECRET, DOMAIN);

    private Challenger challenger;
    private IMocksControl control;

    @Before
    public void setUp() {
        challenger = spy(new Challenger());
        HttpSession httpSession = new TestHttpSession(sessionId, true);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        control = EasyMock.createNiceControl();
    }

    @Test
    public void testAuthHeaderPresent() {
        HttpSession httpSession = new TestHttpSession(sessionId, true);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(CREDENTIALS, false, false), resource);
        assertNull(response);
        control.verify();
    }

    @Test
    public void testPreferHeaderPresent() {
        HttpSession httpSession = new TestHttpSession(sessionId, true);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(CREDENTIALS, true), resource);
        assertNull(response);
        control.verify();
    }

    @Test
    public void testAuthHeaderMissing() {
        HttpSession httpSession = new TestHttpSession(sessionId, true);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(null, false, false), resource);
        assertNotNull(response);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        control.verify();
    }

    @Test
    public void testAuthHeaderValidateTrue() {
        HttpSession httpSession = new TestHttpSession(sessionId, true);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        challenger.setValidator(new ConstValidator(true, sessionId));
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(CREDENTIALS, true, false), resource);
        assertNull(response);
        control.verify();
    }

    @Test
    public void testValidateSessionTrue() {
        HttpSession httpSession = new TestHttpSession(sessionId, false);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        challenger.setValidator(new ConstValidator(true, sessionId));
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(null, true, false), resource);
        assertNull(response);
        control.verify();
    }

    @Test
    public void testValidateSessionFalseOnWrongEngineSessionId() {
        HttpSession httpSession = new TestHttpSession(sessionId, false);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        challenger.setValidator(new ConstValidator(true, "wrong engine session ID"));
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(null, true, false), resource);
        assertNotNull(response);
        control.verify();
    }

    @Test
    public void testValidateSessionFalseOnNewSession() {
        HttpSession httpSession = new TestHttpSession(sessionId, true);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        challenger.setValidator(new ConstValidator(true, sessionId));
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(null, true, false), resource);
        assertNotNull(response);
        control.verify();
    }

    @Test
    public void testAuthHeaderValidateFalse() {
        HttpSession httpSession = new TestHttpSession(sessionId, true);
        doReturn(httpSession).when(challenger).getCurrentSession(anyBoolean());
        challenger.setValidator(new ConstValidator(false, sessionId));
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(CREDENTIALS, false, false), resource);
        assertNotNull(response);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        control.verify();
    }

    private HttpRequest setUpRequestExpectations(String credentials, boolean preferHeader) {
        return setUpRequestExpectations(credentials, credentials != null, preferHeader);
    }

    private HttpRequest setUpRequestExpectations(String credentials, boolean valid, boolean preferHeader) {
        Scheme authorizer = control.createMock(Scheme.class);
        challenger.setScheme(authorizer);
        Current current = control.createMock(Current.class);
        challenger.setCurrent(current);
        HttpRequest request = control.createMock(HttpRequest.class);
        HttpHeaders headers = control.createMock(HttpHeaders.class);
        expect(request.getHttpHeaders()).andReturn(headers);
        List<String> authHeaders = new ArrayList<String>();
        if (preferHeader) {
            List<String> preferHeaders = new ArrayList<String>();
            preferHeaders.add(SessionUtils.PERSIST_FIELD_VALUE);
            expect(headers.getRequestHeader(SessionUtils.PREFER_HEADER_FIELD)).andReturn(preferHeaders);
        }
        if (credentials != null) {
            expect(headers.getRequestHeader(HttpHeaders.AUTHORIZATION)).andReturn(authHeaders).anyTimes();
            Principal principal = new Principal(USER, SECRET, DOMAIN);
            expect(authorizer.decode(headers)).andReturn(principal);
            authHeaders.add(credentials);
            if (valid) {
                current.set(principal);
                EasyMock.expectLastCall();
                current.set(challenger);
                EasyMock.expectLastCall();
            }
        }
        control.replay();
        return request;
    }

    protected class ConstValidator implements Validator {
        private boolean valid;
        private String validSessionId;

        protected ConstValidator(boolean valid, String validSessionId) {
            this.valid = valid;
            this.validSessionId = validSessionId;
        }

        @Override
        public boolean validate(Principal principal, String sessionId) {
            return valid;
        }

        @Override
        public Principal validate(String sessionID) {
            if (sessionID.equals(validSessionId)) {
                return principal;
            } else {
                return null;
            }
        }

        @Override
        public void usePersistentSession(boolean logoutSession) {
        }
    }

    protected class TestHttpSession implements HttpSession {

        private String sessionId;
        private boolean isNew;

        public TestHttpSession(String sessionId, boolean isNew) {
            this.sessionId = sessionId;
            this.isNew = isNew;
        }

        @Override
        public long getCreationTime() {
            return 0;
        }

        @Override
        public String getId() {
            return sessionId;
        }

        @Override
        public long getLastAccessedTime() {
            return 0;
        }

        @Override
        public void setMaxInactiveInterval(int interval) {
        }

        @Override
        public int getMaxInactiveInterval() {
            return 0;
        }

        @Override
        public Object getAttribute(String name) {
            if (name.equals(SessionUtils.ENGINE_SESSION_ID_KEY)) {
                return sessionId;
            } else {
                return null;
            }
        }

        @Override
        public Object getValue(String name) {
            return null;
        }

        @Override
        public Enumeration getAttributeNames() {
            return null;
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public HttpSessionContext getSessionContext() {
            return null;
        }

        @Override
        public String[] getValueNames() {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {
        }

        @Override
        public void putValue(String name, Object value) {
        }

        @Override
        public void removeAttribute(String name) {
        }

        @Override
        public void removeValue(String name) {
        }

        @Override
        public void invalidate() {
        }

        @Override
        public boolean isNew() {
            return isNew;
        }
    }
}
