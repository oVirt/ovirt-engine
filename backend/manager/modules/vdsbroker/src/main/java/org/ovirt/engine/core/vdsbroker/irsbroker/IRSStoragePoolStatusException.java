package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

public class IRSStoragePoolStatusException extends IRSErrorException implements Serializable {
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
