package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

public class IrsSpmStartFailedException extends IRSErrorException implements Serializable {
    // protected IrsSpmStartFailedException(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    //
    public IrsSpmStartFailedException(RuntimeException baseException) {
        super(baseException);
    }

    public IrsSpmStartFailedException(String errorStr) {
        super(errorStr);
    }

    public IrsSpmStartFailedException() {
        this("SpmStart failed");
    }
}
