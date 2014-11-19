package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

public enum AuthenticationResult {
    OK("", 0),

    INVALID_CREDENTIALS(
            "Authentication Failed. Please verify the username and password.",
            11),

    CLOCK_SKEW_TOO_GREAT(
            "Authentication Failed. The Engine clock is not synchronized with directory services (must be within 5"
                    + " minutes difference). Please verify the clocks are synchronized",
            12),

    NO_KDCS_FOUND(
            "Authentication Failed. Please verify the fully qualified domain name that is used for authentication is"
                    + " correct.",
            13),

    DNS_ERROR(
            "Authentication Failed. Error in DNS configuration. Please verify the Engine host has a valid reverse"
                    + " DNS (PTR) record.",
            14),

    OTHER(
            "Kerberos error. Please check log for further details.",
            15),

    USER_ACCOUNT_DISABLED_OR_LOCKED(
            "Authentication failed. The user is either locked or disabled",
            16),

    DNS_COMMUNICATION_ERROR(
            "Authentication Failed. Cannot lookup DNS for SRV records. Please check your DNS configuration",
            17),

    CONNECTION_TIMED_OUT(
            "Authentication Failed. Connection to LDAP server has timed out. Please contact your system"
                    + " administrator",
            18),

    WRONG_REALM(
            "Authentication Failed. Wrong domain name was provided for authentication.",
            19),

    CONNECTION_ERROR(
            "Connection refused or some configuration problems exist. Possible DNS error." +
                    " Check your Kerberos and LDAP records",
            20),

    CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN(
            "Cannot find valid LDAP server for domain",
            21),

    NO_USER_INFORMATION_WAS_FOUND_FOR_USER(
            "No user information was found for user",
            22),

    PASSWORD_EXPIRED(
            "Authentication Failed. The password has expired. Please change your password and login again.",
            23),

    CLIENT_NOT_FOUND_IN_KERBEROS_DATABASE(
            "Authentication Failed. Client not found in kerberos database.",
            24),

    INTERNAL_KERBEROS_ERROR(
            "An internal error has ocurred in the Kerberos implementation of the Java virtual machine. This usually"
                    + " means that the LDAP server is configured with a minimum security strength factor (minssf)"
                    + " of 0. Change it to 1 and try again. You can also try to change the SASL quality of protection to \"auth\" which will lower the protection level. "
                    + " To change the SASL quality of protection to \"auth\" use engine-config -s SASL_QOP=auth and restart engine.",
            25);

    private String detailedMessage;
    private final int exitCode;

    private AuthenticationResult(String detailedMsg, int exitCode) {
        this.detailedMessage = detailedMsg;
        this.exitCode = exitCode;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public int getExitCode() {
        return exitCode;
    }
}
