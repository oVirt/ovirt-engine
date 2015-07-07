package org.ovirt.engine.ui.common.widget.table;

import org.ovirt.engine.ui.common.widget.action.ActionPanel;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;

/**
 * Represents an action table widget.
 *
 * @param <T>
 *            Table row data type.
 */
public interface ActionTable<T> extends ActionPanel<T> {

    /**
     * Returns the selection model used by this table.
     */
    OrderedMultiSelectionModel<T> getSelectionModel();

    /**
     * Resets table scroll position to zero (left-most) position.
     */
    void resetScrollPosition();

    /**
     * Enforces given loading state on this table.
     */
    void setLoadingState(LoadingState state);

}
