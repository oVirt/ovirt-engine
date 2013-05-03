package org.ovirt.engine.ui.webadmin.plugin.restapi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 * Takes care of Engine REST API session management for UI plugin infrastructure.
 * <p>
 * This class has following responsibilities:
 * <ul>
 * <li>acquire new session upon successful user authentication (classic login)
 * <li>reuse existing session if the user is already authenticated (auto login)
 * </ul>
 * <p>
 * Note that the REST API session is not closed upon user logout, as there might be other systems still working with it.
 * <p>
 * Triggers {@link RestApiSessionAcquiredEvent} upon acquiring or reusing REST API session.
 */
public class RestApiSessionManager {

    private static class RestApiCallback implements RequestCallback {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if (response.getStatusCode() == Response.SC_OK) {
                processResponse(response);
            } else {
                RestApiSessionManager.logger.warning(
                        "Engine REST API responded with non-OK status code " //$NON-NLS-1$
                                + response.getStatusCode());
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            RestApiSessionManager.logger.log(Level.WARNING,
                    "Error while dispatching Engine REST API request", exception); //$NON-NLS-1$
        }

        protected void processResponse(Response response) {
            // No-op, override as necessary
        }

    }

    private static final Logger logger = Logger.getLogger(RestApiSessionManager.class.getName());

    private static final String SESSION_TIMEOUT = "360"; //$NON-NLS-1$
    private static final String SESSION_ID_HEADER = "JSESSIONID"; //$NON-NLS-1$
    private static final String SESSION_ID_KEY = "RestApiSessionId"; //$NON-NLS-1$

    private final EventBus eventBus;
    private final ClientStorage clientStorage;
    private final String restApiBaseUrl;

    @Inject
    public RestApiSessionManager(EventBus eventBus, ClientStorage clientStorage) {
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;
        this.restApiBaseUrl = FrontendUrlUtils.getRootURL() + "api"; //$NON-NLS-1$
    }

    void sendRequest(RequestBuilder requestBuilder, RestApiCallback callback) {
        try {
            requestBuilder.sendRequest(null, callback);
        } catch (RequestException e) {
            // Request failed to initiate, nothing we can do about it
        }
    }

    /**
     * Acquires new REST API session using the given credentials.
     */
    public void acquireSession(String userNameWithDomain, String password) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, restApiBaseUrl);
        requestBuilder.setHeader("Prefer", "persistent-auth"); //$NON-NLS-1$ //$NON-NLS-2$
        requestBuilder.setHeader("Session-TTL", SESSION_TIMEOUT); //$NON-NLS-1$
        requestBuilder.setUser(userNameWithDomain);
        requestBuilder.setPassword(password);

        sendRequest(requestBuilder, new RestApiCallback() {
            @Override
            protected void processResponse(Response response) {
                // Obtain the session ID from response header, as we're unable
                // to access Engine REST API JSESSIONID cookie value directly
                // (cookie is set for different path than WebAdmin host page)
                String sessionIdFromHeader = response.getHeader(SESSION_ID_HEADER);

                if (sessionIdFromHeader != null) {
                    setSessionId(sessionIdFromHeader);
                }

                reuseSession();
            }
        });
    }

    /**
     * Attempts to reuse existing REST API session that was previously {@linkplain #acquireSession acquired}.
     */
    public void reuseSession() {
        String sessionId = getSessionId();

        if (sessionId != null) {
            RestApiSessionAcquiredEvent.fire(eventBus, sessionId);
        } else {
            RestApiSessionManager.logger.severe("Engine REST API session ID is not available"); //$NON-NLS-1$
        }
    }

    /**
     * Releases REST API session currently in use.
     */
    public void releaseSession() {
        clearSessionId();
    }

    String getSessionId() {
        return clientStorage.getLocalItem(SESSION_ID_KEY);
    }

    void setSessionId(String sessionId) {
        clientStorage.setLocalItem(SESSION_ID_KEY, sessionId);
    }

    void clearSessionId() {
        clientStorage.removeLocalItem(SESSION_ID_KEY);
    }

}
