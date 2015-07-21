package org.ovirt.engine.ui.frontend.utils;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for JS object containing single {@code value} property of type String,
 * for example:
 * <pre>
 *  { value: "anything" }
 * </pre>
 */
public abstract class JsSingleValueStringObject extends JavaScriptObject {

    protected JsSingleValueStringObject() {
    }

    public static native String getValueFrom(String globalObjName) /*-{
        var obj = $wnd[globalObjName];
        return obj && obj.value;
    }-*/;

}
