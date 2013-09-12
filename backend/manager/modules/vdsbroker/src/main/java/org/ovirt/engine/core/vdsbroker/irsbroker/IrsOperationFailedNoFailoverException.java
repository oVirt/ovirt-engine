package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

public class IrsOperationFailedNoFailoverException extends IRSErrorException implements Serializable {
    public IrsOperationFailedNoFailoverException(RuntimeException baseException) {
        super(baseException);
    }

    public IrsOperationFailedNoFailoverException(String errorStr) {
        super(errorStr);
    }

    public IrsOperationFailedNoFailoverException() {
        this("Operation failed No failover will be performed");
    }
}
