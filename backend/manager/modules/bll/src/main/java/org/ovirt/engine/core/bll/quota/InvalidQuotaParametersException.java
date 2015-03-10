package org.ovirt.engine.core.bll.quota;


public class InvalidQuotaParametersException extends RuntimeException {

    private static final long serialVersionUID = 5337841671396280088L;

    public InvalidQuotaParametersException() {
    }

    public InvalidQuotaParametersException(String errorStr){
        super(errorStr);
    }

    public InvalidQuotaParametersException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }
}
