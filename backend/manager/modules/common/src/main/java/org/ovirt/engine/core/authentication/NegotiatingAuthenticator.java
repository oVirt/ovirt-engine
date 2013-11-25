package org.ovirt.engine.core.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Some authenticators need to negotiate with the client, exchanging HTTP requests and responses, in order to determine
 * the name of the entity being authenticated and to verify its credentials. The result of this negotiation is an
 * instance of the {@link NegotiationResult} class containing the result of the authentication (succeeded or not) and
 * the name of the authenticated entity.
 */
public interface NegotiatingAuthenticator extends Authenticator {
    /**
     * Process the given request and return a new result object if the negotiation has finished or {@code null} if it
     * hasn't. If the process hasn't finished then the response must be populated by the authenticator and it will be
     * sent back to the client.
     *
     * @param request the HTTP request to be processed
     * @param response the HTTP response to be processed by the application or sent to back the browser if the
     *     authentication didn't finish yet
     * @return a result object if the authentication process has finished or {@code null} if it hasn't
     */
    NegotiationResult negotiate(HttpServletRequest request, HttpServletResponse response);
}
