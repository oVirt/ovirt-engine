package org.ovirt.engine.ui.common.editor;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.uicompat.IEventListener;

/**
 * A Map of {@link IEventListener}s, used by the Editor Driver to call the relevant
 * listener when a change has occurred.
 */
public class UiCommonListenerMap {

    private final Map<String, IEventListener<?>> listenerMap = new HashMap<>();

    /**
     * Add a Listener to the map
     *
     * @param name The property name (i.e. "DefinedMemory")
     * @param type The event type (i.e. "PropertyChanged")
     * @param listener The Listener
     */
    public void addListener(String name, String type, IEventListener listener) {
        listenerMap.put(getKey(name, type), listener);
    }

    /**
     * Invoke a registered Listener
     *
     * @param name The property name (i.e. "DefinedMemory")
     * @param type The event type (i.e. "PropertyChanged")
     */
    public void callListener(String name, String type) {
        String key = getKey(name, type);

        if (listenerMap.containsKey(key)) {
            listenerMap.get(key).eventRaised(null, null, null);
        }
    }

    private String getKey(String name, String type) {
        return name + "_" + type; //$NON-NLS-1$
    }

    public void clear() {
        listenerMap.clear();
    }

}
