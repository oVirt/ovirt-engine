package org.ovirt.engine.ui.common.event;

import com.google.gwt.event.dom.client.DomEvent;

public class InputEvent extends DomEvent<InputHandler> {

    private static final Type<InputHandler> TYPE = new Type<>("input", new InputEvent()); //$NON-NLS-1$

    public static Type<InputHandler> getType() {
        return TYPE;
    }

    protected InputEvent() {
    }

    @Override
    public final Type<InputHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(InputHandler handler) {
        handler.onInput(this);
    }

}
