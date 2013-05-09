package org.ovirt.engine.ui.common.uicommon.model;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

/**
 * Provider of {@link SearchableListModel} instances.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public interface SearchableModelProvider<T, M extends SearchableListModel> extends ModelProvider<M> {

    /**
     * Updates the item selection of the model.
     * @param items The list of items to select.
     */
    void setSelectedItems(List<T> items);

    /**
     * Implement this method if you wish to do anything additional when manually refreshing.
     */
    void onManualRefresh();
}
