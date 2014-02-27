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
     * Creates a {@link JsArray} containing mixed values.
     * <p>
     * Supported value types:
     * <ul>
     * <li>{@link JavaScriptObject}, maps to native JS object
     * <li>String, maps to JS {@code string}
     * <li>Double, maps to JS {@code number}
     * <li>Boolean, maps to JS {@code boolean}
     * </ul>
     */
    public static JsArray<JavaScriptObject> createMixedArray(Object... values) {
        JsArray<JavaScriptObject> array = JavaScriptObject.createArray().cast();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                Object obj = values[i];
                if (obj instanceof JavaScriptObject) {
                    array.push((JavaScriptObject) obj);
                } else if (obj instanceof String) {
                    pushString(array, (String) obj);
                } else if (obj instanceof Double) {
                    pushNumber(array, (Double) obj);
                } else if (obj instanceof Boolean) {
                    pushBoolean(array, (Boolean) obj);
                }
            }
        }
        return array;
    }

    private static native void pushString(JavaScriptObject arrayObj, String value) /*-{
        arrayObj[arrayObj.length] = value;
    }-*/;

    private static native void pushNumber(JavaScriptObject arrayObj, Double value) /*-{
        arrayObj[arrayObj.length] = value.@java.lang.Double::doubleValue()();
    }-*/;

    private static native void pushBoolean(JavaScriptObject arrayObj, Boolean value) /*-{
        arrayObj[arrayObj.length] = value.@java.lang.Boolean::booleanValue()();
    }-*/;

    /**
     * Casts the given native array object into {@link JsArray} representation.
     * <p>
     * Returns {@code null} if {@code arrayObj} is not a native array object.
     */
    private static native JsArray<?> toGenericArray(JavaScriptObject arrayObj) /*-{
        return (Object.prototype.toString.call(arrayObj) === '[object Array]') ? arrayObj : null;
    }-*/;

}
