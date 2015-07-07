package org.ovirt.engine.ui.common.widget.editor;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;

public class CompositeHandlerRegistration implements HandlerRegistration {

    private final List<HandlerRegistration> registrations;

    private CompositeHandlerRegistration(List<HandlerRegistration> registrations) {
        this.registrations = registrations;
    }

    public static CompositeHandlerRegistration of(HandlerRegistration... registrations) {
        return new CompositeHandlerRegistration(Arrays.asList(registrations));
    }

    @Override
    public void removeHandler() {
        for(HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
    }
}
