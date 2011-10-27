package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IRSUnicodeArgumentException extends IRSGenericException implements java.io.Serializable {
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
