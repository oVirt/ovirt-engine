package org.ovirt.engine.ui.common.widget.uicommon.popup.console;

import org.ovirt.engine.ui.common.widget.editor.BaseEntityModelCheckbox;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * A checkbox connected to the model which renders it's value using a renderer (similar to the ValueLabel)
 *
 * @param <T>
 *            the model type to edit
 */
public class EntityModelValueCheckbox<T> extends BaseEntityModelCheckbox<T> {

    private final ValueCheckboxRenderer<T> renderer;
    private T value;

    public EntityModelValueCheckbox(ValueCheckboxRenderer<T> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
        asCheckBox().setValue(renderer.render(value));
    }

    @Override
    public T getValue() {
        return renderer.read(asCheckBox().getValue(), value);
    }

    public interface ValueCheckboxRenderer<T> {
        boolean render(T value);

        T read(boolean value, T model);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
        return asCheckBox().addValueChangeHandler(new ValueChangeHandlerDecorator(handler));
    }

    /**
     * Intercept the onValueChange on the checkbox and fire an event which's value will not be a boolean but a type T
     * calculated using the renderer.
     */
    class ValueChangeHandlerDecorator implements ValueChangeHandler<Boolean> {

        private ValueChangeHandler<T> originalHandler;

        public ValueChangeHandlerDecorator(ValueChangeHandler<T> originalHandler) {
            this.originalHandler = originalHandler;
        }

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            originalHandler.onValueChange(new AccessibleValueChangeEvent(renderer.read(event.getValue(), value)));
        }

        class AccessibleValueChangeEvent extends ValueChangeEvent<T> {

            protected AccessibleValueChangeEvent(T value) {
                super(value);
            }

        }
    }
}
