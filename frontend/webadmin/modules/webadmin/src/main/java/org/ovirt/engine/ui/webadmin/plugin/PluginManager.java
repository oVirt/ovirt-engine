package org.ovirt.engine.ui.webadmin.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.DisplayUncaughtUIExceptions;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.logging.ApplicationLogManager;
import org.ovirt.engine.ui.common.widget.AlertManager;
import org.ovirt.engine.ui.webadmin.plugin.api.ApiOptions;
import org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

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
public class PluginManager implements HasHandlers {

    public interface PluginsReadyCallback {

        void onPluginsReady();

    }

    public interface PluginInvocationCondition {

        boolean canInvoke(Plugin plugin);

    }

    private static final PluginInvocationCondition INVOKE_ANY_PLUGIN = plugin -> true;

    private static final Logger logger = Logger.getLogger(PluginManager.class.getName());

    private static final Logger pluginLogger = Logger.getLogger(Plugin.class.getName());

    private ApplicationLogManager applicationLogManager;

    private AlertManager alertManager;

    // Maps plugin names to corresponding object representations
    private final Map<String, Plugin> plugins = new HashMap<>();

    // Used to track plugins that need to be pre-loaded before loading the main UI
    private final Map<String, Boolean> pluginsToPreLoad = new HashMap<>();

    // Maps plugin names to scheduled event handler functions invoked via Command interface
    private final Map<String, List<Command>> scheduledFunctionCommands = new HashMap<>();

    private final PluginUiFunctions uiFunctions;
    private final CurrentUser user;
    private final EventBus eventBus;

    // PluginsReadyCallback should be invoked only once and as early as possible
    private boolean pluginsReadyCallbackInvoked = false;
    private PluginsReadyCallback pluginsReadyCallback;

    @Inject
    public PluginManager(PluginUiFunctions uiFunctions,
            ApplicationLogManager applicationLogManager,
            AlertManager alertManager,
            CurrentUser user,
            EventBus eventBus) {
        this.uiFunctions = uiFunctions;
        this.applicationLogManager = applicationLogManager;
        this.alertManager = alertManager;
        this.user = user;
        this.eventBus = eventBus;
        exposePluginApi();
        defineAndLoadPlugins();
    }

    public void setPluginsReadyCallback(PluginsReadyCallback callback) {
        this.pluginsReadyCallback = callback;
    }

    Plugin getPlugin(String pluginName) {
        return plugins.get(pluginName);
    }

    Collection<Plugin> getPlugins() {
        return plugins.values();
    }

    void addPlugin(Plugin plugin) {
        plugins.put(plugin.getName(), plugin);
    }

    void scheduleFunctionCommand(String pluginName, Command command) {
        if (!scheduledFunctionCommands.containsKey(pluginName)) {
            scheduledFunctionCommands.put(pluginName, new ArrayList<Command>());
        }
        scheduledFunctionCommands.get(pluginName).add(command);
    }

    void invokeScheduledFunctionCommands(String pluginName) {
        List<Command> commands = scheduledFunctionCommands.get(pluginName);
        if (commands != null) {
            for (Command c : commands) {
                c.execute();
            }
        }
        scheduledFunctionCommands.remove(pluginName);
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

        Scheduler.get().scheduleDeferred(() -> maybeInvokePluginsReadyCallback());
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
        logger.fine("Plugin [" + pluginName + "] is defined to be loaded from URL " + pluginHostPageUrl); //$NON-NLS-1$ //$NON-NLS-2$

        // Load the plugin host page
        if (pluginMetaData.isEnabled()) {
            loadPlugin(plugin);

            if (plugin.shouldPreLoad()) {
                pluginsToPreLoad.put(pluginName, false);
                logger.fine("Plugin [" + pluginName + "] will be pre-loaded"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * Loads the given plugin by attaching the corresponding iframe element to DOM.
     */
    void loadPlugin(Plugin plugin) {
        if (plugin.isInState(PluginState.DEFINED)) {
            logger.fine("Loading plugin [" + plugin.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            Document.get().getBody().appendChild(plugin.getIFrameElement());
            plugin.markAsLoading();
        }
    }

    /**
     * Invokes an event handler function on all plugins which are currently {@linkplain PluginState#IN_USE in use}.
     * <p>
     * {@code functionArgs} represents the argument list to use when calling the given function (can be {@code null}).
     */
    public void invokePluginsNow(String functionName, JsArray<?> functionArgs) {
        invokePluginsNow(functionName, functionArgs, INVOKE_ANY_PLUGIN);
    }

    /**
     * Invokes an event handler function on all plugins which are currently {@linkplain PluginState#IN_USE in use} and
     * meet the given condition.
     * <p>
     * {@code functionArgs} represents the argument list to use when calling the given function (can be {@code null}).
     */
    public void invokePluginsNow(String functionName, JsArray<?> functionArgs, PluginInvocationCondition condition) {
        for (Plugin plugin : getPlugins()) {
            if (plugin.isInState(PluginState.IN_USE) && condition.canInvoke(plugin)) {
                invokePlugin(plugin, functionName, functionArgs);
            }
        }
    }

    /**
     * Invokes an event handler function on all plugins which are currently {@linkplain PluginState#IN_USE in use}, and
     * schedules invocation of the given function on all plugins that might be put in use later on.
     * <p>
     * {@code functionArgs} represents the argument list to use when calling the given function (can be {@code null}).
     */
    public void invokePluginsNowOrLater(String functionName, JsArray<?> functionArgs) {
        invokePluginsNowOrLater(functionName, functionArgs, INVOKE_ANY_PLUGIN);
    }

    /**
     * Invokes an event handler function on all plugins which are currently {@linkplain PluginState#IN_USE in use} and
     * meet the given condition, and schedules invocation of the given function on all plugins that might be put in use
     * later on.
     * <p>
     * {@code functionArgs} represents the argument list to use when calling the given function (can be {@code null}).
     */
    public void invokePluginsNowOrLater(final String functionName, final JsArray<?> functionArgs,
            final PluginInvocationCondition condition) {
        invokePluginsNow(functionName, functionArgs, condition);

        for (final Plugin plugin : getPlugins()) {
            if (!plugin.isInState(PluginState.IN_USE)) {
                scheduleFunctionCommand(plugin.getName(), () -> {
                    if (plugin.isInState(PluginState.IN_USE) && condition.canInvoke(plugin)) {
                        invokePlugin(plugin, functionName, functionArgs);
                    }
                });
            }
        }
    }

    /**
     * Invokes an event handler function on the given plugin.
     * <p>
     * No checks are performed here, make sure to call this method only in a context that fits the general plugin
     * lifecycle.
     * <p>
     * If the function fails due to uncaught exception for the given plugin, that plugin will be automatically
     * {@linkplain PluginState#FAILED removed from service}. Callers should therefore never call this method if the
     * given plugin is already out of service.
     * <p>
     * Returns {@code true} if the function completed successfully, or {@code false} if an exception escaped the
     * function call.
     */
    boolean invokePlugin(final Plugin plugin, final String functionName, JsArray<?> functionArgs) {
        final String pluginName = plugin.getName();
        logger.fine("Invoking event handler function [" + functionName + "] for plugin [" + pluginName + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        return plugin.getEventHandlerFunction(functionName).invoke(functionArgs, message -> {
            logger.severe("Exception caught while invoking event handler function [" + functionName //$NON-NLS-1$
                    + "] for plugin [" + pluginName + "]: " + message); //$NON-NLS-1$ //$NON-NLS-2$

            // Remove the given plugin from service
            Document.get().getBody().removeChild(plugin.getIFrameElement());
            plugin.markAsFailed();
            logger.warning("Plugin [" + pluginName + "] removed from service due to failure"); //$NON-NLS-1$ //$NON-NLS-2$
        });
    }

    /**
     * Returns {@code true} when the given plugin can perform actions through the API.
     * <p>
     * If {@code allowWhileLoading} is {@code false}, the plugin must be either
     * {@linkplain PluginState#INITIALIZING initializing} (actions performed from UiInit function),
     * or {@linkplain PluginState#IN_USE in use} (actions performed from other event handler functions).
     * <p>
     * If {@code allowWhileLoading} is {@code true}, above constraint is relaxed by allowing the plugin
     * to be {@linkplain PluginState#LOADING loading} (at this point, infra expects the plugin to call
     * {@code ready} API function to signal that the plugin is ready for use).
     */
    boolean validatePluginAction(String pluginName, boolean allowWhileLoading) {
        Plugin plugin = getPlugin(pluginName);
        if (plugin == null) {
            return false;
        }

        boolean isInitializingOrInUse = plugin.isInState(PluginState.INITIALIZING) || plugin.isInState(PluginState.IN_USE);
        boolean isLoading = plugin.isInState(PluginState.LOADING);
        return isInitializingOrInUse || (allowWhileLoading && isLoading);
    }

    /**
     * Registers an event handler object (object containing plugin event handler functions) for the given plugin.
     */
    void registerPluginEventHandlerObject(String pluginName, JavaScriptObject eventHandlerObject) {
        Plugin plugin = getPlugin(pluginName);
        if (plugin == null || eventHandlerObject == null) {
            return;
        }

        // Allow plugin event handler object to be set only once
        if (plugin.getEventHandlerObject() == null) {
            plugin.setEventHandlerObject(eventHandlerObject);
            logger.fine("Plugin [" + pluginName + "] has registered the event handler object"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            logger.warning("Plugin [" + pluginName + "] has already registered the event handler object"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Registers a custom API options object for the given plugin.
     */
    void registerPluginApiOptionsObject(String pluginName, ApiOptions apiOptionsObject) {
        Plugin plugin = getPlugin(pluginName);
        if (plugin == null || apiOptionsObject == null) {
            return;
        }

        plugin.setApiOptionsObject(apiOptionsObject);
        logger.fine("Plugin [" + pluginName + "] has registered custom API options object"); //$NON-NLS-1$ //$NON-NLS-2$
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
            logger.fine("Plugin [" + pluginName + "] reports in as ready"); //$NON-NLS-1$ //$NON-NLS-2$

            // Initialize the plugin once it's ready
            initPlugin(plugin);

            if (plugin.shouldPreLoad()) {
                pluginsToPreLoad.put(pluginName, true);
            }

            maybeInvokePluginsReadyCallback();
        }
    }

    /**
     * Checks if all plugins that need pre-loading have been loaded already, and if so,
     * invokes the {@link #pluginsReadyCallback}.
     */
    void maybeInvokePluginsReadyCallback() {
        if (pluginsReadyCallback != null && !pluginsReadyCallbackInvoked && !pluginsToPreLoad.containsValue(false)) {
            pluginsReadyCallback.onPluginsReady();
            pluginsReadyCallbackInvoked = true;
        }
    }

    /**
     * Attempts to {@linkplain PluginState#INITIALIZING initialize} the given plugin by calling UiInit event handler
     * function on the corresponding event handler object.
     * <p>
     * The UiInit function will be called just once during the lifetime of a plugin. More precisely, UiInit function
     * will be called:
     * <ul>
     * <li>after the plugin reports in as {@linkplain PluginState#READY ready}
     * <li>before any other event handler functions are invoked by the plugin infrastructure
     * </ul>
     * <p>
     * As part of attempting to initialize the given plugin, all event handler functions that have been
     * {@linkplain #invokePluginsNowOrLater scheduled} for such plugin will be invoked immediately after the UiInit
     * function completes successfully.
     */
    void initPlugin(Plugin plugin) {
        String pluginName = plugin.getName();

        // Try to invoke UiInit event handler function
        if (plugin.isInState(PluginState.READY)) {
            logger.fine("Initializing plugin [" + pluginName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            plugin.markAsInitializing();

            if (invokePlugin(plugin, "UiInit", null)) { //$NON-NLS-1$
                plugin.markAsInUse();
                logger.fine("Plugin [" + pluginName + "] is initialized and in use now"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        // Try to invoke all event handler functions scheduled for this plugin
        if (plugin.isInState(PluginState.IN_USE)) {
            invokeScheduledFunctionCommands(pluginName);
        }
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    /**
     * Returns the configuration object associated with the given plugin, or {@code null} if no such object exists.
     */
    JavaScriptObject getConfigObject(String pluginName) {
        Plugin plugin = getPlugin(pluginName);
        return plugin != null ? plugin.getMetaData().getConfigObject() : null;
    }

    void reportFatalError(String message) {
        Exception e = new PluginException(message);
        applicationLogManager.logUncaughtException(e);

        if (DisplayUncaughtUIExceptions.getValue()) {
            alertManager.showUncaughtExceptionAlert(e);
        }
    }

    private native void exposePluginApi() /*-{
        var ctx = this;
        var uiFunctions = ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::uiFunctions;
        var user = ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::user;
        var logger = @org.ovirt.engine.ui.webadmin.plugin.PluginManager::pluginLogger;

        var validatePluginAction = function(pluginName) {
            return ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::validatePluginAction(Ljava/lang/String;Z)(pluginName,false);
        };

        var validateSafePluginAction = function(pluginName) {
            return ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::validatePluginAction(Ljava/lang/String;Z)(pluginName,true);
        };

        var getEntityType = function(entityTypeName) {
            return @org.ovirt.engine.ui.webadmin.plugin.entity.EntityType::from(Ljava/lang/String;)(entityTypeName);
        };

        var getAlertType = function(alertTypeName) {
            return @org.ovirt.engine.ui.common.widget.panel.AlertPanel.Type::from(Ljava/lang/String;)(alertTypeName);
        };

        var sanitizeObject = function(object) {
            return (object != null) ? object : {};
        };

        var logDeprecatedCall = function(deprecated, replacement) {
            $wnd.console.warn(deprecated + " is deprecated, please use " + replacement + " instead.");
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
            register: function(eventHandlerObject) {
                ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::registerPluginEventHandlerObject(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(this.pluginName,sanitizeObject(eventHandlerObject));
            },

            // Registers custom API options object associated with the plugin
            options: function(apiOptionsObject) {
                ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::registerPluginApiOptionsObject(Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/ApiOptions;)(this.pluginName,sanitizeObject(apiOptionsObject));
            },

            // Indicates that the plugin is ready for use
            ready: function() {
                ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::pluginReady(Ljava/lang/String;)(this.pluginName);
            },

            // Returns the configuration object associated with the plugin
            configObject: function() {
                return ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::getConfigObject(Ljava/lang/String;)(this.pluginName);
            },
            addPrimaryMenuPlace: function(label, historyToken, contentUrl, options) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::addPrimaryMenuPlace(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/TabOptions;)(label,historyToken,contentUrl,sanitizeObject(options));
                }
            },
            addPrimaryMenuContainer: function(label, primaryMenuId, options) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::addPrimaryMenuContainer(Ljava/lang/String;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/TabOptions;)(label,primaryMenuId,sanitizeObject(options));
                }
            },
            addSecondaryMenuPlace: function(primaryMenuId, label, historyToken, contentUrl, options) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::addSecondaryMenu(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/TabOptions;)(primaryMenuId,label,historyToken,contentUrl,sanitizeObject(options));
                }
            },
            addMainTab: function(label, historyToken, contentUrl, options) {
                logDeprecatedCall("addMainTab", "addPrimaryMenuPlace");
                this.addPrimaryMenuPlace(label, historyToken, contentUrl, options);
            },
            addDetailPlace: function(entityTypeName, label, historyToken, contentUrl, options) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::addDetailPlace(Lorg/ovirt/engine/ui/webadmin/plugin/entity/EntityType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/TabOptions;)(getEntityType(entityTypeName),label,historyToken,contentUrl,sanitizeObject(options));
                }
            },
            addSubTab: function(entityTypeName, label, historyToken, contentUrl, options) {
                logDeprecatedCall("addSubTab", "addDetailPlace");
                this.addDetailPlace(entityTypeName, label, historyToken, contentUrl, options);
            },
            setPlaceContentUrl: function(historyToken, contentUrl) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::setPlaceContentUrl(Ljava/lang/String;Ljava/lang/String;)(historyToken,contentUrl);
                }
            },
            setTabContentUrl: function(historyToken, contentUrl) {
                logDeprecatedCall("setTabContentUrl", "setPlaceContentUrl");
                this.setPlaceContentUrl(historyToken, contentUrl);
            },
            setPlaceAccessible: function(historyToken, tabAccessible) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::setPlaceAccessible(Ljava/lang/String;Z)(historyToken,tabAccessible);
                }
            },
            setTabAccessible: function(historyToken, tabAccessible) {
                logDeprecatedCall("setTabAccessible", "setPlaceAccessible");
                this.setPlaceAccessible(historyToken, tabAccessible);
            },
            setPlaceUnloadHandler: function(historyToken, unloadHandler) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::setPlaceUnloadHandler(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(historyToken,unloadHandler);
                }
            },
            addMenuPlaceActionButton: function(entityTypeName, label, actionButtonInterface) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::addMenuPlaceActionButton(Lorg/ovirt/engine/ui/webadmin/plugin/entity/EntityType;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/ActionButtonInterface;)(getEntityType(entityTypeName),label,sanitizeObject(actionButtonInterface));
                }
            },
            addMainTabActionButton: function(entityTypeName, label, actionButtonInterface) {
                logDeprecatedCall("addMainTabActionButton", "addMenuPlaceActionButton");
                this.addMenuPlaceActionButton(entityTypeName, label, actionButtonInterface);
            },
            addDetailPlaceActionButton: function(mainEntityTypeName, detailPlaceId, label, actionButtonInterface) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::addDetailPlaceActionButton(Lorg/ovirt/engine/ui/webadmin/plugin/entity/EntityType;Ljava/lang/String;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/ActionButtonInterface;)(getEntityType(mainEntityTypeName),detailPlaceId,label,sanitizeObject(actionButtonInterface));
                }
            },
            addSubTabActionButton: function(mainEntityTypeName, detailPlaceId, label, actionButtonInterface) {
                logDeprecatedCall("addSubTabActionButton", "addDetailPlaceActionButton");
                this.addDetailPlaceActionButton(mainEntityTypeName, detailPlaceId, label, actionButtonInterface);
            },
            showDialog: function(title, dialogToken, contentUrl, width, height, options) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::showDialog(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/DialogOptions;)(title,dialogToken,contentUrl,width,height,sanitizeObject(options));
                }
            },
            setDialogContentUrl: function(dialogToken, contentUrl) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::setDialogContentUrl(Ljava/lang/String;Ljava/lang/String;)(dialogToken,contentUrl);
                }
            },
            closeDialog: function(dialogToken) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::closeDialog(Ljava/lang/String;)(dialogToken);
                }
            },
            showAlert: function(alertTypeName, message, options) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::showAlert(Lorg/ovirt/engine/ui/common/widget/panel/AlertPanel$Type;Ljava/lang/String;Lorg/ovirt/engine/ui/webadmin/plugin/api/AlertOptions;)(getAlertType(alertTypeName),message,sanitizeObject(options));
                }
            },
            showToast: function(toastType, message) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::showToast(Ljava/lang/String;Ljava/lang/String;)(toastType,message);
                }
            },
            revealPlace: function(historyToken) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::revealPlace(Ljava/lang/String;)(historyToken);
                }
            },
            setSearchString: function(searchString) {
                if (validatePluginAction(this.pluginName)) {
                    uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::setSearchString(Ljava/lang/String;)(searchString);
                }
            },
            loginUserName: function() {
                if (validateSafePluginAction(this.pluginName)) {
                    return user.@org.ovirt.engine.ui.common.auth.CurrentUser::getFullUserName()();
                }
            },
            loginUserId: function() {
                if (validateSafePluginAction(this.pluginName)) {
                    return user.@org.ovirt.engine.ui.common.auth.CurrentUser::getUserId()();
                }
            },
            ssoToken: function() {
                if (validateSafePluginAction(this.pluginName)) {
                    return user.@org.ovirt.engine.ui.common.auth.CurrentUser::getSsoToken()();
                }
            },
            engineBaseUrl: function() {
                if (validateSafePluginAction(this.pluginName)) {
                    return @org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils::getRootURL()() + @org.ovirt.engine.ui.frontend.utils.BaseContextPathData::getRelativePath()();
                }
            },
            currentLocale: function() {
                if (validateSafePluginAction(this.pluginName)) {
                    return uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::getCurrentLocale()();
                }
            },
            currentPlace: function() {
                if (validateSafePluginAction(this.pluginName)) {
                    return uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::getCurrentPlace()();
                }
            },
            rootTag: function() {
                if (validateSafePluginAction(this.pluginName)) {
                    return uiFunctions.@org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions::getRootTagNode()();
                }
            },
            logger: function() {
                  return {
                     info: function(message) {
                         logger.@java.util.logging.Logger::info(Ljava/lang/String;)(message);
                     },
                     warning: function(message) {
                         logger.@java.util.logging.Logger::warning(Ljava/lang/String;)(message);
                     },
                     severe: function(message) {
                         logger.@java.util.logging.Logger::severe(Ljava/lang/String;)(message);
                     }
                }
            },
            reportFatalError: function(message) {
                 ctx.@org.ovirt.engine.ui.webadmin.plugin.PluginManager::reportFatalError(Ljava/lang/String;)(message);
            }
        };

        // Give init function the pluginApi prototype for later instantiation
        pluginApi.fn.init.prototype = pluginApi.fn;

        // Expose pluginApi function as a global object
        $wnd.pluginApi = pluginApi;
    }-*/;

}
