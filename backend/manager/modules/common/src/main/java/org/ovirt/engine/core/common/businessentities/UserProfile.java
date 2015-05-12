package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class UserProfile  implements IVdcQueryable {
    private static final long serialVersionUID = 7251907866347833460L;

    private Guid id;

    private Guid userId;

    private String sshPublicKey;

    private String loginName;

    public UserProfile() {
        sshPublicKey = "";
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getUserId();
    }

    public Guid getUserId() {
        return userId;
    }

    public void setUserId(Guid user_id) {
        this.userId = user_id;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginName() {
        return loginName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sshPublicKey == null) ? 0 : sshPublicKey.hashCode());
        result = prime * result + ((loginName == null) ? 0 : loginName.hashCode());
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
        UserProfile other = (UserProfile) obj;
        return  ObjectUtils.objectsEqual(sshPublicKey, other.sshPublicKey)
             && ObjectUtils.objectsEqual(loginName, other.loginName);
    }
}
