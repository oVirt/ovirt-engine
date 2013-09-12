package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

public class IRSNoMasterDomainException extends IRSErrorException implements Serializable {
    // protected IRSNoMasterDomainException(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    public IRSNoMasterDomainException(RuntimeException baseException) {
        super(baseException);
    }

    public IRSNoMasterDomainException(String errorStr) {
        super("IRSNoMasterDomainException: " + errorStr);
    }
}
