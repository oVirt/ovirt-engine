package org.ovirt.engine.ui.uicommonweb.models.plugin;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class PluginModel extends SearchableListModel<Void, Void> {

    private final String historyToken;

    public PluginModel(String historyToken, String searchPrefix) {
        this.historyToken = historyToken;
        setSearchString(searchPrefix.endsWith(":") ? searchPrefix : searchPrefix + ":"); //$NON-NLS-1$ //$NON-NLS-2$
        setApplicationPlace(historyToken);
        setDefaultSearchString("");
    }

    public String getHistoryToken() {
        return historyToken;
    }

    @Override
    protected String getListName() {
        return getHistoryToken();
    }

}
