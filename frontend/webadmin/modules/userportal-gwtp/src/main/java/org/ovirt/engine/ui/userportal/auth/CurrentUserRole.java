package org.ovirt.engine.ui.userportal.auth;

public class CurrentUserRole {

    private boolean engineUser = true;

    /**
     * Returns {@code true} if the user is a basic user, having access only to places within the "basic" main tab.
     */
    public boolean isBasicUser() {
        return engineUser;
    }

    /**
     * Returns {@code true} if the user is an advanced user, having access to all places of the "main" section (places
     * within "basic" and "advanced" main tabs).
     */
    public boolean isExtendedUser() {
        return !engineUser;
    }

    public void setEngineUser(boolean engineUser) {
        this.engineUser = engineUser;
    }

}
