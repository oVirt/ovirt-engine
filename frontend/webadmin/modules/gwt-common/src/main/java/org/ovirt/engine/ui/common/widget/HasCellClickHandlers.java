package org.ovirt.engine.ui.common.widget;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * @param <T> table row data
 */
public interface HasCellClickHandlers<T> {

    HandlerRegistration addHandler(CellClickHandler<T> handler);
}
