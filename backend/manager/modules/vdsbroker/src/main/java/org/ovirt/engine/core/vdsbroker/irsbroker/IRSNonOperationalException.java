package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IRSNonOperationalException extends IRSErrorException implements java.io.Serializable {
    // protected IRSNonOperationalException(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    public IRSNonOperationalException(RuntimeException baseException) {
        super(baseException);
    }

    public IRSNonOperationalException(String errorStr) {
        super("IRSNonOperationalException: " + errorStr);

    }
}
