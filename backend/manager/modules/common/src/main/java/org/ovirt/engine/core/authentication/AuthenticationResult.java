package org.ovirt.engine.core.authentication;

import java.util.List;

import org.ovirt.engine.core.common.errors.VdcBllMessages;

/**
 * This class represents a result returned by an Authenticator
 */
public abstract class AuthenticationResult<T> {

    protected T detailedInfo;

    protected AuthenticationResult(T detailedInfo) {
        this.detailedInfo = detailedInfo;
    }

    public void setDetailedInfo(T detailedInfo) {
        this.detailedInfo = detailedInfo;
    }

    public T getDetailedInfo() {
        return detailedInfo;
    }

    /**
     * Returns whether the authentication is successful
     * @return
     */
    public abstract boolean isSuccessful();

    /**
     * Resolves the detailed information into VdcBll messages
     * @return
     */
    public abstract List<VdcBllMessages> resolveMessage();
}
