package org.ovirt.engine.ui.uicommonweb.models;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class ValidationCompleteEvent extends GwtEvent<ValidationCompleteEvent.ValidationCompleteEventHandler>{

    Model model;

    /**
     * Constructor for serialization purposes.
     */
    protected ValidationCompleteEvent() {
        // Possibly for serialization.
    }

    protected ValidationCompleteEvent(Model model) {
        this.model = model;
    }

    /**
     * Fire the event from the source.
     * @param source The source.
     */
    public static void fire(HasHandlers source, Model model) {
        ValidationCompleteEvent eventInstance = new ValidationCompleteEvent(model);
        source.fireEvent(eventInstance);
    }

    /**
     * Fire the event from the passed in source, with the passed in event instance.
     * @param source The source.
     * @param eventInstance The event instance.
     */
    public static void fire(HasHandlers source, ValidationCompleteEvent eventInstance) {
        source.fireEvent(eventInstance);
    }

    /**
     *
     */
    public interface HasValidationCompleteEventHandlers extends HasHandlers {
        HandlerRegistration addValidationCompleteEventHandler(ValidationCompleteEventHandler handler);
    }

    /**
     * The event handler interface for this event type.
     */
    public interface ValidationCompleteEventHandler extends EventHandler {
        /**
         * Method called when validation complete event is fired.
         * @param event The event.
         */
        public void onValidationComplete(ValidationCompleteEvent event);
    }

    /**
     * Type instance.
     */
    private static final Type<ValidationCompleteEventHandler> TYPE = new Type<>();

    /**
     * Get the GWT event type.
     * @return The {@code Type} of this event.
     */
    public static Type<ValidationCompleteEventHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<ValidationCompleteEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ValidationCompleteEventHandler handler) {
        handler.onValidationComplete(this);
    }

    public Model getModel() {
        return model;
    }
}
