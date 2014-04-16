package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * List box widget that adapts to UiCommon list model items.
 *
 * @param <T>
 *            List box item type.
 */
public class ListModelListBox<T> extends ValueListBox<T> implements EditorWidget<T, TakesValueEditor<T>> {

    private TakesConstrainedValueEditor<T> editor;

    /**
     * Creates a list box that renders its items using the specified {@link Renderer}.
     *
     * @param renderer
     *            Renderer for list box items.
     */
    public ListModelListBox(Renderer<T> renderer) {
        super(renderer);
        addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                int keyCode = event.getNativeEvent().getKeyCode();
                if (keyCode == KeyCodes.KEY_DOWN || keyCode == KeyCodes.KEY_UP
                        || keyCode == KeyCodes.KEY_RIGHT || keyCode == KeyCodes.KEY_LEFT) {

                    // deferring is required to allow the ListBox's selected index to update first
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            DomEvent.fireNativeEvent(Document.get().createChangeEvent(), asListBox());
                        }
                    });
                }
            }
        });
    }

    @Override
    public TakesConstrainedValueEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
        }
        return editor;
    }

    public ListBox asListBox() {
        return (ListBox) getWidget();
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return asListBox().addKeyUpHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return asListBox().addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return asListBox().addKeyPressHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return asListBox().getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        asListBox().setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        asListBox().setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        asListBox().setTabIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return asListBox().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        asListBox().setEnabled(enabled);
    }

}
