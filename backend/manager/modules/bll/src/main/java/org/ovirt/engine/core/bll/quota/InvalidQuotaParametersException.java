package org.ovirt.engine.core.bll.quota;


public class InvalidQuotaParametersException extends RuntimeException {

    public InvalidQuotaParametersException() {
    }

    public InvalidQuotaParametersException(String errorStr){
        super(errorStr);
    }

    public InvalidQuotaParametersException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }
}
