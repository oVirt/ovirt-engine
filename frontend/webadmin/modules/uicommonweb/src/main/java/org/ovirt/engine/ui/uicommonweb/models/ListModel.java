package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.HasCleanup;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IProvideCollectionChangedEvent;
import org.ovirt.engine.ui.uicompat.IProvidePropertyChangedEvent;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ProvideCollectionChangedEvent;
import org.ovirt.engine.ui.uicompat.ProvidePropertyChangedEvent;

public class ListModel<T> extends Model {

    public static final EventDefinition selectedItemChangedEventDefinition;
    private Event<EventArgs> privateSelectedItemChangedEvent;

    public Event<EventArgs> getSelectedItemChangedEvent() {
        return privateSelectedItemChangedEvent;
    }

    private void setSelectedItemChangedEvent(Event<EventArgs> value) {
        privateSelectedItemChangedEvent = value;
    }

    public static final EventDefinition selectedItemsChangedEventDefinition;
    private Event<EventArgs> privateSelectedItemsChangedEvent;

    public Event<EventArgs> getSelectedItemsChangedEvent() {
        return privateSelectedItemsChangedEvent;
    }

    private void setSelectedItemsChangedEvent(Event<EventArgs> value) {
        privateSelectedItemsChangedEvent = value;
    }

    public static final EventDefinition itemsChangedEventDefinition;
    private Event<EventArgs> privateItemsChangedEvent;

    public Event<EventArgs> getItemsChangedEvent() {
        return privateItemsChangedEvent;
    }

    private void setItemsChangedEvent(Event<EventArgs> value) {
        privateItemsChangedEvent = value;
    }

    protected List<T> selectedItems;

    public List<T> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<T> value) {
        if (selectedItems != value) {
            selectedItemsChanging(value, selectedItems);
            selectedItems = value;
            selectedItemsChanged();
            getSelectedItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("SelectedItems")); //$NON-NLS-1$
        }
    }

    protected T selectedItem;

    public T getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(T value) {
        setSelectedItem(value, true);
    }

    public void setSelectedItem(T value, boolean fireEvent) {
        if (selectedItem != value && fireEvent) {
            onSelectedItemChanging(value, selectedItem);
            selectedItem = value;
            onSelectedItemChanged();
            getSelectedItemChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("SelectedItem")); //$NON-NLS-1$
        }
    }

    protected Collection<T> items;

    public Collection<T> getItems() {
        return items;
    }

    public void setItems(Collection<T> value) {
        setItems(value, null);
    }

    public void setItems(Collection<T> value, T selectedItem) {
        if (items != value) {
            itemsChanging(value, items);
            items = value;
            itemsChanged(selectedItem);
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    private boolean isEmpty;

    /**
     * Gets or sets the value indicating whether this model is empty. Notice, that this value is not updated
     * automatically.
     */
    public boolean getIsEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(boolean value) {
        if (isEmpty != value) {
            isEmpty = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsEmpty")); //$NON-NLS-1$
        }
    }

    /**
     * Override this property and return true in order to receive property change notifications for any item but not
     * only for selected ones. Pay attention, when property change occurs either SelectedItemPropertyChanged or
     * ItemPropertyChanged will be called but not both of them.
     */
    protected boolean getNotifyPropertyChangeForAnyItem() {
        return false;
    }

    static {
        selectedItemChangedEventDefinition = new EventDefinition("SelectedItemChanged", ListModel.class); //$NON-NLS-1$
        selectedItemsChangedEventDefinition = new EventDefinition("SelectedItemsChanged", ListModel.class); //$NON-NLS-1$
        itemsChangedEventDefinition = new EventDefinition("ItemsChanged", ListModel.class); //$NON-NLS-1$
    }

    public ListModel() {
        setSelectedItemChangedEvent(new Event<>(selectedItemChangedEventDefinition));
        setSelectedItemsChangedEvent(new Event<>(selectedItemsChangedEventDefinition));
        setItemsChangedEvent(new Event<>(itemsChangedEventDefinition));
        this.selectionModel = new OvirtSelectionModel<>(isSingleSelectionOnly());
        this.selectionModel.setDataDisplay(new HasDataMinimalDelegate<T>() {
            @Override
            public Iterable<T> getVisibleItems() {
                return getItems() != null ? getItems() : Collections.emptyList();
            }
        });
        this.selectionModel.addSelectionChangeHandler(e -> synchronizeSelection());
    }

    protected void onSelectedItemChanging(T newValue, T oldValue) {
    }

    protected void onSelectedItemChanged() {
    }

    protected void selectedItemsChanged() {
    }

    protected void selectedItemsChanging(List<T> newValue, List<T> oldValue) {
        // Skip this method when notifying on property change for any
        // item but not only for selected ones is requested.
        // Subscribtion to the event will be done in ItemsCollectionChanged method.
        if (getNotifyPropertyChangeForAnyItem()) {
            return;
        }

        unsubscribeList(oldValue);
        subscribeList(newValue);
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ProvidePropertyChangedEvent.definition)) {
            if (getNotifyPropertyChangeForAnyItem()) {
                // If notification on property change for any item was requested,
                // check whether the event was sent by a selected item or not.
                boolean anyOfSelectedItem = false;
                if (getSelectedItems() != null) {
                    for (T item : getSelectedItems()) {
                        if (item == sender) {
                            anyOfSelectedItem = true;
                            break;
                        }
                    }
                }

                if (anyOfSelectedItem) {
                    selectedItemPropertyChanged(sender, (PropertyChangedEventArgs) args);
                } else {
                    itemPropertyChanged(sender, (PropertyChangedEventArgs) args);
                }
            } else {
                // In this case a sender always will be a one of selected item.
                selectedItemPropertyChanged(sender, (PropertyChangedEventArgs) args);
            }
        } else if (ev.matchesDefinition(ProvideCollectionChangedEvent.Definition)) {
            itemsCollectionChanged(sender, (NotifyCollectionChangedEventArgs<T>) args);
        }
    }

    /**
     * Invoked whenever some property of any selected item was changed.
     */
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
    }

    /**
     * Invoked whenever some property of any item was changed. For performance considerations, in order to get this
     * method called, override NotifyPropertyChangeForAnyItem property and return true.
     */
    protected void itemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
    }

    protected void itemsChanged(T selectedItem) {
        if (selectedItem == null) {
            itemsChanged();
        } else {
            List<T> selectedItems = new ArrayList<>();
            selectedItems.add(selectedItem);
            setSelectedItem(selectedItem);
            setSelectedItems(selectedItems);
            getSelectionModel().setSelected(selectedItem, true);
        }
    }

    protected void itemsChanged() {
        // if Items are updated, SelectedItem and SelectedItems become irrelevant:
        setSelectedItem(null);
        setSelectedItems(null);
    }

    protected void itemsChanging(Collection<T> newValue, Collection<T> oldValue) {
        IProvideCollectionChangedEvent notifier =
                (IProvideCollectionChangedEvent) ((oldValue instanceof IProvideCollectionChangedEvent) ? oldValue
                        : null);
        if (notifier != null) {
            notifier.getCollectionChangedEvent().removeListener(this);
        }

        notifier =
                (IProvideCollectionChangedEvent) ((newValue instanceof IProvideCollectionChangedEvent) ? newValue
                        : null);
        if (notifier != null) {
            notifier.getCollectionChangedEvent().addListener(this);
        }

        // Unsure subscribing to the property change notification for all items.
        unsubscribeList(oldValue);
        subscribeList(newValue);
    }

    /**
     * Invoked whenever items collection was changed, i.e. some items was added or removed.
     */
    protected void itemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs<T> e) {
        if (!getNotifyPropertyChangeForAnyItem()) {
            return;
        }

        // Track property change on all items as necessary.
        unsubscribeList(e.oldItems);
        subscribeList(e.newItems);
    }

    public void validateSelectedItem(IValidation[] validations) {
        setIsValid(true);

        if (!getIsAvailable() || !getIsChangable()) {
            return;
        }

        for (IValidation validation : validations) {
            ValidationResult result = validation.validate(getSelectedItem());
            if (!result.getSuccess()) {
                for (String reason : result.getReasons()) {
                    getInvalidityReasons().add(reason);
                }
                setIsValid(false);

                break;
            }
        }
    }

    private void subscribeList(Iterable<T> list) {
        if (list == null) {
            return;
        }

        for (T a : list) {
            IProvidePropertyChangedEvent notifier =
                    (IProvidePropertyChangedEvent) ((a instanceof IProvidePropertyChangedEvent) ? a : null);
            if (notifier != null) {
                notifier.getPropertyChangedEvent().addListener(this);
            }
        }
    }

    private void unsubscribeList(Iterable<T> list) {
        if (list == null) {
            return;
        }

        for (T a : list) {
            IProvidePropertyChangedEvent notifier =
                    (IProvidePropertyChangedEvent) ((a instanceof IProvidePropertyChangedEvent) ? a : null);
            if (notifier != null) {
                notifier.getPropertyChangedEvent().removeListener(this);
            }
        }
    }

    @Override
    public void cleanup() {
        cleanupEvents(getSelectedItemChangedEvent(),
                getSelectedItemsChangedEvent(),
                getItemsChangedEvent());

        if (getItems() != null) {
            for (T item : getItems()) {
                if (item instanceof HasCleanup) {
                    ((HasCleanup) item).cleanup();
                }
            }
        }

        super.cleanup();
    }

    public List<T> getItemsAsList() {
        Collection<T> items = getItems();
        return items instanceof List ? (List<T>) items : null;
    }

    protected boolean isSingleSelectionOnly() {
        // Most standard list models will have a single selection.
        return true;
    }

    private final OvirtSelectionModel<T> selectionModel;

    public OvirtSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    /**
     * This method synchronizes the item selection from the OvirtSelectionModel. If it is a
     * single selection model, it will call setSelectedItem, and if it is an OrderedMultiSelectionModel it will call
     * setSelectedItems.
     */
    private void synchronizeSelection() {
        if (isSingleSelectionOnly()) {
            setSelectedItem(selectionModel.asSingleSelectionModel().getSelectedObject());
        } else {
            List<T> selectedItems = selectionModel.getSelectedObjects();
            setSelectedItems(selectedItems);
            if (selectedItems.size() == 1) {
                setSelectedItem(selectedItems.get(0));
            } else if (selectedItems.size() == 0) {
                setSelectedItem(null);
            }
        }
    }

    /**
     * Firing this event causes the model to redraw in the UI - useful when using special rendering
     * (e. g. displaying cluster defaults)
     */
    public void fireItemsChangedEvent() {
        getItemsChangedEvent().raise(this, EventArgs.EMPTY);
        onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
    }
}
