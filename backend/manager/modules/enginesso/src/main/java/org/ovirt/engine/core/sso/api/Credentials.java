package org.ovirt.engine.core.sso.api;

import java.util.Objects;

public class Credentials {
    private String username;
    private String password;
    private String profile;
    private boolean profileValid;
    private String credentials;
    private String newCredentials;
    private String confirmedNewCredentials;

    public Credentials() {
    }

    public Credentials(String username, String password, String profile, boolean profileValid) {
        setUsername(username);
        setPassword(password);
        setProfile(profile);
        setProfileValid(profileValid);
    }

    public Credentials(
            String username,
            String credentials,
            String newCredentials,
            String confirmedNewCredentials,
            String profile) {
        setUsername(username);
        setCredentials(credentials);
        setNewCredentials(newCredentials);
        setConfirmedNewCredentials(confirmedNewCredentials);
        setProfile(profile);
    }

    public String getUsername() {
        return username;
    }

    public String getUsernameWithProfile() {
        String user = String.format("%s@%s",
                Objects.toString(username, ""),
                Objects.toString(profile, ""));
        return "@".equals(user) ? "N/A" : user;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setProfileValid(boolean profileValid) {
        this.profileValid = profileValid;
    }

    public boolean isProfileValid() {
        return profileValid;
    }

    @Override
    public String toString() {
        String user = getUsernameWithProfile();
        return "N/A".equals(user) ? "" : (" for user " + user);
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getNewCredentials() {
        return newCredentials;
    }

    public void setNewCredentials(String credentialsNew1) {
        this.newCredentials = credentialsNew1;
    }

    public String getConfirmedNewCredentials() {
        return confirmedNewCredentials;
    }

    public void setConfirmedNewCredentials(String credentialsNew2) {
        this.confirmedNewCredentials = credentialsNew2;
    }
}
