package org.ovirt.engine.ui.frontend.utils;

import com.google.gwt.core.client.JavaScriptObject;

/**
* Overlay type for {@code baseContextPath} global JS object.
*/
public final class BaseContextPathData extends JavaScriptObject {

    protected BaseContextPathData() {
    }

    public static native BaseContextPathData getInstance() /*-{
    return $wnd.baseContextPath;
    }-*/;

    public native String getPath() /*-{
    return this.value;
    }-*/;

}
