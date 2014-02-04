package org.ovirt.engine.core.authentication;

/**
 * An authentication profile is the combination of an authenticator and a directory. An user wishing to login to the
 * system is authenticated by the authenticator and then the details are looked up in the directory.
 */
public class AuthenticationProfile {
    /**
     * The name of the profile.
     */
    private String name;

    /**
     * Reference to the authenticator object.
     */
    private Authenticator authenticator;

    /**
     * Reference to the directory object.
     */
    private Directory directory;

    /**
     * Create a new authentication profile with the given name, authenticator and directory.
     *
     * @param name the name of the profile
     * @param authenticator the authenticator that will be used to check the credentials of the user
     * @param directory the directory that will be used to lookup the details of the user once it is successfully
     *     authenticated
     */
    public AuthenticationProfile(String name, Authenticator authenticator, Directory directory) {
        this.name = name;
        this.authenticator = authenticator;
        this.directory = directory;
    }

    /**
     * Get the name of the profile.
     */
    public String getName() {
        return name;
    }

    /**
     * Get a reference to the authenticator.
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Get a reference to the directory.
     */
    public Directory getDirectory() {
        return directory;
    }
}
