package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class AutoScrollEnableEvent extends Event<AutoScrollEnableEvent.Handler> {
    public interface Handler {
        public void enableAutoScroll(AutoScrollEnableEvent event);
    }


    private static final Type<AutoScrollEnableEvent.Handler> TYPE = new Type<>();
    protected Widget sourceWidget;


    public static HandlerRegistration register(EventBus eventBus, AutoScrollEnableEvent.Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }


    @Override
    public Type<AutoScrollEnableEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.enableAutoScroll(this);
    }


    public AutoScrollEnableEvent(Widget source) {
        this.sourceWidget = source;
    }

    public Widget getSourceWidget() {
        return sourceWidget;
    }
}
