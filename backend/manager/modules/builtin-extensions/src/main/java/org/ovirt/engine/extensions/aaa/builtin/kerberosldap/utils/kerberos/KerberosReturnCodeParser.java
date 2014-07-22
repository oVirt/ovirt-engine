package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

import java.util.HashMap;
import java.util.Map;

public class KerberosReturnCodeParser {

    public static final String INVALID_PRE_AUTH_RETURN_MSG = "Pre-authentication information was invalid";
    public static final String CLOCK_SKEW_TOO_GREAT_RETURN_MSG = "Clock skew too great";
    public static final String SERVER_NOT_FOUND_RETURN_MSG = "Server not found in Kerberos database";
    public static final String CLIENT_NOT_FOUND_RETURN_MSG = "Client not found in Kerberos database";
    public static final String USER_ACCOUNT_DISABLED_OR_LOCKED_RETURN_MSG = "Clients credentials have been revoked";
    public static final String CANNOT_GET_KDC_FOR_REALM = "Cannot get kdc for realm";
    public static final String CONNECTION_TIMED_OUT = "Connection timed out";
    public static final String WRONG_REALM = "null (68)";
    public static final String PASSWORD_EXPIRED =
            "Password has expired - change password to reset (23)";

    private static Map<String, AuthenticationResult> messagesToReturnCode = new HashMap<String, AuthenticationResult>();

    static {
        messagesToReturnCode.put(INVALID_PRE_AUTH_RETURN_MSG, AuthenticationResult.INVALID_CREDENTIALS);
        messagesToReturnCode.put(CLIENT_NOT_FOUND_RETURN_MSG, AuthenticationResult.CLIENT_NOT_FOUND_IN_KERBEROS_DATABASE);
        messagesToReturnCode.put(SERVER_NOT_FOUND_RETURN_MSG, AuthenticationResult.DNS_ERROR);
        messagesToReturnCode.put(CLOCK_SKEW_TOO_GREAT_RETURN_MSG, AuthenticationResult.CLOCK_SKEW_TOO_GREAT);
        messagesToReturnCode.put(USER_ACCOUNT_DISABLED_OR_LOCKED_RETURN_MSG,
                AuthenticationResult.USER_ACCOUNT_DISABLED_OR_LOCKED);
        messagesToReturnCode.put(CANNOT_GET_KDC_FOR_REALM, AuthenticationResult.NO_KDCS_FOUND);
        messagesToReturnCode.put(CONNECTION_TIMED_OUT, AuthenticationResult.CONNECTION_TIMED_OUT);
        messagesToReturnCode.put(WRONG_REALM, AuthenticationResult.WRONG_REALM);
        messagesToReturnCode.put(PASSWORD_EXPIRED, AuthenticationResult.PASSWORD_EXPIRED);

    }

    public AuthenticationResult parse(String returnMessage) {
        for (Map.Entry<String, AuthenticationResult> entry : messagesToReturnCode.entrySet()) {
            if (returnMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return AuthenticationResult.OTHER;
    }

}
