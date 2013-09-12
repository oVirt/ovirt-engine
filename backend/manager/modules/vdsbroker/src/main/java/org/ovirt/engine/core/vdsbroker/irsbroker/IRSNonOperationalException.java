package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

public class IRSNonOperationalException extends IRSErrorException implements Serializable {
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
