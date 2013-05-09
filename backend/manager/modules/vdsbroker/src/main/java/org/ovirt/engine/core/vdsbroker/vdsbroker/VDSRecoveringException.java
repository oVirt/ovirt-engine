package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.VdcBllErrors;

public class VDSRecoveringException extends VDSErrorException {
    public VDSRecoveringException(VdcBllErrors errorCode) {
        super(errorCode);
    }

    public VDSRecoveringException(VdcBllErrors errorCode, String errorStr) {
        super(errorCode, errorStr);
    }
}
