package org.ovirt.engine.core.common.interfaces;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public interface IVdcUser extends Serializable {
    String getPassword();

    void setPassword(String value);

    Guid getUserId();

    void setUserId(Guid value);

    String getUserName();

    void setUserName(String value);

    String getDomainControler();

    void setDomainControler(String value);

    String getBrowser();

    void setBrowser(String value);

    String getGroupNames();

    void setGroupNames(String value);

    String getFirstName();

    void setFirstName(String value);

    String getSurName();

    void setSurName(String value);

    boolean isAdmin();

    void setAdmin(boolean isAdmin);

    String getGroupIds();

    void setGroupIds(String groupIds);

    String getFQN();
}
