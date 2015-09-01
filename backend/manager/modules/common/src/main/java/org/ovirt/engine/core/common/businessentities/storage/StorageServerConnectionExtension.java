package org.ovirt.engine.core.common.businessentities.storage;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionExtension implements BusinessEntity<Guid> {
    @NotNull
    private Guid id;

    @NotNull
    private Guid hostId;

    @NotNull
    private String iqn;

    @NotNull
    private String userName;

    @NotNull
    private String password;

    public StorageServerConnectionExtension() {
        id = Guid.newGuid();
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public String getIqn() {
        return iqn;
    }

    public void setIqn(String iqn) {
        this.iqn = iqn;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StorageServerConnectionExtension) {
            StorageServerConnectionExtension that = (StorageServerConnectionExtension) obj;
            return getId().equals(that.getId()) &&
                    getIqn().equals(that.getIqn()) &&
                    getHostId().equals(that.getHostId()) &&
                    getPassword().equals(that.getPassword()) &&
                    getUserName().equals(that.getUserName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + hostId.hashCode();
        result = 31 * result + iqn.hashCode();
        result = 31 * result + userName.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("hostId", getHostId())
                .append("target", getIqn())
                .append("username", getUserName())
                .build();
    }
}
