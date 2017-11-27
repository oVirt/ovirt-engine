package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Collections;
import java.util.List;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;

/**
 * This class provides classes with a no-op implementation of the HasData interface. For a lot of widgets that don't
 * care about a lot of the methods defined by HasData but are interested in some of them this provides a convenient
 * way to not have to implement all the methods. Simply do:
 * <pre>
 * delegate = new HasDataMinimalDelegate {
 *   // Override the methods you are interested in.
 * }
 * </pre>
 * In the widget class.
 * @param <T> The type of the data.
 */
public class HasDataMinimalDelegate<T> implements HasData<T> {

    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(
            com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        return null;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public Range getVisibleRange() {
        return null;
    }

    @Override
    public boolean isRowCountExact() {
        return false;
    }

    @Override
    public void setRowCount(int count) {
    }

    @Override
    public void setRowCount(int count, boolean isExact) {
    }

    @Override
    public void setVisibleRange(int start, int length) {
    }

    @Override
    public void setVisibleRange(Range range) {
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
    }

    @Override
    public HandlerRegistration addCellPreviewHandler(com.google.gwt.view.client.CellPreviewEvent.Handler<T> handler) {
        return null;
    }

    @Override
    public SelectionModel<? super T> getSelectionModel() {
        return null;
    }

    @Override
    public T getVisibleItem(int indexOnPage) {
        return null;
    }

    @Override
    public int getVisibleItemCount() {
        return 0;
    }

    @Override
    public Iterable<T> getVisibleItems() {
        return Collections.emptyList();
    }

    @Override
    public void setRowData(int start, List<? extends T> values) {
    }

    @Override
    public void setSelectionModel(SelectionModel<? super T> selectionModel) {
    }

    @Override
    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
    }

}
