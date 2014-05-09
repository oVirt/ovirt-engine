package org.ovirt.engine.ui.common.auth;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for {@code ssoToken} global JS object.
 */
public final class SSOTokenData extends JavaScriptObject {

    protected SSOTokenData() {
    }

    public static native SSOTokenData instance() /*-{
        return $wnd.ssoToken;
    }-*/;

    public native String getValue() /*-{
        return this.value;
    }-*/;

    public String getToken() {
        return getValue();
    }
}
