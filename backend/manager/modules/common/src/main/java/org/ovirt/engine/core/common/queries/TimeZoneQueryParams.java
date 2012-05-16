package org.ovirt.engine.core.common.queries;


public class TimeZoneQueryParams extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1L;
    private boolean windowsOS = true;

    public TimeZoneQueryParams() {
    }

    public boolean isWindowsOS() {
        return windowsOS;
    }

    public void setWindowsOS(boolean windowsOS) {
        this.windowsOS = windowsOS;
    }
}
