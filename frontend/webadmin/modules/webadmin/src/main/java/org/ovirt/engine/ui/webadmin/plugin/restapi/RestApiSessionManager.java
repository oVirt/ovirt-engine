package org.ovirt.engine.ui.webadmin.plugin.restapi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.utils.HttpUtils;
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
 * <b>
 * Important: acquired (physical) REST API session maps to current user's (logical) Engine session.
 * </b>
 * <p>
 * This means that the REST API session is usable only while the corresponding Engine session
 * is alive. Once the user logs out, corresponding Engine session will expire and any unclosed
 * physical sessions that map to it will become unusable.
 * <p>
 * Triggers {@link RestApiSessionAcquiredEvent} upon acquiring or reusing REST API session.
 */
public class RestApiSessionManager {

    private static class RestApiRequestCallback implements RequestCallback {

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
    private static final String ENGINE_AUTH_TOKEN_HEADER = "OVIRT-INTERNAL-ENGINE-AUTH-TOKEN"; //$NON-NLS-1$

    // Heartbeat (delay) between REST API keep-alive requests
    private static final int SESSION_HEARTBEAT_MS = 1000 * 60; // 1 minute

    private final EventBus eventBus;
    private final String restApiBaseUrl;

    private String restApiSessionTimeout = DEFAULT_SESSION_TIMEOUT;
    private String restApiSessionId;

    @Inject
    public RestApiSessionManager(EventBus eventBus) {
        this.eventBus = eventBus;

        // Note that the slash at the end of the URL is not just a whim. With the trailing slash the browser will only
        // send authentication headers to URLs ending in api/, otherwise it will send them to URLs ending in /, and
        // this causes problems in other applications, for example in the reports application.
        this.restApiBaseUrl = BaseContextPathData.getInstance().getPath() + "api/"; //$NON-NLS-1$
    }

    public void setSessionTimeout(String sessionTimeout) {
        this.restApiSessionTimeout = sessionTimeout;
    }

    /**
     * Build HTTP request to acquire new or keep-alive existing REST API session.
     * <p>
     * The {@code engineAuthToken} is required only when creating new session. Once the session
     * is created, {@code Prefer:persistent-auth} ensures that client receives the JSESSIONID
     * cookie used to associate any subsequent requests with that session.
     */
    RequestBuilder createRequest(String engineAuthToken) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restApiBaseUrl);

        // Control REST API session timeout
        builder.setHeader("Session-TTL", restApiSessionTimeout); //$NON-NLS-1$

        // Express additional preferences for serving this request
        String preferValue = "persistent-auth, csrf-protection"; //$NON-NLS-1$
        if (engineAuthToken != null) {
            // Enforce expiry of existing session when acquiring new session
            preferValue += ", new-auth"; //$NON-NLS-1$

            // Map this (physical) REST API session to current user's (logical) Engine session
            builder.setHeader(ENGINE_AUTH_TOKEN_HEADER, engineAuthToken);
        }
        builder.setHeader("Prefer", preferValue); //$NON-NLS-1$

        // Add CSRF token, this is needed due to Prefer:csrf-protection
        String sessionId = getSessionId();
        if (sessionId != null) {
            builder.setHeader(SESSION_ID_HEADER, sessionId);
        }

        return builder;
    }

    void sendRequest(RequestBuilder requestBuilder, RestApiRequestCallback callback) {
        try {
            requestBuilder.sendRequest(null, callback);
        } catch (RequestException e) {
            // Request failed to initiate, nothing we can do about it
        }
    }

    void scheduleKeepAliveHeartbeat() {
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
            @Override
            public boolean execute() {
                String sessionId = getSessionId();

                if (sessionId != null) {
                    // The browser takes care of sending JSESSIONID cookie for this request automatically
                    sendRequest(createRequest(null), new RestApiRequestCallback());

                    // The session is still in use, proceed with the heartbeat
                    return true;
                } else {
                    // The session has been released, cancel the heartbeat
                    return false;
                }
            }
        }, SESSION_HEARTBEAT_MS);
    }

    /**
     * Acquires new REST API session that maps to current user's Engine session.
     */
    public void acquireSession(String engineAuthToken) {
        sendRequest(createRequest(engineAuthToken), new RestApiRequestCallback() {
            @Override
            protected void processResponse(Response response) {
                // Obtain session ID from response header, as we're unable to access the
                // JSESSIONID cookie directly (cookie is set for REST API specific path)
                String sessionIdFromHeader = HttpUtils.getHeader(response, SESSION_ID_HEADER);

                if (sessionIdFromHeader != null) {
                    setSessionId(sessionIdFromHeader);
                }

                reuseSession();
            }
        });
    }

    /**
     * Attempts to reuse existing REST API session that was previously acquired.
     */
    public void reuseSession() {
        // If reuseSession is called right after setSessionId, then getSessionId() without the callback will not
        // be null. If it is null then reuseSession was called from an automatic login (as restApiSessionId is null
        // can we can utilize the async call to retrieve it from the backend.
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
            });
        }
    }

    void processSessionId(String sessionId) {
        RestApiSessionAcquiredEvent.fire(eventBus, sessionId);
        scheduleKeepAliveHeartbeat();
    }

    void processSessionIdException() {
        logger.severe("Engine REST API session ID is not available"); //$NON-NLS-1$
    }

    /**
     * Releases existing REST API session.
     * <p>
     * Note that we're not closing (physical) REST API session via HTTP request since the user
     * logout operation already triggered (logical) Engine session expiry. Even if the physical
     * session is still alive (JSESSIONID cookie still valid), it won't work when the associated
     * logical session is dead.
     */
    public void releaseSession() {
        setSessionId(null);
    }

    String getSessionId() {
        return restApiSessionId;
    }

    void getSessionId(StorageCallback callback) {
        Frontend.getInstance().retrieveFromHttpSession(SESSION_ID_KEY, callback);
    }

    void setSessionId(String sessionId) {
        Frontend.getInstance().storeInHttpSession(SESSION_ID_KEY, sessionId);
        restApiSessionId = sessionId;
    }

}
