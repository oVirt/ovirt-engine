package org.ovirt.engine.ui.webadmin.plugin;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Contains meta-data describing a UI plugin.
 * <p>
 * We expect the meta-data to be read during WebAdmin startup as part of {@link PluginDefinitions}.
 */
public final class PluginMetaData extends JavaScriptObject {

    protected PluginMetaData() {
    }

    /**
     * A name that uniquely identifies the plugin.
     */
    public native String getName()  /*-{
        return this.name;
    }-*/;

    /**
     * URL of plugin host page that invokes the plugin code.
     */
    public native String getHostPageUrl() /*-{
        return this.url;
    }-*/;

    /**
     * Configuration object associated with the plugin.
     */
    public native JavaScriptObject getConfigObject() /*-{
        return this.config;
    }-*/;

    /**
     * Indicates whether the plugin host page is loaded asynchronously (if {@code false},
     * WebAdmin pre-loads the plugin host page before loading the main UI).
     */
    public native boolean isLazyLoad() /*-{
        return this.lazyLoad;
    }-*/;

    /**
     * Indicates whether the plugin should be loaded on WebAdmin startup.
     */
    public native boolean isEnabled() /*-{
        return this.enabled;
    }-*/;

}
