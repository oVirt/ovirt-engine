package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

import javax.naming.CommunicationException;
import javax.naming.NameNotFoundException;

/**
 * Helper methods for kerberos related issues
 *
 */
public class KerberosUtils {
    public static AuthenticationResult convertDNSException(Exception ex) {
        AuthenticationResult result = AuthenticationResult.DNS_ERROR;
        if (ex instanceof NameNotFoundException) {
            result = AuthenticationResult.NO_KDCS_FOUND;
        } else if (ex instanceof CommunicationException) {
            result = AuthenticationResult.DNS_COMMUNICATION_ERROR;
        }
        return result;
    }
}
