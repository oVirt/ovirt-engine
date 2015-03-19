package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class EngineSession extends IVdcQueryable implements Serializable {
    private static final long serialVersionUID = 6964615561527013329L;

    private long id;

    private String engineSessionId;

    private Guid userId;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_LOGIN_NAME_SIZE)
    private String userName;

    private Collection<Guid> roleIds;

    private Collection<Guid> groupIds;

    public EngineSession() {}

    public EngineSession(DbUser dbUser, String engineSessionId) {
        setUserId(dbUser.getId());
        setUserName(dbUser.getLoginName());
        setGroupIds(dbUser.getGroupIds());
        setEngineSessionId(engineSessionId);
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
        return new HashSet<Guid>(roleIds);
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
        return new HashSet<Guid>(groupIds);
    }

    public void setGroupIds(Collection<Guid> groupIds) {
        this.groupIds = groupIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((engineSessionId == null) ? 0 : engineSessionId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EngineSession other = (EngineSession) obj;
        return  ObjectUtils.objectsEqual(engineSessionId, other.engineSessionId)
                && ObjectUtils.objectsEqual(userId, other.userId);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
