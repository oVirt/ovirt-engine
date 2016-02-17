package org.ovirt.engine.ui.common;

import com.google.gwt.core.client.JavaScriptObject;

public class DisplayUncaughtUIExceptions extends JavaScriptObject {

    protected DisplayUncaughtUIExceptions() {
    }

    public static native boolean getValue() /*-{
        return $wnd.displayUncaughtUIExceptions;
    }-*/;

}
