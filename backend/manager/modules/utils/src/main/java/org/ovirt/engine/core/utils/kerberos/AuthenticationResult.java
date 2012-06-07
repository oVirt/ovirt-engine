package org.ovirt.engine.core.utils.kerberos;

public enum AuthenticationResult {
    OK("", "", 0),
    INVALID_CREDENTIALS(
            "Authentication Failed. Please verify the username and password.",
            "USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWORD",
            11),
    CLOCK_SKEW_TOO_GREAT(
            "Authentication Failed. oVirt Engine clock is not synchronized with directory services (must be within 5 minutes difference). Please verify the clocks are synchronized",
            "USER_FAILED_TO_AUTHENTICATE_CLOCK_SKEW_TOO_GREAT",
            12),
    NO_KDCS_FOUND(
            "Authentication Failed. Please verify the fully qualified domain name that is used for authentication is correct.",
            "USER_FAILED_TO_AUTHENTICATE_NO_KDCS_FOUND",
            13),
    DNS_ERROR(
            "Authentication Failed. Error in DNS configuration. Please verify the oVirt Engine host has a valid reverse DNS (PTR) record.",
            "USER_FAILED_TO_AUTHENTICATE_DNS_ERROR",
            14),
    OTHER("Kerberos error. Please check log for further details.", "USER_FAILED_TO_AUTHENTICATE", 15),
    USER_ACCOUNT_DISABLED_OR_LOCKED(
            "Authentication failed. The user is either locked or disabled",
            "USER_FAILED_TO_AUTHENTICATE_ACCOUNT_IS_LOCKED_OR_DISABLED",
            16),
    DNS_COMMUNICATION_ERROR(
            "Authentication Failed. Cannot lookup DNS for SRV records. Please check your DNS configuration",
            "USER_FAILED_TO_AUTHENTICATE_DNS_ERROR",
            17),
    CONNECTION_TIMED_OUT(
            "Authentication Failed. Connection to LDAP server has timed out. Please contact your system administrator",
            "USER_FAILED_TO_AUTHENTICATE_CONNECTION_TIMED_OUT",
            18),
    WRONG_REALM(
            "Authentication Failed. Wrong domain name was provided for authentication.",
            "USER_FAILED_TO_AUTHENTICATE_WRONG_REALM",
            19),
    CONNECTION_ERROR(
            "Connection refused or some configuration problems exists. Possible DNS error." +
                    " Check your Kerberos and LDAP records",
            "USER_FAILED_TO_AUTHENTICATE_CONNECTION_ERROR",
            20),
    CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN(
            "Cannot find valid LDAP server for domain",
            "CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN",
            21),
    NO_USER_INFORMATION_WAS_FOUND_FOR_USER(
            "No user information was found for user",
            "NO_USER_INFORMATION_WAS_FOUND_FOR_USER",
            22),
    PASSWORD_EXPIRED(
            "Authentication Failed. The password has expired. Please change your password and login again.",
            "USER_PASSWORD_EXPIRED",
            23);

    private String vdcBllMessage;
    private String detailedMessage;
    private final int exitCode;

    private AuthenticationResult(String detailedMsg, String vdcBllMessage, int exitCode) {
        this.detailedMessage = detailedMsg;
        this.vdcBllMessage = vdcBllMessage;
        this.exitCode = exitCode;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public String getVdcBllMessage() {
        return vdcBllMessage;
    }

    public int getExitCode() {
        return exitCode;
    }
}
