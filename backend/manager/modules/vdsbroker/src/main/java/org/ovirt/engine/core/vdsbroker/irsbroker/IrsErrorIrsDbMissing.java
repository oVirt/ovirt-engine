package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IrsErrorIrsDbMissing extends IRSErrorException implements java.io.Serializable {
    // protected IrsErrorIrsDbMissing(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public IrsErrorIrsDbMissing(RuntimeException baseException) {
        super(baseException);
    }

    public IrsErrorIrsDbMissing(String errorStr) {
        super(errorStr);

    }
}
