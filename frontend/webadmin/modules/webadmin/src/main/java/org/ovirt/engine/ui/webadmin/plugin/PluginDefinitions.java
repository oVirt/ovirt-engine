package org.ovirt.engine.ui.webadmin.plugin;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type for {@code pluginDefinitions} global JS object.
 * <p>
 * Serves as {@link PluginMetaData} aggregate object using native array representation. The order of objects contained
 * in the array represents the order in which corresponding plugins should be loaded on WebAdmin startup.
 */
public final class PluginDefinitions extends JavaScriptObject {

    protected PluginDefinitions() {
    }

    public static native PluginDefinitions instance() /*-{
        return $wnd.pluginDefinitions;
    }-*/;

    public native JsArray<PluginMetaData> getMetaDataArray()  /*-{
        return this;
    }-*/;

}
