package org.ovirt.engine.ui.uicommonweb.auth;

public interface CurrentUserRole {

    boolean isCreateInstanceOnly();

    void setCreateInstanceOnly(boolean createInstanceOnly);
}
