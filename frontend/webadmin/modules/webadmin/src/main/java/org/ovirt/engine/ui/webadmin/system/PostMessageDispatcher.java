package org.ovirt.engine.ui.webadmin.system;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.inject.Inject;

/**
 * Intercepts HTML5 {@code message} events triggered via {@code window.postMessage} API.
 */
public class PostMessageDispatcher implements HasHandlers {

    private final EventBus eventBus;

    @Inject
    public PostMessageDispatcher(EventBus eventBus) {
        this.eventBus = eventBus;
        registerMessageEventListener();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    void onMessage(String origin, Object data, JavaScriptObject sourceWindow) {
        MessageReceivedEvent.fire(this, new MessageEventData(origin, data, sourceWindow));
    }

    private native void registerMessageEventListener() /*-{
        var context = this;

        var callback = function(event) {
            context.@org.ovirt.engine.ui.webadmin.system.PostMessageDispatcher::onMessage(Ljava/lang/String;Ljava/lang/Object;Lcom/google/gwt/core/client/JavaScriptObject;)(event.origin,event.data,event.source);
        };

        $wnd.addEventListener('message', callback, false);
    }-*/;

}
