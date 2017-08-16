package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

/**
 * Provider of main model instances.
 *
 * @param <T>
 *            Main model item type.
 * @param <M>
 *            Main model type.
 */
public interface MainModelProvider<T, M extends SearchableListModel> extends SearchableTableModelProvider<T, M> {

    /**
     * Notifies {@link org.ovirt.engine.ui.uicommonweb.models.CommonModel} that the corresponding main tab has been selected.
     */
    void onMainViewSelected();

}
