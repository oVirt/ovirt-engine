package org.ovirt.engine.ui.webadmin.widget.editor;

import java.util.List;

import org.ovirt.engine.ui.webadmin.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.webadmin.widget.HasAccess;
import org.ovirt.engine.ui.webadmin.widget.HasValidation;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.editor.client.adapters.OptionalFieldEditor;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;

/**
 * Composite editor that exposes {@link AbstractValidatedWidgetWithLabel} functionality for the given sub-Editor.
 *
 * @param <T>
 *            The type being edited.
 * @param <E>
 *            Editor type.
 * @param <W>
 *            Type that exposes validated widget functionality.
 *
 * @see OptionalFieldEditor
 */
public class WidgetWithLabelEditor<T, E extends LeafValueEditor<T>, W extends AbstractValidatedWidgetWithLabel<T, ?>>
        extends OptionalFieldEditor<T, E> implements HasValidation, HasEnabled, HasAccess, HasAllKeyHandlers, Focusable {

    public static <T, E extends LeafValueEditor<T>, W extends AbstractValidatedWidgetWithLabel<T, ?>> WidgetWithLabelEditor<T, E, W> of(
            E subEditor, W widgetWithLabel) {
        return new WidgetWithLabelEditor<T, E, W>(subEditor, widgetWithLabel);
    }

    private final E subEditor;
    private final W widgetWithLabel;

    protected WidgetWithLabelEditor(E subEditor, W widgetWithLabel) {
        super(subEditor);
        this.subEditor = subEditor;
        this.widgetWithLabel = widgetWithLabel;
    }

    public E getSubEditor() {
        return subEditor;
    }

    @Override
    public void markAsValid() {
        widgetWithLabel.markAsValid();
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        widgetWithLabel.markAsInvalid(validationHints);
    }

    @Override
    public boolean isEnabled() {
        return widgetWithLabel.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        widgetWithLabel.setEnabled(enabled);
    }

    @Override
    public boolean isAccessible() {
        return widgetWithLabel.isAccessible();
    }

    @Override
    public void setAccessible(boolean accessible) {
        widgetWithLabel.setAccessible(accessible);
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return widgetWithLabel.addKeyUpHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        widgetWithLabel.fireEvent(event);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return widgetWithLabel.addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return widgetWithLabel.addKeyPressHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return widgetWithLabel.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        widgetWithLabel.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        widgetWithLabel.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        widgetWithLabel.setTabIndex(index);
    }

}
