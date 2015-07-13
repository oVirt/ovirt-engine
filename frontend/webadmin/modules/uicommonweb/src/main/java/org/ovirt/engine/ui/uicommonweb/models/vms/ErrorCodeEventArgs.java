package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicompat.EventArgs;

public final class ErrorCodeEventArgs extends EventArgs {
    private int privateErrorCode;

    public int getErrorCode() {
        return privateErrorCode;
    }

    private void setErrorCode(int value) {
        privateErrorCode = value;
    }

    public ErrorCodeEventArgs(int errorCode) {
        setErrorCode(errorCode);
    }
}
