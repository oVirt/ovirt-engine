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

    private native String getValue() /*-{
        return this.value;
    }-*/;

    public String getPath() {
        String value = getValue();
        assert value != null : "Missing baseContextPath JS object in host page"; //$NON-NLS-1$
        assert value.startsWith("/") : "Value of baseContextPath must start with '/' character"; //$NON-NLS-1$ //$NON-NLS-2$
        return value;
    }

    public String getRelativePath() {
        String path = getPath();
        return path.startsWith("/") ? path.substring(1) : path; //$NON-NLS-1$
    }

}
