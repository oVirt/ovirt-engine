package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IrsErrorImportCandidateMissing extends IRSErrorException implements java.io.Serializable {
    // protected IrsErrorImportCandidateMissing(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    public IrsErrorImportCandidateMissing(RuntimeException baseException) {
        super(baseException);
    }

    public IrsErrorImportCandidateMissing(String errorStr) {
        super(errorStr);

    }
}
