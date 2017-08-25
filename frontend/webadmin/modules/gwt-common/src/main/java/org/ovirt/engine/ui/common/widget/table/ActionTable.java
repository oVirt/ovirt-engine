package org.ovirt.engine.ui.common.widget.table;

import org.ovirt.engine.ui.uicommonweb.models.OvirtSelectionModel;

import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;

/**
 * Represents an action table widget.
 *
 * @param <T>
 *            Table row data type.
 */
public interface ActionTable<T> {

    /**
     * Returns the selection model used by this table.
     */
    OvirtSelectionModel<T> getSelectionModel();

    /**
     * Enforces given loading state on this table.
     */
    void setLoadingState(LoadingState state);

}
