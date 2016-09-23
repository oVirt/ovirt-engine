package org.ovirt.engine.ui.common.system;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.inject.Inject;

/**
 * Responsible for hooking to application window focus change events.
 *
 * @see ApplicationFocusChangeEvent
 */
public class ApplicationFocusManager implements HasHandlers {

    private final EventBus eventBus;

    private boolean hasFocus = true;

    @Inject
    public ApplicationFocusManager(EventBus eventBus) {
        this.eventBus = eventBus;
        attachWindowFocusEvents();
    }

    public boolean isInFocus() {
        return hasFocus;
    }

    void onWindowFocus() {
        if (!hasFocus) {
            hasFocus = true;
            ApplicationFocusChangeEvent.fire(this, true);
        }
    }

    void onWindowBlur() {
        if (hasFocus) {
            hasFocus = false;
            ApplicationFocusChangeEvent.fire(this, false);
        }
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    private native void attachWindowFocusEvents() /*-{
        var context = this;

        $wnd.addEventListener("focus", onFocus, false);
        $wnd.addEventListener("blur", onBlur, false);

        function onFocus() {
            context.@org.ovirt.engine.ui.common.system.ApplicationFocusManager::onWindowFocus()();
        }
        function onBlur() {
            context.@org.ovirt.engine.ui.common.system.ApplicationFocusManager::onWindowBlur()();
        }
    }-*/;

}
