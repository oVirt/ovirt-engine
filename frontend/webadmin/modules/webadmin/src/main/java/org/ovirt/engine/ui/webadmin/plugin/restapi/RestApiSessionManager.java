package org.ovirt.engine.ui.webadmin.plugin.restapi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.communication.StorageCallback;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;

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
 * <li>reuse existing session if the user is already authenticated (auto login)
 * <li>keep the current session alive while the user stays authenticated
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

    private static final String SESSION_ID_HEADER = "JSESSIONID"; //$NON-NLS-1$
    private static final String SESSION_ID_KEY = "RestApiSessionId"; //$NON-NLS-1$
    private static final String DEFAULT_SESSION_TIMEOUT = "30"; //$NON-NLS-1$

    // Heartbeat (delay) between REST API keep-alive requests
    private static final int SESSION_HEARTBEAT_MS = 1000 * 60; // 1 minute

    private final EventBus eventBus;
    private final ClientStorage clientStorage;
    private final String restApiBaseUrl;

    private String sessionTimeout;

    private String restApiSessionId;

    @Inject
    public RestApiSessionManager(EventBus eventBus, ClientStorage clientStorage) {
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;
        // Note that the slash at the end of the URL is not just a whim. With the trailing slash the browser will only
        // send authentication headers to URLs ending in api/, otherwise it will send them to URLs ending in /, and
        // this causes problems in other applications, for example in the reports application.
        this.restApiBaseUrl = BaseContextPathData.getInstance().getPath() + "api/"; //$NON-NLS-1$
    }

    public void setSessionTimeout(String sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    String getSessionTimeout() {
        return sessionTimeout != null ? sessionTimeout : DEFAULT_SESSION_TIMEOUT;
    }

    void sendRequest(RequestBuilder requestBuilder, RestApiCallback callback) {
        try {
            requestBuilder.sendRequest(null, callback);
        } catch (RequestException e) {
            // Request failed to initiate, nothing we can do about it
        }
    }

    RequestBuilder createRequest() {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, restApiBaseUrl);
        requestBuilder.setHeader("Prefer", "persistent-auth, csrf-protection"); //$NON-NLS-1$ //$NON-NLS-2$
        requestBuilder.setHeader("Session-TTL", getSessionTimeout()); //$NON-NLS-1$
        String sessionId = getSessionId();
        if (sessionId != null) {
            requestBuilder.setHeader(SESSION_ID_HEADER, sessionId);
        }
        return requestBuilder;
    }

    void scheduleKeepAliveHeartbeat() {
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
            @Override
            public boolean execute() {
                String sessionId = getSessionId();

                if (sessionId != null) {
                    // The session is still in use
                    RequestBuilder requestBuilder = createRequest();

                    // Note: the browser takes care of sending JSESSIONID cookie for this request automatically
                    sendRequest(requestBuilder, new RestApiCallback() {
                        // No response post-processing, as we expect existing REST API (and associated Engine)
                        // session to stay alive by means of keep-alive requests
                    });

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
        requestBuilder.setUser(userNameWithDomain);
        requestBuilder.setPassword(password);

        sendRequest(requestBuilder, new RestApiCallback() {
            @Override
            protected void processResponse(Response response) {
                // Obtain session ID from response header, as we're unable to access REST API
                // JSESSIONID cookie directly (cookie set for different path than WebAdmin page)
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
        //If reuseSession is called right after setSessionId, then getSessionId() without the callback will not
        //be null. If it is null then reuseSession was called from an automatic login (as restApiSessionId is null
        //can we can utilize the async call to retrieve it from the backend.
        if (getSessionId() != null) {
            processSessionId(getSessionId());
        } else {
            getSessionId(new StorageCallback() {

                @Override
                public void onSuccess(String result) {
                    if (result != null) {
                        restApiSessionId = result;
                        processSessionId(result);
                    } else {
                        processSessionIdException();
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    processSessionIdException();
                }

                private void processSessionIdException() {
                    RestApiSessionManager.logger.severe("Engine REST API session ID is not available"); //$NON-NLS-1$
                }
            });
        }
    }

    private void processSessionId(String sessionId) {
        RestApiSessionAcquiredEvent.fire(eventBus, sessionId);
        scheduleKeepAliveHeartbeat();
    }

    /**
     * Releases REST API session currently in use.
     */
    public void releaseSession() {
        clearSessionId();
    }

    String getSessionId() {
        return restApiSessionId;
    }

    void getSessionId(final StorageCallback callback) {
        Frontend.getInstance().retrieveFromHttpSession(SESSION_ID_KEY, callback);
    }

    void setSessionId(String sessionId) {
        Frontend.getInstance().storeInHttpSession(SESSION_ID_KEY, sessionId);
        restApiSessionId = sessionId;
    }

    void clearSessionId() {
        setSessionId(null);
    }

}
