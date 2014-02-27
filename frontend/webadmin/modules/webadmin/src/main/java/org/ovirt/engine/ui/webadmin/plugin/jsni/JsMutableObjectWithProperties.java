package org.ovirt.engine.ui.webadmin.plugin.jsni;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Extension of {@link JsObjectWithProperties} that allows setting typed properties.
 */
public abstract class JsMutableObjectWithProperties extends JsObjectWithProperties {

    protected JsMutableObjectWithProperties() {
    }

    /**
     * Sets the value for the given key as native JS object.
     */
    protected final native void setValueAsJavaScriptObject(String key, JavaScriptObject value) /*-{
        this[key] = value;
    }-*/;

    /**
     * Sets the value for the given key as String (maps to JS {@code string}).
     */
    protected final native void setValueAsString(String key, String value) /*-{
        this[key] = value;
    }-*/;

    /**
     * Sets the value for the given key as Double (maps to JS {@code number}).
     */
    protected final native void setValueAsDouble(String key, Double value) /*-{
        this[key] = value.@java.lang.Double::doubleValue()();
    }-*/;

    /**
     * Sets the value for the given key as Boolean (maps to JS {@code boolean}).
     */
    protected final native void setValueAsBoolean(String key, Boolean value) /*-{
        this[key] = value.@java.lang.Boolean::booleanValue()();
    }-*/;

}
