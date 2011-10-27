package org.ovirt.engine.core.vdsbroker.vdsbroker;

public class VDSTaskResultNotSuccessException extends VDSGenericException {
    // protected VDSTaskResultNotSuccessException(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    //
    public VDSTaskResultNotSuccessException(RuntimeException baseException) {
        super("VDSTaskResultNotSuccessException: ", baseException);
    }

    public VDSTaskResultNotSuccessException(String errorStr) {
        super("VDSTaskResultNotSuccessException: " + errorStr);

    }
}
