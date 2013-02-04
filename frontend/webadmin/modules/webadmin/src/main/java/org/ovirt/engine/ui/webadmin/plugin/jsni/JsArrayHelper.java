package org.ovirt.engine.ui.webadmin.plugin.jsni;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

/**
 * Helper class for working with {@link JsArray}.
 */
public class JsArrayHelper {

    /**
     * Creates a {@link JsArray} containing String values.
     */
    public static JsArray<?> createStringArray(String... values) {
        JsArrayString array = JavaScriptObject.createArray().cast();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                array.push(values[i]);
            }
        }
        return toGenericArray(array);
    }

    /**
     * Casts the given native array object into {@link JsArray} representation.
     * <p>
     * Returns {@code null} if {@code arrayObj} is not a native array object.
     */
    private static native JsArray<?> toGenericArray(JavaScriptObject arrayObj) /*-{
        return (@org.ovirt.engine.ui.webadmin.plugin.jsni.JsArrayHelper::isArray(Lcom/google/gwt/core/client/JavaScriptObject;)(arrayObj)) ? arrayObj : null;
    }-*/;

    /**
     * Returns {@code true} if the given JS object is a native array object.
     */
    public static native boolean isArray(JavaScriptObject obj) /*-{
        return Object.prototype.toString.call(obj) === '[object Array]';
    }-*/;

}
