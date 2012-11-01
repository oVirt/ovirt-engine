package org.ovirt.engine.ui.webadmin.plugin.restapi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
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
 * <li>reuse current session if the user is already authenticated (auto login)
 * <li>keep the current session alive while the user stays authenticated
 * <li>release the current session when the user signs out
 * </ul>
 * <p>
 * Triggers {@link RestApiSessionAcquiredEvent} upon acquiring REST API session.
 */
public class RestApiSessionManager {

    private static class RestApiRequestCallback implements RequestCallback {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if (response.getStatusCode() == Response.SC_OK) {
                processResponse(response);
            } else {
                RestApiSessionManager.logger.warning("Engine REST API response received with non-OK status code " //$NON-NLS-1$
                        + response.getStatusCode());
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            RestApiSessionManager.logger.log(Level.WARNING, "Error while requesting Engine REST API", exception); //$NON-NLS-1$
        }

        protected void processResponse(Response response) {
            // No-op, override as necessary
        }

    }

    private static final Logger logger = Logger.getLogger(RestApiSessionManager.class.getName());

    private static final String SESSION_ID_HEADER = "JSESSIONID"; //$NON-NLS-1$
    private static final String SESSION_ID_KEY = "RestApiSessionId"; //$NON-NLS-1$

    // Heartbeat (delay) between REST API keep-alive requests
    private static final int SESSION_HEARTBEAT_MS = 1000 * 60; // 1 minute

    private final EventBus eventBus;
    private final ClientStorage clientStorage;
    private final String restApiBaseUrl;

    @Inject
    public RestApiSessionManager(EventBus eventBus, ClientStorage clientStorage) {
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;
        this.restApiBaseUrl = FrontendUrlUtils.getRootURL() + "api"; //$NON-NLS-1$
    }

    void setPersistentAuthHeader(RequestBuilder requestBuilder) {
        requestBuilder.setHeader("Prefer", "persistent-auth"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    void sendRequest(RequestBuilder requestBuilder, RestApiRequestCallback callback) {
        try {
            requestBuilder.sendRequest(null, callback);
        } catch (RequestException e) {
            // Request failed to initiate, nothing we can do about it
        }
    }

    RequestBuilder createRequest() {
        return new RequestBuilder(RequestBuilder.GET, restApiBaseUrl);
    }

    void scheduleKeepAliveHeartbeat() {
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
            @Override
            public boolean execute() {
                if (getCurrentSessionId() != null) {
                    // The browser takes care of setting Engine REST API JSESSIONID
                    // cookie automatically as part of processing the HTTP request
                    RequestBuilder requestBuilder = createRequest();
                    setPersistentAuthHeader(requestBuilder);
                    sendRequest(requestBuilder, new RestApiRequestCallback());

                    // Proceed with the heartbeat
                    return true;
                } else {
                    // The session has been released, cancel the heartbeat
                    return false;
                }
            }
        }, SESSION_HEARTBEAT_MS);
    }

    /**
     * Acquires new REST API session using the given credentials.
     */
    public void acquireSession(String userNameWithDomain, String password) {
        RequestBuilder requestBuilder = createRequest();
        setPersistentAuthHeader(requestBuilder);
        requestBuilder.setUser(userNameWithDomain);
        requestBuilder.setPassword(password);

        sendRequest(requestBuilder, new RestApiRequestCallback() {
            @Override
            protected void processResponse(Response response) {
                // Obtain the session ID from response header, as we're unable
                // to access Engine REST API JSESSIONID cookie value directly
                // (cookie is set for different path than WebAdmin host page)
                String sessionIdFromHeader = response.getHeader(SESSION_ID_HEADER);

                if (sessionIdFromHeader != null) {
                    setCurrentSessionId(sessionIdFromHeader);
                }

                reuseCurrentSession();
            }
        });
    }

    /**
     * Attempts to reuse current REST API session.
     */
    public void reuseCurrentSession() {
        String currentSessionId = getCurrentSessionId();

        if (currentSessionId != null) {
            RestApiSessionAcquiredEvent.fire(eventBus, currentSessionId);
            scheduleKeepAliveHeartbeat();
        } else {
            RestApiSessionManager.logger.severe("Engine REST API session ID is not available"); //$NON-NLS-1$
        }
    }

    /**
     * Releases REST API session currently in use.
     */
    public void releaseSession() {
        clearCurrentSessionId();
    }

    String getCurrentSessionId() {
        return clientStorage.getLocalItem(SESSION_ID_KEY);
    }

    void setCurrentSessionId(String sessionId) {
        clientStorage.setLocalItem(SESSION_ID_KEY, sessionId);
    }

    void clearCurrentSessionId() {
        clientStorage.removeLocalItem(SESSION_ID_KEY);
    }

}
