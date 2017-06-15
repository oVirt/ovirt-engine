package org.ovirt.engine.ui.webadmin.plugin;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.ui.webadmin.plugin.api.ApiOptions;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.IFrameElement;

/**
 * Represents a UI plugin, containing meta-data as well as runtime state.
 */
public class Plugin {

    private final PluginMetaData metaData;
    private PluginState state;

    // The iframe element used to load the plugin host page
    private final IFrameElement iframe;

    // The object containing plugin event handler functions
    private JavaScriptObject eventHandlerObject;

    // The object containing custom plugin API options
    private ApiOptions apiOptionsObject;

    public Plugin(PluginMetaData metaData, IFrameElement iframe) {
        assert metaData != null : "Plugin meta-data cannot be null"; //$NON-NLS-1$
        assert iframe != null : "Plugin iframe element cannot be null"; //$NON-NLS-1$
        assert iframe.getParentElement() == null : "Plugin iframe element must be detached from DOM"; //$NON-NLS-1$
        this.metaData = metaData;
        this.state = PluginState.DEFINED;
        this.iframe = iframe;
        this.apiOptionsObject = JavaScriptObject.createObject().cast();
    }

    public PluginMetaData getMetaData() {
        return metaData;
    }

    public String getName() {
        return getMetaData().getName();
    }

    public boolean shouldPreLoad() {
        return !getMetaData().isLazyLoad();
    }

    public boolean isInState(PluginState state) {
        return this.state == state;
    }

    public IFrameElement getIFrameElement() {
        return iframe;
    }

    public JavaScriptObject getEventHandlerObject() {
        return eventHandlerObject;
    }

    public void setEventHandlerObject(JavaScriptObject eventHandlerObject) {
        this.eventHandlerObject = eventHandlerObject;
    }

    /**
     * Returns the given function contained in plugin event handler object, or an empty (no-op) function.
     */
    public JsFunction getEventHandlerFunction(String functionName) {
        return JsFunction.get(eventHandlerObject, functionName);
    }

    public ApiOptions getApiOptionsObject() {
        return apiOptionsObject;
    }

    public void setApiOptionsObject(ApiOptions apiOptionsObject) {
        this.apiOptionsObject = apiOptionsObject;
    }

    /**
     * Verifies if the plugin is currently in one of the given states.
     * <p>
     * Returns {@code true} if successful, {@code false} otherwise.
     */
    boolean checkCurrentState(List<PluginState> possibleCurrentStates) {
        boolean match = possibleCurrentStates.contains(state);
        assert match : "Unexpected plugin state [" + state + "], should be one of: " + possibleCurrentStates; //$NON-NLS-1$ //$NON-NLS-2$
        return match;
    }

    /**
     * Verifies if the plugin is currently in one of the given states, and moves plugin state to {@code newState} if
     * successful.
     */
    void moveToState(List<PluginState> possibleCurrentStates, PluginState newState) {
        if (checkCurrentState(possibleCurrentStates)) {
            state = newState;
        }
    }

    /**
     * Verifies if the plugin is currently in the given state, and moves plugin state to {@code newState} if successful.
     */
    void moveToState(PluginState possibleCurrentState, PluginState newState) {
        moveToState(Arrays.asList(possibleCurrentState), newState);
    }

    public void markAsLoading() {
        assert iframe.getParentElement() != null : "Plugin iframe element must be attached to DOM"; //$NON-NLS-1$
        moveToState(PluginState.DEFINED, PluginState.LOADING);
    }

    public void markAsReady() {
        assert eventHandlerObject != null : "Event handler object must be assigned"; //$NON-NLS-1$
        moveToState(PluginState.LOADING, PluginState.READY);
    }

    public void markAsInitializing() {
        moveToState(PluginState.READY, PluginState.INITIALIZING);
    }

    public void markAsInUse() {
        moveToState(PluginState.INITIALIZING, PluginState.IN_USE);
    }

    public void markAsFailed() {
        assert iframe.getParentElement() == null : "Plugin iframe element must be detached from DOM"; //$NON-NLS-1$
        moveToState(Arrays.asList(PluginState.INITIALIZING, PluginState.IN_USE), PluginState.FAILED);
    }

}
