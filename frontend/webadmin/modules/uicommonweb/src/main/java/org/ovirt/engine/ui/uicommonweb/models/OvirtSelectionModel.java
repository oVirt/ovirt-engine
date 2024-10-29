package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Higher-order {@link SelectionModel} that delegates to either {@linkplain SingleSelectionModel single}
 * or {@linkplain OrderedMultiSelectionModel multi} or {@linkplain NoSelectionModel }selection model implementation.
 *
 * @param <T> Type of items tracked by the selection model.
 */
public class OvirtSelectionModel<T> implements SelectionModel<T> {

    public enum Mode {
        SINGLE_SELECTION,
        MULTI_SELECTION,
        NO_SELECTION
    }

    private final SelectionModel<T> delegate;
    private final Mode mode;

    public OvirtSelectionModel() {
        this(Mode.NO_SELECTION);
    }

    public OvirtSelectionModel(Mode mode) {
        this.mode = mode;
        switch (mode) {
        case SINGLE_SELECTION:
            this.delegate = new SingleSelectionModel<>(new QueryableEntityKeyProvider<>());
            break;
        case MULTI_SELECTION:
            this.delegate = new OrderedMultiSelectionModel<>(new QueryableEntityKeyProvider<>());
            break;
        default:
            this.delegate = new NoSelectionModel<>();
            break;
        }
    }

    public Mode getMode() {
        return mode;
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
        switch (mode) {
        case SINGLE_SELECTION:
            asSingleSelectionModel().clear();
            break;
        case MULTI_SELECTION:
            asMultiSelectionModel().clear();
            break;
        case NO_SELECTION:
            // no selection to clear
            break;
        }
    }

    /**
     * @return List of currently selected items. If {@code #isMultiSelectionOnly}
     * is {@code true}, the list order reflects the order in which items were
     * selected.
     */
    public List<T> getSelectedObjects() {
        switch (mode) {
        case SINGLE_SELECTION:
            return new ArrayList<>(asSingleSelectionModel().getSelectedSet());
        case MULTI_SELECTION:
            return asMultiSelectionModel().getSelectedList();
        case NO_SELECTION:
        default:
            return Collections.emptyList();
        }
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
     * Call this if {@link #isSingleSelectionOnly()} is {@code true}.
     */
    public SingleSelectionModel<T> asSingleSelectionModel() {
        assert isSingleSelectionOnly() : "singleSelectionOnly value mismatch"; //$NON-NLS-1$
        return (SingleSelectionModel<T>) delegate;
    }

    /**
     * @return Delegate selection model as {@link OrderedMultiSelectionModel}.
     * Call this if {@link #isMultiSelectionOnly()} is {@code true}.
     */
    public OrderedMultiSelectionModel<T> asMultiSelectionModel() {
        assert isMultiSelectionOnly() : "singleSelectionOnly value mismatch"; //$NON-NLS-1$
        return (OrderedMultiSelectionModel<T>) delegate;
    }

    private boolean isMultiSelectionOnly() {
        return mode.equals(Mode.MULTI_SELECTION);
    }

    public void setDataDisplay(HasData<T> dataDisplay) {
        if (isMultiSelectionOnly()) {
            asMultiSelectionModel().setDataDisplay(dataDisplay);
        }
    }

    public boolean isSingleSelectionOnly() {
        return mode.equals(Mode.SINGLE_SELECTION);
    }

    /**
     * Turns multiple selection feature on or off. If the underlying selection model supports multi selection
     * otherwise does nothing.
     */
    public void setMultiSelectEnabled(boolean multiSelectEnabled) {
        if (isMultiSelectionOnly()) {
            asMultiSelectionModel().setMultiSelectEnabled(multiSelectEnabled);
        }
    }

    /**
     * Turns multiple 'range' selection feature on or off. If the underlying selection model supports multi selection
     * otherwise does nothing.
     */
    public void setMultiRangeSelectEnabled(boolean multiRangeSelectEnabled) {
        if (isMultiSelectionOnly()) {
            asMultiSelectionModel().setMultiRangeSelectEnabled(multiRangeSelectEnabled);
        }
    }
}
