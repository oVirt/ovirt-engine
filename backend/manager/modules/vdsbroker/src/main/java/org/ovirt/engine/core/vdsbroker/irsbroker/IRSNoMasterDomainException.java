package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IRSNoMasterDomainException extends IRSErrorException implements java.io.Serializable {
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
