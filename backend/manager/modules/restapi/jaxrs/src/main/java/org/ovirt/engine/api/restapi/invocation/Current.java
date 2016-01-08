package org.ovirt.engine.api.restapi.invocation;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.mode.ApplicationMode;

/**
 * This class stores information that its scoped to the request currently being processed.
 */
public class Current {
    /**
     * The version of the API.
     */
    private String apiVersion;

    /**
     * The identifier of the backend session.
     */
    private String sessionId;

    /**
     * This indicates the application mode for the current request.
     */
    private ApplicationMode applicationMode;

    /**
     * This is a reference to the user that is performing the request.
     */
    private DbUser user;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ApplicationMode getApplicationMode() {
        return applicationMode;
    }

    public void setApplicationMode(ApplicationMode applicationMode) {
        this.applicationMode = applicationMode;
    }

    public DbUser getUser() {
        return user;
    }

    public void setUser(DbUser user) {
        this.user = user;
    }
}
