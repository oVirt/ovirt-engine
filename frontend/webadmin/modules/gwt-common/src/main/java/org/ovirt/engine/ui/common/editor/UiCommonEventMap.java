package org.ovirt.engine.ui.common.editor;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

/**
 * A Map of {@link Event}s, used by the Editor Driver to be updated when
 * a change occurs in the Model.
 */
public class UiCommonEventMap {

    private final Map<String, Event<?>> eventMap = new HashMap<>();

    /**
     * Add an Event to the Map
     *
     * @param path The property path (i.e. "userName.entity")
     * @param type The event type (i.e. "SelectedItemsChanged")
     * @param event The Event
     */
    public void addEvent(String path, String type, Event event) {
        eventMap.put(getKey(path, type), event);
    }

    /**
     * Register a Listener to an Event from the Map
     *
     * @param path The property path (i.e. "userName.entity")
     * @param type The event type (i.e. "SelectedItemsChanged")
     * @param listener The Listener to add
     */
    public void registerListener(String path, String type, IEventListener<? extends EventArgs> listener) {
        String key = getKey(path, type);

        if (eventMap.containsKey(key)) {
            eventMap.get(key).addListener((IEventListener) listener);
        }
    }

    private String getKey(String path, String type) {
        return path + "_" + type; //$NON-NLS-1$
    }

    public void clear() {
        eventMap.clear();
    }

}
