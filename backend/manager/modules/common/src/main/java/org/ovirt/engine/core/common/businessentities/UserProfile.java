package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class UserProfile  implements IVdcQueryable {
    private static final long serialVersionUID = 7251907866347833460L;

    private Guid id;

    private Guid userId;

    private Guid sshPublicKeyId;

    private String sshPublicKey;

    private String loginName;

    private boolean userPortalVmLoginAutomatically;

    public UserProfile() {
        userPortalVmLoginAutomatically = true;
        sshPublicKeyId = Guid.Empty;
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

    public Guid getSshPublicKeyId() {
        return sshPublicKeyId;
    }

    public void setSshPublicKeyId(Guid ssh_public_key_id) {
        this.sshPublicKeyId = ssh_public_key_id;
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
        return Objects.hash(
                sshPublicKeyId,
                sshPublicKey,
                userPortalVmLoginAutomatically,
                loginName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserProfile)) {
            return false;
        }
        UserProfile other = (UserProfile) obj;
        return  Objects.equals(sshPublicKeyId, other.sshPublicKeyId)
                && Objects.equals(sshPublicKey, other.sshPublicKey)
                && Objects.equals(userPortalVmLoginAutomatically, other.userPortalVmLoginAutomatically)
                && Objects.equals(loginName, other.loginName);
    }

    public Boolean isUserPortalVmLoginAutomatically() {
        return userPortalVmLoginAutomatically;
    }

    public void setUserPortalVmLoginAutomatically(boolean userPortalVmLoginAutomatically) {
        this.userPortalVmLoginAutomatically = userPortalVmLoginAutomatically;
    }
}
