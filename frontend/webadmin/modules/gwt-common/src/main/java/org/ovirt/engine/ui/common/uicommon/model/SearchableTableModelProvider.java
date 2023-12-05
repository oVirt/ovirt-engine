package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.widget.table.ActionTableDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

/**
 * Provider of {@link SearchableListModel} instances that is also an {@link ActionTableDataProvider}.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public interface SearchableTableModelProvider<T, M extends SearchableListModel> extends SearchableModelProvider<T, M>, ActionTableDataProvider<T> {
    /**
     * Returns base file name for exported CSV content. Or <code>null</code> if CSV export is not supported by the table
     * @return base file name or <code>null</code>
     */
    default String csvExportFilenameBase() {
        return null;
    }
}
