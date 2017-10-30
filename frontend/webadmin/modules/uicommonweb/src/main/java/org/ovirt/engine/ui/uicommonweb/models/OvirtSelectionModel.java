package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Higher-order {@link SelectionModel} that delegates to either {@linkplain SingleSelectionModel single}
 * or {@linkplain OrderedMultiSelectionModel multi} selection model implementation, depending on the
 * {@code singleSelectionOnly} parameter.
 *
 * @param <T> Type of items tracked by the selection model.
 */
public class OvirtSelectionModel<T> implements SelectionModel<T> {

    private final SelectionModel<T> delegate;
    private final boolean singleSelectionOnly;

    public OvirtSelectionModel(boolean singleSelectionOnly) {
        this.singleSelectionOnly = singleSelectionOnly;
        this.delegate = singleSelectionOnly
                ? new SingleSelectionModel<>(new QueryableEntityKeyProvider<>())
                : new OrderedMultiSelectionModel<>(new QueryableEntityKeyProvider<>());
    }

    @Override
    public HandlerRegistration addSelectionChangeHandler(Handler handler) {
        return delegate.addSelectionChangeHandler(handler);
    }

    @Override
    public boolean isSelected(T object) {
        return delegate.isSelected(object);
    }

    @Override
    public void setSelected(T object, boolean selected) {
        delegate.setSelected(object, selected);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        delegate.fireEvent(event);
    }

    @Override
    public Object getKey(T item) {
        return delegate.getKey(item);
    }

    /**
     * Clear the current selection.
     */
    public void clear() {
        if (singleSelectionOnly) {
            asSingleSelectionModel().clear();
        } else {
            asMultiSelectionModel().clear();
        }
    }

    /**
     * @return List of currently selected items. If {@code singleSelectionOnly}
     * is {@code false}, the list order reflects the order in which items were
     * selected.
     */
    public List<T> getSelectedObjects() {
        return singleSelectionOnly
                ? new ArrayList<>(((SingleSelectionModel<T>) delegate).getSelectedSet())
                : ((OrderedMultiSelectionModel<T>) delegate).getSelectedList();
    }

    /**
     * @return First item of the {@linkplain #getSelectedObjects selected list}
     * or {@code null} if the selection is empty.
     */
    public T getFirstSelectedObject() {
        List<T> objects = getSelectedObjects();
        return objects.isEmpty() ? null : objects.get(0);
    }

    /**
     * @return Delegate selection model as {@link SingleSelectionModel}.
     * Don't call this if {@link #singleSelectionOnly} is {@code false}.
     */
    public SingleSelectionModel<T> asSingleSelectionModel() {
        assert singleSelectionOnly : "singleSelectionOnly value mismatch"; //$NON-NLS-1$
        return (SingleSelectionModel<T>) delegate;
    }

    /**
     * @return Delegate selection model as {@link OrderedMultiSelectionModel}.
     * Don't call this if {@link #singleSelectionOnly} is {@code true}.
     */
    public OrderedMultiSelectionModel<T> asMultiSelectionModel() {
        assert !singleSelectionOnly : "singleSelectionOnly value mismatch"; //$NON-NLS-1$
        return (OrderedMultiSelectionModel<T>) delegate;
    }

    public void setDataDisplay(HasData<T> dataDisplay) {
        if (!isSingleSelectionOnly()) {
            asMultiSelectionModel().setDataDisplay(dataDisplay);
        }
    }

    public boolean isSingleSelectionOnly() {
        return singleSelectionOnly;
    }

    /**
     * Turns multiple selection feature on or off. If the underlying selection model supports multi selection
     * otherwise does nothing.
     */
    public void setMultiSelectEnabled(boolean multiSelectEnabled) {
        if (!isSingleSelectionOnly()) {
            asMultiSelectionModel().setMultiSelectEnabled(multiSelectEnabled);
        }
    }

    /**
     * Turns multiple 'range' selection feature on or off. If the underlying selection model supports multi selection
     * otherwise does nothing.
     */
    public void setMultiRangeSelectEnabled(boolean multiRangeSelectEnabled) {
        if (!isSingleSelectionOnly()) {
            asMultiSelectionModel().setMultiRangeSelectEnabled(multiRangeSelectEnabled);
        }
    }
}
