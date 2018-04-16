package org.ovirt.engine.ui.webadmin.plugin.jsni;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

/**
 * Simple wrapper around a native JS object providing type-safe access to its properties.
 */
public abstract class JsObjectWithProperties extends JavaScriptObject {

    protected JsObjectWithProperties() {
    }

    /**
     * Returns the value for the given key as String.
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected final native String getValueAsString(String key, String defaultValue) /*-{
        return (this[key] != null && typeof this[key] === 'string') ? this[key] : defaultValue;
    }-*/;

    /**
     * Returns the value for the given key as Double (equivalent to native JS {@code number}).
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected final native Double getValueAsDouble(String key, Double defaultValue) /*-{
        return (this[key] != null && typeof this[key] === 'number') ? @java.lang.Double::valueOf(D)(this[key]) : defaultValue;
    }-*/;

    /**
     * Returns the value for the given key as Integer (converted from native JS {@code number}).
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected final Integer getValueAsInteger(String key, Integer defaultValue) {
        return getValueAsDouble(key, defaultValue.doubleValue()).intValue();
    }

    /**
     * Returns the value for the given key as Boolean.
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected final native Boolean getValueAsBoolean(String key, Boolean defaultValue) /*-{
        return (this[key] != null && typeof this[key] === 'boolean') ? @java.lang.Boolean::valueOf(Z)(this[key]) : defaultValue;
    }-*/;

    /**
     * Returns the value for the given key as native JS array object.
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected final native <T extends JavaScriptObject> JsArray<T> getValueAsArray(String key, JsArray<T> defaultValue) /*-{
        return (this[key] != null && Object.prototype.toString.call(this[key]) === '[object Array]') ? this[key] : defaultValue;
    }-*/;

    /**
     * Returns the value for the given key as native JS array object containing String elements only.
     * <p>
     * Returns empty array in following situations:
     * <ul>
     * <li>missing key, {@code null} value or wrong value type
     * <li>the underlying array contains no String elements
     * </ul>
     * <p>
     * Returns single-element array if the underlying value is String.
     */
    protected final native JsArrayString getValueAsStringArray(String key) /*-{
        var result = [];

        if (this[key] != null && Object.prototype.toString.call(this[key]) === '[object Array]') {
            for (var i = 0; i < this[key].length; i++) {
                var element = this[key][i];
                if (element != null && typeof element === 'string') {
                    result[result.length] = element;
                }
            }
        } else if (this[key] != null && typeof this[key] === 'string') {
            result[result.length] = this[key];
        }

        return result;
    }-*/;

    /**
     * Returns the value for the given key as Enum via {@link Enum#valueOf(Class, String) Enum.valueOf} method.
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected final native <T extends Enum<T>> T getValueAsEnum(String key, Class<T> enumType, T defaultValue) /*-{
        if (this[key] != null && typeof this[key] === 'string') {
            try {
                return @java.lang.Enum::valueOf(Ljava/lang/Class;Ljava/lang/String;)(enumType,this[key]);
            } catch (e) {
                // Failed to retrieve enum constant
            }
        }
        return defaultValue;
    }-*/;

}
