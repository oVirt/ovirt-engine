package org.ovirt.engine.ui.webadmin.uicommon.model;

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
     */
    void setSelectedItems(List<T> items);

}
