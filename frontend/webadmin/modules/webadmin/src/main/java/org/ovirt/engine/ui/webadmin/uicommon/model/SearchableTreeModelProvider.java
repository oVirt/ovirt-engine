package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.view.client.TreeViewModel;

public interface SearchableTreeModelProvider<T, M extends SearchableListModel> extends SearchableModelProvider<T, M>, TreeViewModel {

}
