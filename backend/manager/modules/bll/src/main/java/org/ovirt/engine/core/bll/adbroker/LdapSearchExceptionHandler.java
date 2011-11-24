package org.ovirt.engine.core.bll.adbroker;

import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.naming.OperationNotSupportedException;
import javax.security.sasl.SaslException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;

public class LdapSearchExceptionHandler implements ExceptionHandler<LdapSearchExceptionHandlingResponse> {

    private static final LogCompat log = LogFactoryCompat.getLog(LdapSearchExceptionHandler.class);

    @Override
    public LdapSearchExceptionHandlingResponse handle(Exception e) {
        LdapSearchExceptionHandlingResponse response = new LdapSearchExceptionHandlingResponse();
        if (e instanceof TimeoutException) { // LDAP server didn't respond fast enough
            handleTimeout(response);
        } else if (e instanceof ExecutionException) { // thrown by FutureTask and may contain relevant Runtime
                                                      // Exceptions.
            Throwable cause = e.getCause();
            if (cause != null) {
                if (cause instanceof EngineDirectoryServiceException) {
                    handleEngineDirectoryServiceException(response, cause);
                } else if (cause instanceof AuthenticationException) {
                    handleAuthenticationException(response);
                } else if (cause instanceof CommunicationException) {
                    handleCommunicationException(response, cause);
                } else if (cause instanceof InterruptedException) {
                    handleInterruptException(response, cause);
                } else {
                    handleGeneralException(response, e);
                }
            } else {
                for (Throwable throwable : ExceptionUtils.getThrowables(e)) {
                    if ((throwable instanceof SaslException || throwable instanceof ConnectException)) {
                        handleSaslException(response, cause);
                    } else if (throwable instanceof OperationNotSupportedException) {
                        handleOperationException(response, throwable);
                    }
                }
            }
        }
        return response;

    }

    private void handleGeneralException(LdapSearchExceptionHandlingResponse response, Exception e) {
        response.setTranslatedException(e).setServerScore(Score.HIGH);
    }

    private void handleSaslException(LdapSearchExceptionHandlingResponse response, Throwable cause) {
        response.setServerScore(Score.LOW)
                .setTranslatedException(new EngineDirectoryServiceException(AuthenticationResult.CONNECTION_ERROR,
                        "General connection problem due to " + cause))
                .setTryNextServer(true);
    }

    private void handleOperationException(LdapSearchExceptionHandlingResponse response,
            Throwable throwable) {
        response.setServerScore(Score.HIGH)
                .setTranslatedException(new EngineDirectoryServiceException(AuthenticationResult.USER_ACCOUNT_DISABLED_OR_LOCKED,
                        throwable))
                .setTryNextServer(false);
    }

    private void handleInterruptException(LdapSearchExceptionHandlingResponse response, Throwable cause) {
        response.setServerScore(Score.HIGH)
                .setTranslatedException((Exception) cause)
                .setTryNextServer(false);
    }

    private void handleCommunicationException(LdapSearchExceptionHandlingResponse response, Throwable cause) {
        log.error("Error in communicating with LDAP server " + cause.getMessage());
        response.setServerScore(Score.LOW).setTryNextServer(true).setTranslatedException((Exception) cause);
    }

    private void handleAuthenticationException(LdapSearchExceptionHandlingResponse response) {
        log.error("Ldap authentication failed. Please check that the login name , password and path are correct. ");
        EngineDirectoryServiceException ex =
                new EngineDirectoryServiceException(AuthenticationResult.OTHER);
        response.setServerScore(Score.HIGH)
                .setTranslatedException(ex)
                .setTryNextServer(false);
    }

    private void handleEngineDirectoryServiceException(LdapSearchExceptionHandlingResponse response, Throwable cause) {
        response.setTranslatedException((EngineDirectoryServiceException) cause);
        switch (((EngineDirectoryServiceException) cause).getResult()) {
        // connection error or timeout indicates problems with the sever so handling the same.
        case CONNECTION_ERROR:
        case CONNECTION_TIMED_OUT:
        case CLOCK_SKEW_TOO_GREAT:
            response.setServerScore(Score.LOW).setTryNextServer(true);
            break;
        default:
            response.setServerScore(Score.HIGH).setTryNextServer(false);
            break;
        }
    }

    private void handleTimeout(LdapSearchExceptionHandlingResponse response) {
        response.setTryNextServer(true)
                .setTranslatedException(
                        new EngineDirectoryServiceException(AuthenticationResult.CONNECTION_TIMED_OUT,
                                "Connection to to server has timed out."))
                .setServerScore(Score.LOW);
    }

}
