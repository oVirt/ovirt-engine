package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.NativeEvent;

/**
 * @param <T> table row data
 */
public interface CellClickHandler<T> {

    void onClick(NativeEvent event, T rowData);
}
