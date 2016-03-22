package org.ovirt.engine.ui.common.widget;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.dom.client.Style.Clear;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
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
public abstract class AddRemoveRowWidget<M extends ListModel<T>, T, V extends Widget & HasValueChangeHandlers<T>>
    extends AbstractModelBoundPopupWidget<M> implements HasEnabled {

    public interface WidgetStyle extends CssResource {
        String buttonStyle();
    }

    @UiField
    public FlowPanel contentPanel;

    @UiField
    public WidgetStyle style;

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    protected final List<Pair<T, V>> items;
    private final IEventListener<EventArgs> itemsChangedListener;
    private final IEventListener<PropertyChangedEventArgs> propertyChangedListener;
    private M model;
    private Collection<T> modelItems;
    private boolean enabled;
    protected boolean showGhost = true;
    protected boolean showAddButton = true;
    protected boolean usePatternFly = false;

    public AddRemoveRowWidget() {
        items = new LinkedList<>();
        itemsChangedListener = new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                init(model);
            }
        };
        propertyChangedListener = new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("IsChangable".equals(args.propertyName)) { //$NON-NLS-1$
                    setEnabled(model.getIsChangable());
                    updateEnabled();
                }
            }
        };
    }

    /**
     * This method initializes the entries of the widget, by creating an entry for each value in the backing model and
     * an additional "ghost" entry. It is called whenever this widget is edited or an ItemsChangedEvent is raised from
     * the backing model.
     *
     * @param model
     *            the model backing this widget.
     */
    protected void init(M model) {
        items.clear();
        contentPanel.clear();

        modelItems = model.getItems();
        if (modelItems == null) {
            modelItems = new LinkedList<>();
            model.setItems(modelItems); // this will invoke init() again with the empty list as values instead of null
            return;
        }

        if (modelItems.isEmpty() && showGhost) {
            T ghostValue = addGhostEntry().getFirst();
            modelItems.add(ghostValue);
        } else {
            Iterator<T> i = modelItems.iterator();
            while (i.hasNext()) {
                T value = i.next();
                addEntry(value, !i.hasNext());
            }
        }
    }

    public void setUsePatternFly(boolean use) {
        usePatternFly = use;
    }

    private void updateEnabled() {
        for (Pair<T, V> item : items) {
            toggleEnabled(item.getFirst(), item.getSecond());
        }
    }

    @Override
    public void edit(final M model) {
        // guard against multiple calls to edit()
        if (this.model != null) {
            this.model.getItemsChangedEvent().removeListener(itemsChangedListener);
            this.model.getPropertyChangedEvent().removeListener(propertyChangedListener);
        }

        this.model = model;
        model.getItemsChangedEvent().addListener(itemsChangedListener);
        model.getPropertyChangedEvent().addListener(propertyChangedListener);
        enabled = model.getIsChangable();
        setVisible(model.getIsAvailable());
        init(model);
    }

    @Override
    public M flush() {
        modelItems.clear();
        for (Pair<T, V> item : items) {
            T value = item.getFirst();
            if (!isGhost(value)) {
                modelItems.add(value);
            }
        }
        return model;
    }

    protected Pair<T, V> addGhostEntry() {
        T value = createGhostValue();
        V widget = addEntry(value, true);
        return new Pair<>(value, widget);
    }

    private V addEntry(final T value, boolean lastItem) {
        final V widget = createWidget(value);
        Pair<T, V> item = new Pair<>(value, widget);
        items.add(item);

        PushButton removeButton = createButton(item, false);
        AddRemoveRowPanel entry =
                (lastItem && showAddButton) ? new AddRemoveRowPanel(widget, !usePatternFly, removeButton, createButton(item, true))
                        : new AddRemoveRowPanel(widget, !usePatternFly, removeButton);
        contentPanel.add(entry);

        toggleEnabled(value, widget);

        widget.addValueChangeHandler(new ValueChangeHandler<T>() {

            private boolean wasGhost = isGhost(value);

            @Override
            public void onValueChange(ValueChangeEvent<T> event) {
                T value = event.getValue();
                boolean becomingGhost = isGhost(value);
                if (becomingGhost != wasGhost) {
                    wasGhost = becomingGhost;
                    if (enabled) {
                        toggleGhost(value, widget, becomingGhost);
                    }
                }
            }
        });

        return widget;
    }

    private void toggleEnabled(T value, V widget) {
        setButtonsEnabled(widget, enabled);
        if (widget instanceof HasEnabled) {
            ((HasEnabled) widget).setEnabled(enabled);
        }

        if (enabled && isGhost(value)) { // if entry is enabled, it still might need to be rendered as a ghost entry
            toggleGhost(value, widget, true);
        }
    }

    private void removeEntry(Pair<T, V> item) {
        items.remove(item);
        removeWidget(item.getSecond());
    }

    private PushButton createButton(final Pair<T, V> item, boolean plusButton) {
        final T value = item.getFirst();
        final V widget = item.getSecond();

        final PushButton button =
                new PushButton(new Image(plusButton ? resources.increaseIcon() : resources.decreaseIcon()));
        button.addStyleName(style.buttonStyle());
        button.addStyleName("buttonStyle_pfly_fix"); //$NON-NLS-1$

        button.addClickHandler(plusButton ?
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        getEntry(widget).removeLastButton();
                        Pair<T, V> item = addGhostEntry();
                        onAdd(item.getFirst(), item.getSecond());
                    }
                } :
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        if (vetoRemoveWidget(item, value, widget)) {
                            return;
                        }

                        doRemoveItem(item, value, widget);
                    }
                });

        return button;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    protected void doRemoveItem(Pair<T, V> item, T value, V widget) {
        ListIterator<Pair<T, V>> last = items.listIterator(items.size());
        if (!last.hasPrevious()) { // just a precaution; if there's no item, there should be no button
            return;
        }

        if (item == last.previous() && last.hasPrevious() && this.showAddButton) { // add plus button to previous item
            Pair<T, V> previousItem = last.previous();
            getEntry(previousItem.getSecond()).appendButton(createButton(previousItem, true));
        }

        removeEntry(item);
        onRemove(value, widget);

        if (items.isEmpty() && this.showGhost) {
            Pair<T, V> ghostItem = addGhostEntry();
            onAdd(ghostItem.getFirst(), ghostItem.getSecond());
        }
    }

    /**
     * Lets to decide if we really want to remove this item or not.
     * Everything can be removed by default.
     */
    protected boolean vetoRemoveWidget(Pair<T, V> item, T value, V widget) {
        return false;
    }

    @SuppressWarnings("unchecked")
    protected AddRemoveRowPanel getEntry(V widget) {
        return (AddRemoveRowPanel) widget.getParent();
    }

    private void removeWidget(V widget) {
        contentPanel.remove(getEntry(widget));
    }

    private void setButtonsEnabled(V widget, boolean enabled) {
        getEntry(widget).setButtonsEnabled(enabled);
    }

    protected class AddRemoveRowPanel extends FlowPanel {

        private List<PushButton> buttons = new LinkedList<>();
        private SimplePanel div = new SimplePanel();

        public AddRemoveRowPanel(Widget widget, boolean floatLeft, PushButton... buttons) {
            append(widget);
            this.buttons.clear();
            for (PushButton button : buttons) {
                append(button, floatLeft);
                this.buttons.add(button);
            }
            div.getElement().getStyle().setClear(Clear.BOTH);
            add(div);
        }

        private void append(Widget widget) {
            append(widget, true);
        }

        private void append(Widget widget, boolean floatLeft) {
            if (floatLeft) {
                widget.getElement().getStyle().setFloat(Float.LEFT);
            } else {
                widget.getElement().getStyle().setFloat(Float.RIGHT);
            }
            add(widget);
        }

        public void setButtonsEnabled(boolean enabled) {
            for (PushButton button : buttons) {
                button.setEnabled(enabled);
            }
        }

        public void removeAllButtons() {
            while (!buttons.isEmpty()) {
                removeLastButton();
            }
        }

        public void removeLastButton() {
            remove(buttons.remove(buttons.size() - 1));
        }

        public void appendButton(PushButton button) {
            buttons.add(button);
            remove(div);
            append(button);
            add(div);
        }

    }

    @Override
    public void focusInput() {
        super.focusInput();

        ListIterator<Pair<T, V>> last = items.listIterator(items.size());
        if (last.hasPrevious()) {
            V widget = last.previous().getSecond();
            if (widget instanceof Focusable) {
                ((Focusable) widget).setFocus(true);
            }
        }
    }

    /**
     * This method is called straight after an entry is added by pressing the plus button. Note that this new entry will
     * necessarily be a "ghost" entry, as the plus button always adds entries that are initially in ghost state.
     * Override to specify implementation-specific behavior.
     *
     * @param value
     *            the value added.
     * @param widget
     *            the widget added.
     */
    protected void onAdd(T value, V widget) {
        modelItems.add(value);

        if (widget instanceof Focusable) {
            ((Focusable) widget).setFocus(true);
        }
    }

    /**
     * This method is called straight after an entry is removed by pressing the minues button. Note that this entry will
     * necessarily be a non-"ghost" entry, as otherwise the minus button would have been disabled. Override to specify
     * implementation-specific behavior.
     *
     * @param value
     *            the value removed.
     * @param widget
     *            the widget removed.
     */
    protected void onRemove(T value, V widget) {
        modelItems.remove(value);
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
     * the ghost state. By default, only the buttons are enabled/disabled when changing state.
     *
     * @param value
     *            the value backing the widget.
     * @param widget
     *            the widget which is transitioning to/from ghost state.
     * @param becomingGhost
     *            true if the item is entering ghost state, false if exiting ghost state.
     */
    protected void toggleGhost(T value, V widget, boolean becomingGhost) {
        setButtonsEnabled(widget, !becomingGhost && enabled);
    }

}
