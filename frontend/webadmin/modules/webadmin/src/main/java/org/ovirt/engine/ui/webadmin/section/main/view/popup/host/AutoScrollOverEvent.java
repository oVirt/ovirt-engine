package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class AutoScrollOverEvent extends Event<AutoScrollOverEvent.Handler> {
    public interface Handler {
        public void overAutoScroll(AutoScrollOverEvent event);
    }


    private static final Type<AutoScrollOverEvent.Handler> TYPE = new Type<>();
    protected final Widget sourceWidget;
    protected final int screenX;
    protected final int screenY;
    protected final int clientX;
    protected final int clientY;


    public static HandlerRegistration register(EventBus eventBus, AutoScrollOverEvent.Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }


    @Override
    public Type<AutoScrollOverEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.overAutoScroll(this);
    }



    public AutoScrollOverEvent(Widget sourceWidget, int screenX, int screenY, int clientX, int clientY) {
        this.sourceWidget = sourceWidget;
        this.screenX = screenX;
        this.screenY = screenY;
        this.clientX = clientX;
        this.clientY = clientY;
    }

    public Widget getSourceWidget() {
        return sourceWidget;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    public int getClientX() {
        return clientX;
    }

    public int getClientY() {
        return clientY;
    }

}
