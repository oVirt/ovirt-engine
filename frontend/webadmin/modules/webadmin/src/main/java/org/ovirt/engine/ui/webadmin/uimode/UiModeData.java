package org.ovirt.engine.ui.webadmin.uimode;

import org.ovirt.engine.ui.uicommonweb.uimode.UiMode;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for {@code applicationMode} global JS object.
 */
public final class UiModeData extends JavaScriptObject {

    protected UiModeData() {
    }

    public static native UiModeData instance() /*-{
        return $wnd.applicationMode;
    }-*/;

    private native String getValue() /*-{
        return this.value;
    }-*/;

    public UiMode getUiMode() {
        return UiMode.from(Integer.parseInt(getValue()));
    }

}
