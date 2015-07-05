package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.EngineError;

public class VDSRecoveringException extends VDSErrorException {
    public VDSRecoveringException(EngineError errorCode) {
        super(errorCode);
    }

    public VDSRecoveringException(EngineError errorCode, String errorStr) {
        super(errorCode, errorStr);
    }
}
