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
     * Converts the given native array object into {@link JsArray} representation.
     * <p>
     * Returns {@code null} in case {@code array} is not a native array object.
     */
    private static native JsArray<?> toGenericArray(JavaScriptObject array) /*-{
        // Safety check: the argument should be a native JavaScript array
        return (Object.prototype.toString.call(array) === '[object Array]') ? array : null;
    }-*/;

}
