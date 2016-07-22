package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import com.google.gwt.core.client.JavaScriptObject;
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
    private final boolean isIE;

    // IE tracks document.activeElement and fires onfocusin and onfocusout events as
    // the activeElement changes. As the onfocusout events occur, we must track what
    // the activeElement is. When an onfocusout event happens and the activeElement
    // doesn't change, then the window has lost focus.
    private JavaScriptObject activeElement;

    private boolean hasFocus = true;

    @Inject
    public ApplicationFocusManager(EventBus eventBus, ClientAgentType clientAgentType) {
        this.eventBus = eventBus;
        this.isIE = clientAgentType.isIE();
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

        if (context.@org.ovirt.engine.ui.common.system.ApplicationFocusManager::isIE) {
            $doc.attachEvent("onfocusin", onFocus);
            $doc.attachEvent("onfocusout", function() {
                if (context.@org.ovirt.engine.ui.common.system.ApplicationFocusManager::activeElement != $doc.activeElement) {
                    context.@org.ovirt.engine.ui.common.system.ApplicationFocusManager::activeElement = $doc.activeElement;
                } else {
                    onBlur();
                }
            });
        } else {
            $wnd.addEventListener("focus", onFocus, false);
            $wnd.addEventListener("blur", onBlur, false);
        }

        function onFocus() {
            context.@org.ovirt.engine.ui.common.system.ApplicationFocusManager::onWindowFocus()();
        }
        function onBlur() {
            context.@org.ovirt.engine.ui.common.system.ApplicationFocusManager::onWindowBlur()();
        }
    }-*/;

}
