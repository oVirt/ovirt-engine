package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;

public class EngineDirectoryServiceException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2854926242091038897L;
    private AuthenticationResult result;

    public EngineDirectoryServiceException(AuthenticationResult result) {
        super(result.getDetailedMessage());
        this.result = result;
    }

    public AuthenticationResult getResult() {
        return result;
    }

    public EngineDirectoryServiceException(AuthenticationResult result,String message) {
        super(message);
        this.result = result;
    }

    public EngineDirectoryServiceException(AuthenticationResult result,Throwable cause) {
        super(result.getDetailedMessage(),cause);
        this.result = result;
    }

    public EngineDirectoryServiceException(AuthenticationResult result,String message, Throwable cause) {
        super(message, cause);
        this.result = result;
    }
}
