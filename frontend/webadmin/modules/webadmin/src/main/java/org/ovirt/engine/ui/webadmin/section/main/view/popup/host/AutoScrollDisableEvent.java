package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class AutoScrollDisableEvent extends Event<AutoScrollDisableEvent.Handler> {
    public interface Handler {
        public void disableAutoScroll(AutoScrollDisableEvent event);
    }


    private static final Type<AutoScrollDisableEvent.Handler> TYPE = new Type<>();
    protected Widget sourceWidget;


    public static HandlerRegistration register(EventBus eventBus, AutoScrollDisableEvent.Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }


    @Override
    public Type<AutoScrollDisableEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.disableAutoScroll(this);
    }


    public AutoScrollDisableEvent(Widget sourceWidget) {
        this.sourceWidget = sourceWidget;
    }

    public Widget getSourceWidget() {
        return sourceWidget;
    }
}
