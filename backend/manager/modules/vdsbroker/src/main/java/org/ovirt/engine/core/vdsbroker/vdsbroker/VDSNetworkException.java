package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.Serializable;

public class VDSNetworkException extends VDSGenericException implements Serializable {

    private static final long serialVersionUID = -486466100359278549L;

    public VDSNetworkException(Throwable baseException) {
        super(baseException);
    }

    public VDSNetworkException(String errorStr) {
        super("VDSNetworkException: " + errorStr);

    }
}
