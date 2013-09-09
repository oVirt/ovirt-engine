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

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class Challenger implements PreProcessInterceptor {

    private static final int MINIMAL_SESSION_TTL = 1;
    private static final int SECONDS_IN_MINUTE = 60;
    protected static final Log LOG = LogFactory.getLog(Challenger.class);
    private static final String SESSION_TTL_EXTRACT_ERROR =
            "%1$s header content extraction has failed because of bad number format: %2$s";
    private static final String SESSION_TTL_ILLEGAL_ERROR =
            "%1$s header cannot be zero or negative, endless session is not supported.";

    private String realm;
    private Scheme scheme;
    private Validator validator;
    private Current current;

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    /**
     * Issue 401 challenge on missing or invalid credentials.
     * May be called further up the call-stack if supplied credentials are
     * found to be invalid.
     *
     * @return ServerResponse containing challenge
     */
    public Response getChallenge() {
        return Response.status(Status.UNAUTHORIZED)
                       .header(HttpHeaders.WWW_AUTHENTICATE, scheme.getChallenge(realm))
                       .build();
    }

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {

        HttpSession httpSession = null;
        ServerResponse response = null;
        boolean successful = false;
        HttpHeaders headers = request.getHttpHeaders();
        boolean preferPersistentAuth = checkPersistentAuthentication(headers);
        boolean hasAuthorizationHeader = checkAuthorizationHeader(headers);
        Integer customHttpSessionTtl = getCustomHttpSessionTtl(headers);

        // Get the current session
        // For persistent auth we will create a new session if authentication
        // is successful
        httpSession = getCurrentSession(false);

        // If the session isn't new and doesn't carry authorization header, we validate it
        if (validator != null && httpSession != null && !hasAuthorizationHeader) {
            successful = executeSessionValidation(httpSession, preferPersistentAuth);
        } else {
            // If the session isn't new but carries authorization header, we invalidate it first
            if (validator != null && httpSession != null) {
                httpSession.invalidate();
                httpSession = getCurrentSession(true);
            }

            // Authenticate the session
            successful = executeBasicAuthentication(headers, httpSession, preferPersistentAuth);

            if (successful && preferPersistentAuth) {
                if (httpSession == null) {
                    httpSession = getCurrentSession(false);
                }
                if (httpSession != null && customHttpSessionTtl != null) {
                    if (customHttpSessionTtl.intValue() >= MINIMAL_SESSION_TTL) {
                        // Specifies the time, between client requests before the servlet
                        // container will invalidate this session.
                        httpSession.setMaxInactiveInterval(
                                customHttpSessionTtl.intValue() * SECONDS_IN_MINUTE);
                    } else {
                        // An interval value of zero or less is not supported
                        // (indicates that the session should never timeout).
                        LOG.error(String.format(
                                SESSION_TTL_ILLEGAL_ERROR,
                                SessionUtils.SESSION_TTL_HEADER_FIELD));

                    }
                }
            }
        }

        if (!successful) {
            response = challenge();
            // In this case we invalidate the session, so that a new one will be created on the next attempt
            if (httpSession != null) {
                httpSession.invalidate();
            }
        }
        return response;
    }

    /**
     * Extracts the SESSION_TTL_HEADER
     *
     * @param headers
     *            HTTP headers
     *
     * @return SESSION_TTL_HEADER or null
     */
    private Integer getCustomHttpSessionTtl(HttpHeaders headers) {
        Integer ttl = null;

        List<String> sessionTtlFields = SessionUtils.getHeaderField(
                headers,
                SessionUtils.SESSION_TTL_HEADER_FIELD);

        if (sessionTtlFields != null && !sessionTtlFields.isEmpty()) {
            try {
                return Integer.valueOf(sessionTtlFields.get(0));
            } catch (NumberFormatException e) {
                LOG.error(String.format(
                        SESSION_TTL_EXTRACT_ERROR,
                        SessionUtils.SESSION_TTL_HEADER_FIELD,
                        sessionTtlFields.get(0)));
            }
        }

        return ttl;
    }

    /*
     * This method executes the basic authentication, and returns true whether it was successful and false otherwise.
     * It also sets the logged-in principal and the challenger object in the Current object
     */
    private boolean executeBasicAuthentication(HttpHeaders headers, HttpSession httpSession, boolean preferPersistentAuth) {
        boolean successful = false;
        List<String> auth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.size() != 0) {
            Principal principal = scheme.decode(headers);
            String engineSessionId = SessionUtils.generateEngineSessionId();
            if (validator == null || validator.validate(principal, engineSessionId)) {
                successful = true;
                if (httpSession == null && preferPersistentAuth) {
                    httpSession = getCurrentSession(true);
                }
                SessionUtils.setEngineSessionId(httpSession, engineSessionId);
                updateAuthenticationProperties(preferPersistentAuth, principal);
            }
        }
        return successful;
    }

    /*
     * This method executes session validation, and returns true whether it was successful and false otherwise.
     * It also sets the logged-in principal and the challenger object in the Current object
     */
    private boolean executeSessionValidation(HttpSession session, boolean preferPersistentAuth) {
        boolean successful = false;
        Principal principal = validator.validate(SessionUtils.getEngineSessionId(session));
        if (principal != null) {
            successful = true;
            updateAuthenticationProperties(preferPersistentAuth, principal);
        }
        return successful;
    }

    private void updateAuthenticationProperties(boolean preferPersistentAuth, Principal principal) {
        current.set(principal);
        current.set(this);

        if (validator != null) {
            validator.usePersistentSession(preferPersistentAuth);
        }
    }

    private boolean checkPersistentAuthentication(HttpHeaders headers) {
        List<String> preferField = SessionUtils.getHeaderField(headers, SessionUtils.PREFER_HEADER_FIELD);

        if (preferField != null) {
            for (String currValue : preferField) {
                if (currValue.equalsIgnoreCase(SessionUtils.PERSIST_FIELD_VALUE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAuthorizationHeader(HttpHeaders headers) {
        List<String> authorizationField = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        return authorizationField != null && !authorizationField.isEmpty();
    }

    // Here to ease mocking it in the tester
    protected HttpSession getCurrentSession(boolean create) {
        return SessionUtils.getCurrentSession(create);
    }

    /**
     * By default principal validation is lazy, with the assumption that this
     * will be initiated by the resource later on the dispatch path. This method
     * allows subclasses to pursue an alternate strategy based on eager validation.
     *
     * @param principal  the decoded principal
     * @return           true iff dispatch should continue
     */
    protected boolean validate(Principal principal) {
        return true;
    }

    /**
     * Helper method to copy the challenge response
     */
    private ServerResponse challenge() {
        return ServerResponse.copyIfNotServerResponse(getChallenge());
    }
}
