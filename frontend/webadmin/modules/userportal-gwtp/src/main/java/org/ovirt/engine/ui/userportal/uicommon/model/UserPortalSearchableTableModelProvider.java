package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public interface UserPortalSearchableTableModelProvider<T, M extends SearchableListModel>
    extends SearchableTableModelProvider<T, M> {

    void clearCurrentItems();
}
