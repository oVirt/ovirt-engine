package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa;

public enum ReturnStatus {
    OK("", 0),
    INPUT_VALIDATION_FAILURE("Input validation failure.", 1),
    LDAP_CONTEXT_FAILURE("Failure setting LDAP context.", 2),
    CANNOT_QUERY_USER("Cannot query user from LDAP server.", 3),
    CANNOT_AUTHENTICATE_USER("Cannot authenticate user to LDAP server.", 4),
    CANNOT_DETECT_PROVIDER_TYPE("Cannot detect LDAP provider type.", 5),
    INVALID_CREDENTIALS("Invalid Credentials", 6);

    private String detailedMessage;
    private final int exitCode;

    private ReturnStatus(String detailedMsg, int exitCode) {
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
