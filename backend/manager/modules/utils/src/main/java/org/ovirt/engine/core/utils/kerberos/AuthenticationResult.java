package org.ovirt.engine.core.utils.kerberos;

import org.ovirt.engine.core.common.AuditLogType;

public enum AuthenticationResult {
    OK("", "", 0, AuditLogType.USER_VDC_LOGIN),

    INVALID_CREDENTIALS(
            "Authentication Failed. Please verify the username and password.",
            "USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWORD",
            11,
            AuditLogType.AUTH_FAILED_INVALID_CREDENTIALS),

    CLOCK_SKEW_TOO_GREAT(
            "Authentication Failed. The Engine clock is not synchronized with directory services (must be within 5"
                    + " minutes difference). Please verify the clocks are synchronized",
            "USER_FAILED_TO_AUTHENTICATE_CLOCK_SKEW_TOO_GREAT",
            12,
            AuditLogType.AUTH_FAILED_CLOCK_SKEW_TOO_GREAT),

    NO_KDCS_FOUND(
            "Authentication Failed. Please verify the fully qualified domain name that is used for authentication is"
                    + " correct.",
            "USER_FAILED_TO_AUTHENTICATE_NO_KDCS_FOUND",
            13,
            AuditLogType.AUTH_FAILED_NO_KDCS_FOUND),

    DNS_ERROR(
            "Authentication Failed. Error in DNS configuration. Please verify the Engine host has a valid reverse"
                    + " DNS (PTR) record.",
            "USER_FAILED_TO_AUTHENTICATE_DNS_ERROR",
            14,
            AuditLogType.AUTH_FAILED_DNS_ERROR),

    OTHER(
            "Kerberos error. Please check log for further details.",
            "USER_FAILED_TO_AUTHENTICATE",
            15,
            AuditLogType.AUTH_FAILED_OTHER),

    USER_ACCOUNT_DISABLED_OR_LOCKED(
            "Authentication failed. The user is either locked or disabled",
            "USER_FAILED_TO_AUTHENTICATE_ACCOUNT_IS_LOCKED_OR_DISABLED",
            16,
            AuditLogType.USER_ACCOUNT_DISABLED_OR_LOCKED),

    DNS_COMMUNICATION_ERROR(
            "Authentication Failed. Cannot lookup DNS for SRV records. Please check your DNS configuration",
            "USER_FAILED_TO_AUTHENTICATE_DNS_ERROR",
            17,
            AuditLogType.AUTH_FAILED_DNS_COMMUNICATION_ERROR),

    CONNECTION_TIMED_OUT(
            "Authentication Failed. Connection to LDAP server has timed out. Please contact your system"
                    + " administrator",
            "USER_FAILED_TO_AUTHENTICATE_CONNECTION_TIMED_OUT",
            18,
            AuditLogType.AUTH_FAILED_CONNECTION_TIMED_OUT),

    WRONG_REALM(
            "Authentication Failed. Wrong domain name was provided for authentication.",
            "USER_FAILED_TO_AUTHENTICATE_WRONG_REALM",
            19,
            AuditLogType.AUTH_FAILED_WRONG_REALM),

    CONNECTION_ERROR(
            "Connection refused or some configuration problems exist. Possible DNS error." +
                    " Check your Kerberos and LDAP records",
            "USER_FAILED_TO_AUTHENTICATE_CONNECTION_ERROR",
            20,
            AuditLogType.AUTH_FAILED_CONNECTION_ERROR),

    CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN(
            "Cannot find valid LDAP server for domain",
            "CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN",
            21,
            AuditLogType.AUTH_FAILED_CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN),

    NO_USER_INFORMATION_WAS_FOUND_FOR_USER(
            "No user information was found for user",
            "NO_USER_INFORMATION_WAS_FOUND_FOR_USER",
            22,
            AuditLogType.AUTH_FAILED_NO_USER_INFORMATION_WAS_FOUND),

    PASSWORD_EXPIRED(
            "Authentication Failed. The password has expired. Please change your password and login again.",
            "USER_PASSWORD_EXPIRED",
            23,
            AuditLogType.USER_ACCOUNT_PASSWORD_EXPIRED),

    CLIENT_NOT_FOUND_IN_KERBEROS_DATABASE(
            "Authentication Failed. Client not found in kerberos database.",
            "USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWORD",
            24,
            AuditLogType.AUTH_FAILED_CLIENT_NOT_FOUND_IN_KERBEROS_DATABASE),

    INTERNAL_KERBEROS_ERROR(
            "An internal error has ocurred in the Kerberos implementation of the Java virtual machine. This usually"
                    + " means that the LDAP server is configured with a minimum security strength factor (minssf)"
                    + " of 0. Change it to 1 and try again.",
            "INTERNAL_KERBEROS_ERROR",
            25,
            AuditLogType.AUTH_FAILED_INTERNAL_KERBEROS_ERROR);

    private String vdcBllMessage;
    private String detailedMessage;
    private final int exitCode;
    private AuditLogType auditLogType;

    private AuthenticationResult(String detailedMsg, String vdcBllMessage, int exitCode, AuditLogType auditLogType) {
        this.detailedMessage = detailedMsg;
        this.vdcBllMessage = vdcBllMessage;
        this.exitCode = exitCode;
        this.auditLogType = auditLogType;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public AuditLogType getAuditLogType() {
        return auditLogType;
    }

    public String getVdcBllMessage() {
        return vdcBllMessage;
    }

    public int getExitCode() {
        return exitCode;
    }
}
