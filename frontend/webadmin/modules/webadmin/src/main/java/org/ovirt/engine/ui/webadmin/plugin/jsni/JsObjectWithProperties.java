package org.ovirt.engine.ui.webadmin.plugin.jsni;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

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
    protected native final String getValueAsString(String key, String defaultValue) /*-{
        return (this[key] != null && typeof this[key] === 'string') ? this[key] : defaultValue;
    }-*/;

    /**
     * Returns the value for the given key as Double.
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected native final Double getValueAsDouble(String key, Double defaultValue) /*-{
        return (this[key] != null && typeof this[key] === 'number') ? @java.lang.Double::valueOf(D)(this[key]) : defaultValue;
    }-*/;

    /**
     * Returns the value for the given key as Boolean.
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected native final  Boolean getValueAsBoolean(String key, Boolean defaultValue) /*-{
        return (this[key] != null && typeof this[key] === 'boolean') ? @java.lang.Boolean::valueOf(Z)(this[key]) : defaultValue;
    }-*/;

    /**
     * Returns the value for the given key as native JS array object.
     * <p>
     * Returns {@code defaultValue} on missing key, {@code null} value or wrong value type.
     */
    protected native final <T extends JavaScriptObject> JsArray<T> getValueAsArray(String key, JsArray<T> defaultValue) /*-{
        return (this[key] != null && @org.ovirt.engine.ui.webadmin.plugin.jsni.JsArrayHelper::isArray(Lcom/google/gwt/core/client/JavaScriptObject;)(this[key])) ? this[key] : defaultValue;
    }-*/;

}
