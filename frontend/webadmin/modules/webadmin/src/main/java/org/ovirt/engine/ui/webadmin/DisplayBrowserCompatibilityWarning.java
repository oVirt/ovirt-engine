package org.ovirt.engine.ui.webadmin;

import com.google.gwt.core.client.JavaScriptObject;

public final class DisplayBrowserCompatibilityWarning extends JavaScriptObject {

    protected DisplayBrowserCompatibilityWarning() {
    }

    public static native boolean getValue() /*-{
        return $wnd.displaySupportedBrowserWarning;
    }-*/;

}
