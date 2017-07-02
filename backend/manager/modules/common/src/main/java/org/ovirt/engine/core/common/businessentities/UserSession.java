package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class UserSession implements Queryable {

    private long id;
    private String userName;
    private Guid userId;
    private String sourceIp;
    private String authzName;
    private Date sessionStartTime;
    private Date sessionLastActiveTime;

    public UserSession(EngineSession engineSession) {
        Objects.requireNonNull(engineSession, "engineSession cannot be null");

        id = engineSession.getId();
        userName = engineSession.getUserName();
        userId = engineSession.getUserId();
        sourceIp = engineSession.getSourceIp();
        authzName = engineSession.getAuthzName();
        sessionStartTime = engineSession.getStartTime();
        sessionLastActiveTime = engineSession.getLastActiveTime();
    }

    private UserSession() {
    }

    public long getId() {
        return id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public Guid getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getAuthzName() {
        return authzName;
    }

    public Date getSessionStartTime() {
        return sessionStartTime;
    }

    public Date getSessionLastActiveTime() {
        return sessionLastActiveTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final UserSession that = (UserSession) other;
        return Objects.equals(this.id, that.id);
    }
}
