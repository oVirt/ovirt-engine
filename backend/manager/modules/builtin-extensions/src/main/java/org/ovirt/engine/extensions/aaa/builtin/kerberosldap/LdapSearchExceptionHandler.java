package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.net.ConnectException;

import javax.naming.OperationNotSupportedException;
import javax.security.sasl.SaslException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.serverordering.OrderingAlgorithmType;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.AuthenticationResult;

public class LdapSearchExceptionHandler implements ExceptionHandler<LdapSearchExceptionHandlingResponse, LdapCredentials> {

    private static final Logger log = LoggerFactory.getLogger(LdapSearchExceptionHandler.class);

    @Override
    public LdapSearchExceptionHandlingResponse handle(Exception e, LdapCredentials params) {
        LdapSearchExceptionHandlingResponse response = new LdapSearchExceptionHandlingResponse();
        if (e instanceof AuthenticationResultException) {
            handleEngineDirectoryServiceException(response, e);
        } else if (e instanceof AuthenticationException) {
            handleAuthenticationException(response);
        } else if (e instanceof CommunicationException) {
            handleCommunicationException(response, e);
        } else if (e instanceof InterruptedException) {
            handleInterruptException(response, e);
        } else {
            boolean found = false;
            for (Throwable throwable : ExceptionUtils.getThrowables(e)) {
                if ((throwable instanceof SaslException || throwable instanceof ConnectException)) {
                    handleSaslException(response, throwable);
                    found = true;
                    break;
                } else if (throwable instanceof OperationNotSupportedException) {
                    handleOperationException(response, throwable, params);
                    found = true;
                    break;
                }
            }
            if (!found) {
                handleGeneralException(response, e);
            }
        }
        return response;
    }

    private void handleGeneralException(LdapSearchExceptionHandlingResponse response, Exception e) {
        response.setTranslatedException(e)
                .setTryNextServer(true)
                .setOrderingAlgorithm(OrderingAlgorithmType.PUT_LAST);
    }

    private void handleSaslException(LdapSearchExceptionHandlingResponse response, Throwable cause) {
        response.setOrderingAlgorithm(OrderingAlgorithmType.PUT_LAST)
                .setTranslatedException(new AuthenticationResultException(AuthenticationResult.CONNECTION_ERROR,
                        "General connection problem due to " + cause))
                .setTryNextServer(true);
    }

    private void handleOperationException(LdapSearchExceptionHandlingResponse response,
            Throwable throwable, LdapCredentials credentials) {
        response.setOrderingAlgorithm(OrderingAlgorithmType.NO_OP)
                .setTranslatedException(new AuthenticationResultException(AuthenticationResult.USER_ACCOUNT_DISABLED_OR_LOCKED,
                        throwable))
                .setTryNextServer(false);
    }

    private void handleInterruptException(LdapSearchExceptionHandlingResponse response, Throwable cause) {
        response.setOrderingAlgorithm(OrderingAlgorithmType.NO_OP)
                .setTranslatedException((Exception) cause)
                .setTryNextServer(false);
    }

    private void handleCommunicationException(LdapSearchExceptionHandlingResponse response, Throwable cause) {
        log.error("Error in communicating with LDAP server: {}", cause.getMessage());
        response.setOrderingAlgorithm(OrderingAlgorithmType.PUT_LAST).setTryNextServer(true).setTranslatedException((Exception) cause);
    }

    private void handleAuthenticationException(LdapSearchExceptionHandlingResponse response) {
        log.error("Ldap authentication failed. Please check that the login name, password and path are correct.");
        AuthenticationResultException ex =
                new AuthenticationResultException(AuthenticationResult.OTHER);
        response.setOrderingAlgorithm(OrderingAlgorithmType.NO_OP)
                .setTranslatedException(ex)
                .setTryNextServer(false);
    }

    private void handleEngineDirectoryServiceException(LdapSearchExceptionHandlingResponse response, Throwable cause) {
        response.setTranslatedException((AuthenticationResultException) cause);
        switch (((AuthenticationResultException) cause).getResult()) {
        // connection error or timeout indicates problems with the sever so handling the same.
        case CONNECTION_ERROR:
        case CONNECTION_TIMED_OUT:
        case CLOCK_SKEW_TOO_GREAT:
            response.setOrderingAlgorithm(OrderingAlgorithmType.PUT_LAST).setTryNextServer(true);
            break;
        default:
            response.setOrderingAlgorithm(OrderingAlgorithmType.NO_OP).setTryNextServer(false);
            break;
        }
    }
}
