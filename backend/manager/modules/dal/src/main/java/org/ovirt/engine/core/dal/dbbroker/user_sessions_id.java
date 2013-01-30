package org.ovirt.engine.core.dal.dbbroker;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class user_sessions_id implements Serializable {
    private static final long serialVersionUID = 1L;

    public Guid getUserId() {
        return userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        user_sessions_id other = (user_sessions_id) obj;
        if (sessionId == null) {
            if (other.sessionId != null)
                return false;
        } else if (!sessionId.equals(other.sessionId))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }

    Guid userId;

    String sessionId;

    public user_sessions_id() {
    }

    public user_sessions_id(Guid userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

}
