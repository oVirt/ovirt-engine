package org.ovirt.engine.core.aaa;

import java.io.Serializable;

public class ProfileEntry implements Serializable {

    private static final long serialVersionUID = 8525199877264821199L;

    private String profile;
    private String authz;
    private boolean supportsPasswordAuthentication;

    public ProfileEntry() {
    }

    public ProfileEntry(String profile, String authz, boolean supportsPasswordAuthentication) {
        this.profile = profile;
        this.authz = authz;
        this.supportsPasswordAuthentication = supportsPasswordAuthentication;
    }

    public String getProfile() {
        return profile;
    }

    public String getAuthz() {
        return authz;
    }

    public boolean getSupportsPasswordAuthenication() {
        return supportsPasswordAuthentication;
    }

    public String toString() {
        return profile + " (" + authz + ")"; //$NON-NLS-1$  //$NON-NLS-2$
    }

}
