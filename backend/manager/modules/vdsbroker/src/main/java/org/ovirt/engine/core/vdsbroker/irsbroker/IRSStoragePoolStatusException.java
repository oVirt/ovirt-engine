package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IRSStoragePoolStatusException extends IRSErrorException implements java.io.Serializable {
    // protected IRSStoragePoolStatusException(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    public IRSStoragePoolStatusException(RuntimeException baseException) {
        super(baseException);
    }

    public IRSStoragePoolStatusException(String errorStr) {
        super("IRSStoragePoolStatusException: " + errorStr);
    }
}
