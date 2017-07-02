package org.ovirt.engine.core.common.businessentities;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class EngineSession implements Queryable {
    private static final long serialVersionUID = 6964615561527013329L;

    private long id;

    private String engineSessionId;

    private Guid userId;

    private String sourceIp;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_LOGIN_NAME_SIZE)
    private String userName;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_DOMAIN_SIZE)
    private String authzName;

    private Collection<Guid> roleIds;

    private Collection<Guid> groupIds;

    private Date startTime;

    private Date lastActiveTime;

    public EngineSession() {}

    public EngineSession(DbUser dbUser, String engineSessionId, String sourceIp) {
        setUserId(dbUser.getId());
        setUserName(dbUser.getLoginName());
        setAuthzName(dbUser.getDomain());
        setGroupIds(dbUser.getGroupIds());
        setEngineSessionId(engineSessionId);
        setSourceIp(sourceIp);
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    /**
     * Session id assigned by engine for the user's session
     */
    public String getEngineSessionId() {
        return engineSessionId;
    }

    public void setEngineSessionId(String engineSessionId) {
        this.engineSessionId = engineSessionId;
    }

    /**
     * Identifier assigned by the engine to this user for internal use only.
     */
    public Guid getUserId() {
        return userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Collection<Guid> getRoleIds() {
        if (roleIds == null) {
            roleIds = Collections.emptySet();
        }
        return new HashSet<>(roleIds);
    }

    public void setRoleIds(Collection<Guid> roleIds) {
        this.roleIds = roleIds;
    }

    /**
     * Comma delimited list of group identifiers.
     */
    public Collection<Guid> getGroupIds() {
        if (groupIds == null) {
            groupIds = Collections.emptyList();
        }
        return new HashSet<>(groupIds);
    }

    public void setGroupIds(Collection<Guid> groupIds) {
        this.groupIds = groupIds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                engineSessionId,
                userId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EngineSession)) {
            return false;
        }
        EngineSession other = (EngineSession) obj;
        return Objects.equals(engineSessionId, other.engineSessionId)
                && Objects.equals(userId, other.userId);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getAuthzName() {
        return authzName;
    }

    public void setAuthzName(String authzName) {
        this.authzName = authzName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
}
