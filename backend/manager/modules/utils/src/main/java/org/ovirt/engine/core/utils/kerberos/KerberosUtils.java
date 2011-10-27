package org.ovirt.engine.core.utils.kerberos;

import javax.naming.CommunicationException;

/**
 * Helper methods for kerberos related issues
 *
 */
public class KerberosUtils {
    public static AuthenticationResult convertDNSException(Exception ex) {
        AuthenticationResult result = AuthenticationResult.DNS_ERROR;
        if (ex instanceof javax.naming.NameNotFoundException) {
            result = AuthenticationResult.NO_KDCS_FOUND;
        } else if (ex instanceof CommunicationException) {
            result = AuthenticationResult.DNS_COMMUNICATION_ERROR;
        }
        return result;
    }
}
