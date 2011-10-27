package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IRSErrorImageNotExistException extends IRSErrorException implements java.io.Serializable {
    // protected IRSErrorImageNotExistException(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    public IRSErrorImageNotExistException(RuntimeException baseException) {
        super(baseException);
    }

    public IRSErrorImageNotExistException(String errorStr) {
        super(errorStr);

    }
}
