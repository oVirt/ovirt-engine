package org.ovirt.engine.ui.webadmin.plugin.restapi;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for {@code engineSessionTimeout} global JS object.
 */
public final class EngineSessionTimeoutData extends JavaScriptObject {

    protected EngineSessionTimeoutData() {
    }

    public static native EngineSessionTimeoutData instance() /*-{
        return $wnd.engineSessionTimeout;
    }-*/;

    public native String getSessionTimeout() /*-{
        return this.sessionTimeout;
    }-*/;

    public native String getSessionHardLimit() /*-{
        return this.sessionHardLimit;
    }-*/;
}
