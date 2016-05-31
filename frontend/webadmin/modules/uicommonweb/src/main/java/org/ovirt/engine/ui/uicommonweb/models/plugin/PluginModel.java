package org.ovirt.engine.ui.uicommonweb.models.plugin;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class PluginModel extends SearchableListModel<Void, Void> {

    private final String listName;

    public PluginModel(String historyToken, String searchPrefix) {
        this.listName = historyToken;
        setSearchString(searchPrefix.endsWith(":") ? searchPrefix : searchPrefix + ":"); //$NON-NLS-1$ //$NON-NLS-2$
        setApplicationPlace(historyToken);
        setDefaultSearchString("");
    }

    @Override
    protected String getListName() {
        return listName;
    }
}
