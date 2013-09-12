package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

public class IRSUnicodeArgumentException extends IRSGenericException implements Serializable {
    // protected IRSUnicodeArgumentException(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    public IRSUnicodeArgumentException(RuntimeException baseException) {
        super("IRSUnicodeArgumentException: ", baseException);
    }

    public IRSUnicodeArgumentException(String errorStr) {
        super("IRSUnicodeArgumentException: " + errorStr);

    }
}
