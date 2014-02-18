package org.ovirt.engine.core.aaa;

/**
 * A authenticator is an object used to verify an identity.
 */
public abstract class Authenticator {

    /**
     * Returns the name of the profile the authenticator is associated with
     * @return profile name
     */
    public String getProfileName() {
        return profileName;
    }

    protected Authenticator(String profileName) {
        this.profileName = profileName;
    }

    private String profileName;

}
