package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.widget.table.ActionTableDataProvider;

public interface SearchableTableModelProvider<T, M extends SearchableListModel> extends SearchableModelProvider<T, M>, ActionTableDataProvider<T> {

}
