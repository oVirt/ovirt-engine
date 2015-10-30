package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class UserSession implements IVdcQueryable {

    private long id;
    private String userName;
    private Guid userId;
    private String sourceIp;

    public UserSession(EngineSession engineSession) {
        Objects.requireNonNull(engineSession, "engineSession cannot be null");

        id = engineSession.getId();
        userName = engineSession.getUserName();
        userId = engineSession.getUserId();
        sourceIp = engineSession.getSourceIp();
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
