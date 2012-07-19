package org.ovirt.engine.ui.webadmin.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ErrorHandler;
import org.ovirt.engine.ui.webadmin.plugin.ui.PluginUiFunctions;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.inject.Inject;

/**
 * The main component of WebAdmin UI plugin infrastructure.
 * <p>
 * This class has following responsibilities:
 * <ul>
 * <li>create and expose plugin API
 * <li>define and load plugins
 * <li>enforce standard plugin lifecycle
 * </ul>
 * <p>
 * Should be bound as GIN eager singleton, created early on during application startup.
 */
public class PluginManager {

    private static final Logger logger = Logger.getLogger(PluginManager.class.getName());

    // Maps plugin names to corresponding object representations
    private final Map<String, Plugin> plugins = new HashMap<String, Plugin>();

    // Controls plugin invocation, allowing WebAdmin to call plugins only in a specific context
    private boolean canInvokePlugins = false;

    private final PluginUiFunctions uiFunctions;

    @Inject
    public PluginManager(PluginUiFunctions uiFunctions) {
        this.uiFunctions = uiFunctions;
        exposePluginApi();
        defineAndLoadPlugins();
    }

    Plugin getPlugin(String pluginName) {
        return plugins.get(pluginName);
    }

    Collection<Plugin> getPlugins() {
        return plugins.values();
    }

    void addPlugin(Plugin plugin) {
        plugins.put(plugin.getMetaData().getName(), plugin);
    }

    /**
     * Defines all plugins that were detected when serving WebAdmin host page, and loads them as necessary.
     */
    void defineAndLoadPlugins() {
        PluginDefinitions definitions = PluginDefinitions.instance();

        if (definitions != null) {
            JsArray<PluginMetaData> metaDataArray = definitions.getMetaDataArray();

            for (int i = 0; i < metaDataArray.length(); i++) {
                PluginMetaData pluginMetaData = metaDataArray.get(i);

                if (pluginMetaData != null) {
                    defineAndLoadPlugin(pluginMetaData);
                }
            }
        }
    }

    /**
     * Defines a plugin from the given meta-data, and loads it as necessary.
     */
    void defineAndLoadPlugin(PluginMetaData pluginMetaData) {
        String pluginName = pluginMetaData.getName();
        String pluginHostPageUrl = pluginMetaData.getHostPageUrl();

        if (pluginName == null || pluginName.trim().isEmpty()) {
            logger.warning("Plugin name cannot be null or empty"); //$NON-NLS-1$
            return;
        } else if (pluginHostPageUrl == null || pluginHostPageUrl.trim().isEmpty()) {
            logger.warning("Plugin [" + pluginName + "] has null or empty host page URL"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        } else if (getPlugin(pluginName) != null) {
            logger.warning("Plugin [" + pluginName + "] is already defined"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Create an iframe element used to load the plugin host page
        IFrameElement iframe = Document.get().createIFrameElement();
        iframe.setSrc(pluginHostPageUrl);
        iframe.setFrameBorder(0);
        iframe.getStyle().setPosition(Position.ABSOLUTE);
        iframe.getStyle().setWidth(0, Unit.PT);
        iframe.getStyle().setHeight(0, Unit.PT);
        iframe.getStyle().setBorderStyle(BorderStyle.NONE);

        Plugin plugin = new Plugin(pluginMetaData, iframe);
        addPlugin(plugin);
        logger.info("Plugin [" + pluginName + "] is defined to be loaded from URL " + pluginHostPageUrl); //$NON-NLS-1$ //$NON-NLS-2$

        if (pluginMetaData.isEnabled()) {
            loadPlugin(plugin);
        }
    }

    /**
     * Loads the given plugin by attaching the corresponding iframe element to DOM.
     */
    void loadPlugin(Plugin plugin) {
        if (plugin.isInState(PluginState.DEFINED)) {
            logger.info("Loading plugin [" + plugin.getMetaData().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            Document.get().getBody().appendChild(plugin.getIFrameElement());
            plugin.markAsLoading();
        }
    }

    /**
     * Called when WebAdmin enters the state that allows plugins to be invoked.
     */
    public void enablePluginInvocation() {
        canInvokePlugins = true;

        // Try to initialize all plugins which are currently ready
        for (Plugin plugin : getPlugins()) {
            if (plugin.isInState(PluginState.READY)) {
                initPlugin(plugin.getMetaData().getName());
            }
        }
    }

    /**
     * Called when WebAdmin leaves the state that allows plugins to be invoked.
     */
    public void disablePluginInvocation() {
        canInvokePlugins = false;
    }

    /**
     * Invokes an event handler function on all plugins which are currently {@linkplain PluginState#IN_USE in use}.
     * <p>
     * {@code functionArgs} represents the argument list to use when calling given function (can be {@code null}).
     * <p>
     * If the function fails due to uncaught exception for the given plugin, that plugin will be
     * {@linkplain PluginState#FAILED removed from service}.
     */
    public void invokePlugins(String functionName, JsArray<?> functionArgs) {
        if (canInvokePlugins) {
            for (Plugin plugin : getPlugins()) {
                if (plugin.isInState(PluginState.IN_USE)) {
                    if (!invokePlugin(plugin, functionName, functionArgs)) {
                        logger.warning("Failed to invoke plugin [" + plugin.getMetaData().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                        pluginFailed(plugin);
                    }
                }
            }
        }
    }

    /**
     * Invokes an event handler function on the given plugin.
     * <p>
     * No checks are performed here, make sure to call this method only in a context that fits the general plugin
     * lifecycle.
     * <p>
     * Returns {@code true} if the function completed successfully, or {@code false} if an exception escaped the
     * function call.
     */
    boolean invokePlugin(final Plugin plugin, final String functionName, JsArray<?> functionArgs) {
        return plugin.getEventHandlerFunction(functionName).invoke(functionArgs, new ErrorHandler() {
            @Override
            public void onError(String message) {
                logger.severe("Exception caught while invoking event handler function [" + functionName //$NON-NLS-1$
                        + "] for plugin [" + plugin.getMetaData().getName() + "]: " + message); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
    }

    /**
     * Returns {@code true} when the given plugin can perform actions through the API.
     * <p>
     * More precisely, returns {@code true} when all of the following conditions are met:
     * <ul>
     * <li>WebAdmin is currently in state that allows plugins to be invoked
     * <li>the plugin is either {@linkplain PluginState#INITIALIZING initializing} (actions performed from UiInit
     * function), or {@linkplain PluginState#IN_USE in use} (actions performed from other event handler functions)
     * </ul>
     */
    boolean canDoPluginAction(String pluginName) {
        Plugin plugin = getPlugin(pluginName);
        boolean pluginInitializingOrInUse = plugin != null
                ? plugin.isInState(PluginState.INITIALIZING) || plugin.isInState(PluginState.IN_USE) : false;
        return canInvokePlugins && pluginInitializingOrInUse;
    }

    /**
     * Registers an event handler object (object containing plugin event handler functions) for the given plugin.
     */
    void registerPluginEventHandlerObject(String pluginName, JavaScriptObject pluginEventHandlerObject) {
        Plugin plugin = getPlugin(pluginName);

        if (plugin != null && pluginEventHandlerObject != null) {
            plugin.setEventHandlerObject(pluginEventHandlerObject);
            logger.info("Plugin [" + pluginName + "] has registered the event handler object"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Indicates that the given plugin is {@linkplain PluginState#READY ready for use}.
     */
    void pluginReady(String pluginName) {
        Plugin plugin = getPlugin(pluginName);

        if (plugin != null && plugin.isInState(PluginState.LOADING)) {
            if (plugin.getEventHandlerObject() == null) {
                logger.warning("Plugin [" + pluginName //$NON-NLS-1$
                        + "] reports in as ready, but has no event handler object assigned"); //$NON-NLS-1$
                return;
            }

            plugin.markAsReady();
            logger.info("Plugin [" + pluginName + "] reports in as ready"); //$NON-NLS-1$ //$NON-NLS-2$

            // Try to initialize the plugin, since the plugin might report in as ready
            // after WebAdmin enters the state that allows plugins to be invoked
            initPlugin(pluginName);
        }
    }

    /**
     * Attempts to {@linkplain PluginState#INITIALIZING initialize} the given plugin by calling UiInit event handler
     * function on the corresponding event handler object.
     * <p>
     * The UiInit function will be called just once during the lifetime of a plugin. More precisely, UiInit function
     * will be called:
     * <ul>
     * <li>after the plugin reports in as {@linkplain PluginState#READY ready} <b>and</b> WebAdmin enters the state that
     * allows plugins to be invoked
     * <li>before any other event handler functions are invoked by the plugin infrastructure
     * </ul>
     * <p>
     * If UiInit function fails due to uncaught exception, the plugin will be {@linkplain PluginState#FAILED removed
     * from service}.
     */
    void initPlugin(String pluginName) {
        Plugin plugin = getPlugin(pluginName);

        if (canInvokePlugins && plugin != null && plugin.isInState(PluginState.READY)) {
            logger.info("Initializing plugin [" + pluginName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            plugin.markAsInitializing();

            if (invokePlugin(plugin, "UiInit", null)) { //$NON-NLS-1$
                plugin.markAsInUse();
                logger.info("Plugin [" + pluginName + "] is initialized and in use now"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                logger.warning("Failed to initialize plugin [" + pluginName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                pluginFailed(plugin);
            }
        }
    }

    /**
     * Removes the given plugin from service due to plugin failure.
     */
    void pluginFailed(Plugin plugin) {
        Document.get().getBody().removeChild(plugin.getIFrameElement());
        plugin.markAsFailed();
        logger.warning("Plugin [" + plugin.getMetaData().getName() + "] removed from service due to failure"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the configuration object associated with the given plugin, or {@code null} if no such object exists.
     */
    JavaScriptObject getConfigObject(String pluginName) {
        Plugin plugin = getPlugin(pluginName);
        return plugin != null ? plugin.getMetaData().getConfigObject() : null;
    }

    private native void exposePluginApi() /*-{
        var ctx = this;
        var uiFunctions = ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::uiFunctions;

        var canDoPluginAction = function(pluginName) {
            return ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::canDoPluginAction(Ljava/lang/String;)(pluginName);
        };

        var getEntityType = function(entityTypeName) {
            return @org.ovirt.engine.ui.webadmin.plugin.entity.EntityType::from(Ljava/lang/String;)(entityTypeName);
        };

        // Define pluginApi function used to construct specific Plugin API instances
        var pluginApi = function(pluginName) {
            return new pluginApi.fn.init(pluginName);
        };

        // Define pluginApi.fn as an alias to pluginApi prototype
        pluginApi.fn = pluginApi.prototype = {

            pluginName: null, // Initialized in constructor function

            // Constructor function
            init: function(pluginName) {
                this.pluginName = pluginName;
                return this;
            },

            // Registers plugin event handler functions for later invocation
            register: function(pluginEventHandlerObject) {
                ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::registerPluginEventHandlerObject(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(this.pluginName,pluginEventHandlerObject);
            },

            // Indicates that the plugin is ready for use
            ready: function() {
                ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::pluginReady(Ljava/lang/String;)(this.pluginName);
            },

            // Returns the configuration object associated with the plugin
            configObject: function() {
                return ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::getConfigObject(Ljava/lang/String;)(this.pluginName);
            },

            // Adds new main tab that shows contents of the given URL
            addMainTab: function(label, historyToken, contentUrl) {
                if (canDoPluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.ui.PluginUiFunctions::addMainTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(label,historyToken,contentUrl);
                }
            },

            // Adds new main tab action button
            addMainTabActionButton: function(entityTypeName, label, actionButtonInterface) {
                if (canDoPluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.ui.PluginUiFunctions::addMainTabActionButton(Lorg/ovirt/engine/ui/webadmin/plugin/entity/EntityType;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/ui/ActionButtonInterface;)(getEntityType(entityTypeName),label,actionButtonInterface);
                }
            }

        };

        // Give init function the pluginApi prototype for later instantiation
        pluginApi.fn.init.prototype = pluginApi.fn;

        // Expose pluginApi function as a global object
        $wnd.pluginApi = pluginApi;
    }-*/;

}
