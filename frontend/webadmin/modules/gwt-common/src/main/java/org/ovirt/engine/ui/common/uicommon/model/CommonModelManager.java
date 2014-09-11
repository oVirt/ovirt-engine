package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.shared.EventBus;

/**
 * Manages the {@link CommonModel} instance shared by all model providers.
 */
public class CommonModelManager {

    private static CommonModel commonModel = null;

    /**
     * Creates new {@link CommonModel} instance and sets up necessary event listeners.
     * <p>
     * Should be called right after successful user authentication, before redirecting the user to the main section.
     */
    public static void init(final EventBus eventBus) {
        commonModel = CommonModel.newInstance();
        commonModel.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                MainModelSelectionChangeEvent.fire(eventBus, commonModel.getSelectedItem());
            }
        });
    }

    /**
     * Returns the {@link CommonModel} instance if the user is currently logged in, {@code null} otherwise.
     */
    public static CommonModel instance() {
        return commonModel;
    }

}
