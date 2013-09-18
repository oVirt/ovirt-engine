package org.ovirt.engine.ui.common.widget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * This model-backed widget may be used to display a list of values of type T, where each value is display using a
 * widget of type V. Existing values may be removed by pressing minus-signed buttons located next to them, while new
 * values may be added by pressing a plus-signed button located next to a special "ghost" entry. This special entry is
 * half-disabled, and only becomes enabled when its value is set to a valid one; it will be overlooked when flushing.
 *
 * @param <M>
 *            the model backing this widget.
 * @param <T>
 *            the type of the values contained in the backing model.
 * @param <V>
 *            the type of widget used to display each value.
 */
public abstract class AddRemoveRowWidget<M extends ListModel, T, V extends Widget & HasValueChangeHandlers<T>> extends AbstractModelBoundPopupWidget<M> {

    public interface WidgetStyle extends CssResource {
        String buttonStyle();
    }

    @UiField
    public FlowPanel contentPanel;

    @UiField
    public WidgetStyle style;

    protected CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);

    private final List<Pair<T, V>> items;

    public AddRemoveRowWidget() {
        items = new LinkedList<Pair<T, V>>();
    }

    protected void init(ListModel model) {
        items.clear();
        contentPanel.clear();
        Iterable<T> values = model.getItems();
        if (values != null) {
            for (T value : values) {
                addEntry(value);
            }
        }
        addGhostEntry();
    }

    protected void flush(ListModel model) {
        ArrayList<T> values = new ArrayList<T>();
        for (Pair<T, V> item : items) {
            T value = item.getFirst();
            if (!isGhost(value)) {
                values.add(value);
            }
        }
        model.setItems(values);
    }

    private void addGhostEntry() {
        addEntry(createGhostValue());
    }

    private void addEntry(T value) {
        final V widget = createWidget(value);
        Pair<T, V> item = new Pair<T, V>(value, widget);
        items.add(item);
        PushButton button = createButton(item);

        final boolean ghost = isGhost(value);
        toggleGhost(value, widget, ghost);
        widget.addValueChangeHandler(new ValueChangeHandler<T>() {

            private boolean wasGhost = ghost;

            @Override
            public void onValueChange(ValueChangeEvent<T> event) {
                T value = event.getValue();
                boolean becomingGhost = isGhost(value);
                if (becomingGhost != wasGhost) {
                    ((AddRemoveRowPanel) widget.getParent()).setButtonEnabled(!becomingGhost);
                    toggleGhost(value, widget, becomingGhost);
                    wasGhost = becomingGhost;
                }
            }
        });

        AddRemoveRowPanel entry = new AddRemoveRowPanel(widget, button);
        contentPanel.add(entry);

        onAdd(value, widget);
    }

    private void removeEntry(Pair<T, V> item) {
        items.remove(item);
        contentPanel.remove(item.getSecond().getParent());
        onRemove(item.getFirst(), item.getSecond());
    }

    private PushButton createButton(final Pair<T, V> item) {
        boolean ghostItem = isGhost(item.getFirst());
        final PushButton button =
                new PushButton(new Image(ghostItem ? resources.increaseIcon() : resources.decreaseIcon()));
        button.addStyleName(style.buttonStyle());
        button.setEnabled(!ghostItem);

        button.addClickHandler(ghostItem ?
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        ((AddRemoveRowPanel) item.getSecond().getParent()).swapButton(createButton(item));
                        addGhostEntry();
                    }
                } :
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        removeEntry(item);
                    }
                });

        return button;
    }

    private class AddRemoveRowPanel extends FlowPanel {

        private PushButton button;

        public AddRemoveRowPanel(Widget widget, PushButton button) {
            append(widget);
            append(button);
            this.button = button;
        }

        private void append(Widget widget) {
            widget.getElement().getStyle().setFloat(Float.LEFT);
            add(widget);
        }

        public void setButtonEnabled(boolean enabled) {
            button.setEnabled(enabled);
        }

        public void swapButton(PushButton newButton) {
            remove(button);
            append(newButton);
            button = newButton;
        }

    }

    /**
     * This method is called straight after an entry is removed. Override to specify implementation-specific behavior.
     *
     * @param value
     *            the value removed.
     * @param widget
     *            the widget removed.
     */
    protected void onRemove(T value, V widget) {
        // do nothing
    }

    /**
     * This method is called straight after an entry is added. Override to specify implementation-specific behavior.
     *
     * @param value
     *            the value added.
     * @param widget
     *            the widget added.
     */
    protected void onAdd(T value, V widget) {
        // do nothing
    }

    /**
     * This method should return a new widget of type V backed by a value of type T.
     *
     * @param value
     *            the value backing the widget.
     * @return a newly-constructed widget of type V.
     */
    protected abstract V createWidget(T value);

    /**
     * This method should manufacture a new object of type T, corresponding to a "ghost" entry as implemented by a
     * specific subclass. This object should be distinct from regular values, so that the entry could not be mistaken
     * for a regular entry.
     *
     * @return a value corresponding to a ghost entry.
     */
    protected abstract T createGhostValue();

    /**
     * This method receives a value of type T, and checks whether it corresponds to a "ghost" entry as implemented by a
     * specific subclass. Please make sure to implement it in a consistent manner with respect to
     * {@link #createGhostValue() }.
     *
     * @param value
     *            the value to check.
     * @return whether the value corresponds to a ghost entry.
     */
    protected abstract boolean isGhost(T value);

    /**
     * This method is called when the value backing the widget of type V has changed so that the widget transitioned into
     * or out of "ghost" state. It should implement the details of the widget's appearance as it moves in or out of
     * the ghost state.
     *
     * @param value
     *            the value backing the widget.
     * @param widget
     *            the widget which is transitioning to/from ghost state.
     * @param becomingGhost
     *            true if the item is entering ghost state, false if exiting ghost state.
     */
    protected abstract void toggleGhost(T value, V widget, boolean becomingGhost);

}
